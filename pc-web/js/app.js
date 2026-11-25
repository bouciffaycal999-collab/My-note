import {
  FIREBASE_PROJECT_ID,
  FIRESTORE_DATABASE_ID,
  SITES,
  ALL_TABS,
  DEFAULT_ROLE_POLICIES,
  DEFAULT_USERS,
  DEFAULT_CUSTOMERS,
  DEFAULT_SUPPLIERS,
  DEFAULT_STORE_INFO,
  DEFAULT_SETTINGS,
  DIVERS_PRESETS,
  TileCalculatorMath,
  resolveEffectivePermissions,
  parseFirebaseConfigInput,
  formatDa,
  formatNumber,
  formatM2,
  formatDateTimeFr,
  nowIso,
  makeId,
  round2,
  toDzWhatsAppDigits,
  computeSaleCommitPlan,
  computeDeleteInvoicePlan,
} from "./pos-domain.js";
import {
  FirebasePosSyncClient,
  loadSavedFirebaseConfig,
  saveFirebaseConfig,
  clearFirebaseConfig,
  playAlertChime,
  showBrowserDesktopNotification,
} from "./firebase-sync.js";

const TAB_META = [
  { id: "pos", label: "Vente & Panier", icon: "🛒" },
  { id: "stock", label: "Stock & Prix", icon: "📦" },
  { id: "invoices", label: "Factures & Bons", icon: "🧾" },
  { id: "dashboard", label: "Statistiques", icon: "📊" },
  { id: "customers", label: "Clients & Crédits", icon: "👥" },
  { id: "suppliers", label: "Usines & Achats", icon: "🏭" },
  { id: "caisse", label: "Caisse & Clôture", icon: "💰" },
  { id: "users", label: "Équipe & Droits", icon: "🔐" },
  { id: "settings", label: "Réglages", icon: "⚙️" },
  { id: "calculator", label: "Calculateur m²", icon: "📐" },
];

const esc = (s) =>
  String(s ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");

const syncClient = new FirebasePosSyncClient();
const urlParams = new URLSearchParams(window.location.search);
const useEmulator = urlParams.get("emulator") === "1";

const state = {
  // Cloud & Auth
  firebaseConfig: loadSavedFirebaseConfig(),
  firebaseReady: false,
  mode: "setup", // "setup" | "auth" | "ready" | "local_preview"
  googleUser: null,
  isOnline: navigator.onLine,
  isSyncing: false,
  cloudError: null,
  statusToast: null,

  // Data synced with Android
  products: [],
  invoices: [],
  customers: [...DEFAULT_CUSTOMERS],
  suppliers: [...DEFAULT_SUPPLIERS],
  supplierMovements: [],
  caisseMovements: [],
  dailyCaisseCounts: [],
  staffUsers: [...DEFAULT_USERS],
  rolePolicies: [...DEFAULT_ROLE_POLICIES],
  notifications: [],
  storeInfo: { ...DEFAULT_STORE_INFO },
  settings: { ...DEFAULT_SETTINGS },

  // Active UI & POS session
  currentUserId: "u_kader",
  isPinLocked: false,
  selectedPinUserId: "u_kader",
  pinError: "",
  activeTab: "pos",
  cart: [],
  freeLines: [],
  editingInvoice: null,

  // Filters & Selection
  posSearch: "",
  posUsine: "Toutes",
  posDimension: "Toutes",
  posInStockOnly: false,
  stockSearch: "",
  stockUsine: "Toutes",
  stockOnlyAlerts: false,
  invoiceSearch: "",
  invoiceStatusFilter: "Tous",
  customerSearch: "",
  selectedSupplierId: null,
  statsPeriod: "all", // "today" | "month" | "all"

  // Overlays
  notificationsOpen: false,
  modal: null, // { type, ... }
  knownNotifIds: new Set(),
  initialNotifLoaded: false,

  // Standalone Calculator State
  calcState: {
    roomL: 5,
    roomW: 4,
    tileL: 60,
    tileW: 60,
    tileUnit: "cm",
    m2Carton: 1.44,
    pcsCarton: 4,
    marginPct: 10,
    linkedProductId: "",
  },
};

let toastTimer = null;
function showToast(msg) {
  state.statusToast = msg;
  renderToast();
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    state.statusToast = null;
    renderToast();
  }, 3400);
}

function getCurrentUser() {
  const activeUsers = state.staffUsers.filter((u) => u.active !== false);
  return (
    activeUsers.find((u) => u.id === state.currentUserId) ||
    activeUsers[0] ||
    DEFAULT_USERS[0]
  );
}

function getEffectivePermissions() {
  return resolveEffectivePermissions(getCurrentUser(), state.rolePolicies);
}

function ensureValidActiveTab() {
  const perms = getEffectivePermissions();
  if (!perms.allowedTabs.includes(state.activeTab)) {
    state.activeTab = perms.allowedTabs[0] || "pos";
  }
}

// --- Cloud Bootstrap ---

async function bootFirebase(config) {
  try {
    state.isSyncing = true;
    state.cloudError = null;
    render();
    await syncClient.init(config, { useEmulator });
    state.firebaseReady = true;
    state.mode = "auth";

    syncClient.onAuthChange(async (user) => {
      state.googleUser = user;
      if (!user) {
        syncClient.stopListeners();
        state.mode = "auth";
        state.isSyncing = false;
        render();
        return;
      }

      state.mode = "ready";
      state.isSyncing = true;
      state.cloudError = null;
      state.initialNotifLoaded = false;
      render();

      try {
        await syncClient.ensureAccountAndSeedIfEmpty(user);
      } catch (err) {
        state.cloudError = err?.message || String(err);
      }

      syncClient.startListeners(user.uid, {
        onProducts: (list) => {
          state.products = list;
          state.isSyncing = false;
          render();
        },
        onInvoices: (list) => {
          state.invoices = list;
          render();
        },
        onCustomers: (list) => {
          if (list.length) state.customers = list;
          render();
        },
        onSuppliers: (list) => {
          if (list.length) state.suppliers = list;
          render();
        },
        onSupplierMovements: (list) => {
          state.supplierMovements = list;
          render();
        },
        onCaisseMovements: (list) => {
          state.caisseMovements = list;
          render();
        },
        onDailyCaisseCounts: (list) => {
          state.dailyCaisseCounts = list;
          render();
        },
        onStaffUsers: (list) => {
          if (list.length) state.staffUsers = list;
          ensureValidActiveTab();
          render();
        },
        onRolePolicies: (list) => {
          if (list.length) state.rolePolicies = list;
          ensureValidActiveTab();
          render();
        },
        onNotifications: (list) => {
          handleIncomingNotifications(list);
          state.notifications = list;
          render();
        },
        onStoreInfo: (info) => {
          if (info) state.storeInfo = { ...DEFAULT_STORE_INFO, ...info };
          render();
        },
        onAppSettings: (s) => {
          if (s) state.settings = { ...DEFAULT_SETTINGS, ...s };
          render();
        },
        onError: (msg) => {
          state.cloudError = msg;
          state.isSyncing = false;
          render();
        },
      });
    });
  } catch (err) {
    state.cloudError = err?.message || String(err);
    state.isSyncing = false;
    state.mode = "setup";
    render();
  }
}

function handleIncomingNotifications(list) {
  if (!state.initialNotifLoaded) {
    state.initialNotifLoaded = true;
    state.knownNotifIds = new Set(list.map((n) => n.id));
    return;
  }
  const freshUnread = list.filter((n) => !n.read && !state.knownNotifIds.has(n.id));
  for (const n of list) state.knownNotifIds.add(n.id);
  if (freshUnread.length > 0) {
    if (state.settings.enableSoundAlerts) {
      playAlertChime();
    }
    showBrowserDesktopNotification(freshUnread[0]);
    showToast(`Nouvelle alerte Dépôt : ${freshUnread[0].reference}`);
  }
}

async function runCloudWrite(fn, successMsg = null) {
  state.cloudError = null;
  if (state.mode === "local_preview") {
    if (successMsg) showToast(successMsg);
    render();
    return true;
  }
  try {
    state.isSyncing = true;
    render();
    await fn();
    const ok = await syncClient.awaitPendingSync(12000);
    state.isSyncing = false;
    if (!ok) {
      state.cloudError = "Enregistré localement — synchronisation cloud en attente de réseau.";
    }
    if (successMsg) showToast(successMsg);
    render();
    return true;
  } catch (err) {
    state.isSyncing = false;
    state.cloudError = err?.message || String(err);
    showToast("Erreur Cloud — voir la bannière");
    render();
    return false;
  }
}

async function enterLocalPreviewMode() {
  state.mode = "local_preview";
  state.isPinLocked = false;
  if (state.products.length === 0) {
    try {
      const resp = await fetch("./data/catalog_seed.json");
      if (resp.ok) {
        const seed = await resp.json();
        state.products = seed.map((p) => ({
          ...p,
          m2Carton: p.m2_carton ?? p.m2Carton ?? 1.44,
          pricingUnit: p.pricingUnit || "carton",
          prixPiece: p.prixPiece || 0,
        }));
      }
    } catch {
      // ignore
    }
  }
  render();
}

// --- Business Actions ---

function addOrUpdateCartItemFromModal(product, { cartons, loosePieces, prixUnitaire, prixPiece, bain, calibre }) {
  const perms = getEffectivePermissions();
  const unitPrice = perms.canModifyPrices ? Math.max(0, Number(prixUnitaire) || 0) : product.prix;
  const piecePrice = perms.canModifyPrices ? Math.max(0, Number(prixPiece) || 0) : product.prixPiece || 0;
  const cleanCartons = Math.max(0, Math.trunc(Number(cartons) || 0));
  const cleanPieces = Math.max(0, Math.trunc(Number(loosePieces) || 0));
  if (cleanCartons === 0 && cleanPieces === 0) {
    showToast("Saisissez au moins 1 carton ou 1 pièce.");
    return;
  }
  const totals = TileCalculatorMath.calculateCartItemTotals(
    cleanCartons,
    cleanPieces,
    product.m2Carton,
    product.pcs,
    unitPrice,
    piecePrice
  );
  const existingIdx = state.cart.findIndex(
    (c) => c.productId === product.id && (c.bain || "") === (bain || "") && (c.calibre || "") === (calibre || "")
  );
  if (existingIdx >= 0) {
    const prev = state.cart[existingIdx];
    const nextCartons = prev.cartons + cleanCartons;
    const nextPieces = prev.pieces + cleanPieces;
    const nextTotals = TileCalculatorMath.calculateCartItemTotals(
      nextCartons,
      nextPieces,
      product.m2Carton,
      product.pcs,
      unitPrice,
      piecePrice
    );
    state.cart[existingIdx] = {
      ...prev,
      prixUnitaire: unitPrice,
      prixPiece: piecePrice,
      cartons: nextCartons,
      pieces: nextPieces,
      m2Total: nextTotals.m2Total,
      sousTotal: nextTotals.sousTotal,
    };
  } else {
    state.cart.push({
      id: makeId("ci"),
      productId: product.id,
      name: product.name,
      usine: product.usine,
      dimension: product.dimension,
      m2Carton: product.m2Carton,
      pcs: product.pcs,
      prixUnitaire: unitPrice,
      prixAchat: product.prixAchat,
      cartons: cleanCartons,
      pieces: cleanPieces,
      m2Total: totals.m2Total,
      sousTotal: totals.sousTotal,
      bain: bain || "",
      calibre: calibre || "",
      color: product.color || "#94a3b8",
      colorName: product.colorName || "",
      image: product.image || "",
      prixPiece: piecePrice,
      pricingUnit: product.pricingUnit || "carton",
    });
  }
  state.modal = null;
  showToast(`${product.name} ajouté au panier`);
  render();
}

function addFreeLineItem({ designation, quantite, unite, prixUnitaire }) {
  const q = Math.max(0.01, Number(quantite) || 1);
  const p = Math.max(0, Number(prixUnitaire) || 0);
  state.freeLines.push({
    id: makeId("fl"),
    designation: designation || "Article divers",
    quantite: q,
    unite: unite || "u",
    prixUnitaire: p,
    sousTotal: round2(q * p),
  });
  showToast("Ligne libre ajoutée");
  render();
}

async function submitSaleCheckout({ clientId, clientNom, clientTel, clientAdresse, remise, modePaiement, montantPaye, notes }) {
  const user = getCurrentUser();
  const perms = getEffectivePermissions();
  const client = clientId
    ? (() => {
        const found = state.customers.find((c) => c.id === clientId);
        return found
          ? { id: found.id, nom: found.nom, tel: found.tel, adresse: found.adresse }
          : { id: null, nom: clientNom || "Client Comptoir", tel: clientTel || "", adresse: clientAdresse || "" };
      })()
    : { id: null, nom: (clientNom || "").trim() || "Client Comptoir", tel: clientTel || "", adresse: clientAdresse || "" };

  const sellerName = (state.settings.deviceSellerName || "").trim() || user.name || "Vendeur";
  const sellerPhone = (state.settings.deviceSellerPhone || "").trim() || user.phone || "";
  const magasin = state.settings.activeStore || user.magasin || "Depot Remchi";

  const plan = computeSaleCommitPlan({
    cart: state.cart,
    freeLines: state.freeLines,
    client,
    remise,
    montantPaye,
    modePaiement,
    notes,
    editingInvoice: state.editingInvoice,
    existingInvoicesCount: state.invoices.length,
    sellerName,
    sellerPhone,
    magasin,
    canModifyPrices: perms.canModifyPrices,
    products: state.products,
    customers: state.customers,
  });

  if (state.mode === "local_preview") {
    // Apply locally
    if (plan.isEdit) {
      state.invoices = state.invoices.map((i) => (i.id === plan.invoice.id ? plan.invoice : i));
    } else {
      state.invoices = [plan.invoice, ...state.invoices];
    }
    state.products = state.products.map((p) =>
      plan.stockDeltas[p.id] ? { ...p, stock: p.stock + plan.stockDeltas[p.id] } : p
    );
    if (plan.updatedCustomer) {
      state.customers = state.customers.map((c) => (c.id === plan.updatedCustomer.id ? plan.updatedCustomer : c));
    }
    if (plan.caisseMovement) state.caisseMovements = [plan.caisseMovement, ...state.caisseMovements];
    state.notifications = [...plan.notifications, ...state.notifications];
  } else {
    const ok = await runCloudWrite(() => syncClient.commitSaleBatch(plan));
    if (!ok) return;
  }

  for (const n of plan.notifications) state.knownNotifIds.add(n.id);
  state.cart = [];
  state.freeLines = [];
  state.editingInvoice = null;
  state.modal = { type: "invoice_preview", invoiceId: plan.invoice.id, snapshot: plan.invoice };
  showToast(`Facture #${plan.invoice.reference} enregistrée`);
  render();
}

async function deleteInvoice(invoice) {
  const perms = getEffectivePermissions();
  if (!perms.canDeleteInvoices) return showToast("Permission suppression requise.");
  if (!confirm(`Supprimer la facture #${invoice.reference} et restituer le stock ?`)) return;

  const user = getCurrentUser();
  const plan = computeDeleteInvoicePlan({
    invoice,
    actorName: user.name,
    customers: state.customers,
  });

  if (state.mode === "local_preview") {
    state.invoices = state.invoices.filter((i) => i.id !== invoice.id);
    state.products = state.products.map((p) =>
      plan.restoredStockDeltas[p.id] ? { ...p, stock: p.stock + plan.restoredStockDeltas[p.id] } : p
    );
    if (plan.updatedCustomer) {
      state.customers = state.customers.map((c) => (c.id === plan.updatedCustomer.id ? plan.updatedCustomer : c));
    }
    state.notifications = [plan.notification, ...state.notifications];
    state.modal = null;
    showToast("Facture supprimée & stock restitué");
    render();
    return;
  }

  const ok = await runCloudWrite(
    () => syncClient.commitDeleteInvoiceBatch(invoice.id, plan),
    "Facture supprimée & stock restitué"
  );
  if (ok) state.modal = null;
}

async function requestDepotDispo(product) {
  const user = getCurrentUser();
  const site = state.settings.activeStore || user.magasin || "Depot Remchi";
  const m2Stock = Math.max(0, product.stock) * product.m2Carton;
  const notif = {
    id: makeId("notif"),
    invoiceId: product.id,
    reference: `[DISPO] ${product.name}`.slice(0, 60),
    date: nowIso(),
    vendeur: user.name,
    magasin: site,
    client: `Demande Dispo Dépôt • ${product.usine}`,
    clientTel: "",
    totalNet: product.prix,
    m2Total: round2(m2Stock),
    cartonsTotal: Math.max(0, product.stock),
    articlesSummary: `Vérification dépôt demandée par ${user.name} (${site}) pour ${product.name} (${product.dimension}) — Stock actuel: ${product.stock} ctn (${formatM2(m2Stock)} m²)`,
    read: false,
  };
  state.knownNotifIds.add(notif.id);
  if (state.mode === "local_preview") {
    state.notifications = [notif, ...state.notifications];
    showToast("Dispo Dépôt enregistrée");
    render();
    return;
  }
  await runCloudWrite(() => syncClient.createNotification(notif), `Dispo Dépôt envoyée (${product.name})`);
}

function buildWhatsAppInvoiceText(invoice, store) {
  return [
    `*${store.nom} — Facture #${invoice.reference}*`,
    `📅 ${formatDateTimeFr(invoice.date)} | 🏪 ${invoice.magasin}`,
    `👤 Client: ${invoice.client?.nom || "Comptoir"} ${invoice.client?.tel ? `(${invoice.client.tel})` : ""}`,
    `🧑‍💼 Vendeur: ${invoice.vendeur}`,
    `---`,
    ...(invoice.articles || []).map(
      (a) =>
        `• ${a.name}: ${a.cartons} ctn${a.pieces ? ` + ${a.pieces} pcs` : ""} (${formatM2(a.m2Total)} m²)${
          invoice.hidePrices ? "" : ` = ${formatDa(a.sousTotal)}`
        }`
    ),
    ...(invoice.lignesLibres || []).map(
      (l) => `• ${l.designation} x${l.quantite}${invoice.hidePrices ? "" : ` = ${formatDa(l.sousTotal)}`}`
    ),
    invoice.hidePrices ? "Bon de livraison (Sans Prix)" : `*TOTAL NET: ${formatDa(invoice.totalNet)}* (${invoice.statut})`,
  ]
    .filter(Boolean)
    .join("\n");
}

function renderInvoicePhotoBlob(invoice, store, hideBain) {
  const hidePrices = Boolean(invoice.hidePrices);
  const articles = invoice.articles || [];
  const libres = invoice.lignesLibres || [];
  const rowCount = Math.max(1, articles.length + libres.length);
  const width = 900;
  const headerH = 215;
  const rowH = 54;
  const footerH = 175;
  const height = headerH + 46 + rowCount * rowH + footerH;

  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const ctx = canvas.getContext("2d");

  ctx.fillStyle = "#FFFFFF";
  ctx.fillRect(0, 0, width, height);

  ctx.fillStyle = "#142824";
  ctx.fillRect(0, 0, width, 112);

  ctx.fillStyle = "#FFFFFF";
  ctx.font = "bold 30px sans-serif";
  ctx.fillText(store.nom || "CERAMICA", 32, 48);
  ctx.fillStyle = "#E6F4F1";
  ctx.font = "15px sans-serif";
  ctx.fillText((store.adresse || "").slice(0, 72), 32, 74);
  ctx.fillText(`Tél: ${store.tel || ""}${store.tel2 ? " / " + store.tel2 : ""}`, 32, 96);

  ctx.textAlign = "right";
  ctx.fillStyle = "#FFFFFF";
  ctx.font = "bold 24px monospace";
  ctx.fillText(`${hidePrices ? "BON LIVRAISON" : "FACTURE"} #${invoice.reference}`, width - 32, 48);
  ctx.font = "15px sans-serif";
  ctx.fillStyle = "#FDE68A";
  ctx.fillText(formatDateTimeFr(invoice.date), width - 32, 74);
  ctx.fillStyle = "#FFFFFF";
  ctx.fillText(`${invoice.magasin || ""} • Vendeur: ${invoice.vendeur || ""}`, width - 32, 96);
  ctx.textAlign = "left";

  // Client strip
  ctx.fillStyle = "#F5F2EC";
  ctx.fillRect(28, 128, width - 56, 66);
  ctx.strokeStyle = "#DDD6C8";
  ctx.lineWidth = 1.5;
  ctx.strokeRect(28, 128, width - 56, 66);
  ctx.fillStyle = "#161412";
  ctx.font = "bold 17px sans-serif";
  ctx.fillText(`Client : ${invoice.client?.nom || "Client Comptoir"}${invoice.client?.tel ? " (" + invoice.client.tel + ")" : ""}`, 44, 156);
  ctx.fillStyle = "#534D45";
  ctx.font = "14px sans-serif";
  ctx.fillText(`Statut : ${invoice.statut || ""} (${invoice.modePaiement || ""})`, 44, 180);

  // Table header
  let y = headerH;
  ctx.fillStyle = "#142824";
  ctx.fillRect(28, y, width - 56, 38);
  ctx.fillStyle = "#FFFFFF";
  ctx.font = "bold 14px sans-serif";
  ctx.fillText("Désignation", 40, y + 24);
  ctx.fillText("Qté", 435, y + 24);
  ctx.fillText("Surface", 550, y + 24);
  if (!hidePrices) {
    ctx.textAlign = "right";
    ctx.fillText("P.U", 720, y + 24);
    ctx.fillText("Total", width - 40, y + 24);
    ctx.textAlign = "left";
  }
  y += 42;

  const allRows = [
    ...articles.map((a) => ({
      title: a.name,
      sub: `${a.usine || ""} • ${a.dimension || ""}${!hideBain && (a.bain || a.calibre) ? ` • Bain ${a.bain} Cal ${a.calibre}` : ""}`,
      qty: `${a.cartons} ctn${a.pieces ? " +" + a.pieces + "p" : ""}`,
      m2: `${formatM2(a.m2Total)} m²`,
      pu: formatDa(a.prixUnitaire),
      total: formatDa(a.sousTotal),
    })),
    ...libres.map((l) => ({
      title: l.designation,
      sub: "Article divers",
      qty: `${l.quantite} ${l.unite || ""}`,
      m2: "—",
      pu: formatDa(l.prixUnitaire),
      total: formatDa(l.sousTotal),
    })),
  ];

  allRows.forEach((r, idx) => {
    if (idx % 2 === 1) {
      ctx.fillStyle = "#FAF8F4";
      ctx.fillRect(28, y, width - 56, rowH);
    }
    ctx.fillStyle = "#161412";
    ctx.font = "bold 15px sans-serif";
    ctx.fillText(String(r.title || "").slice(0, 38), 40, y + 22);
    ctx.fillStyle = "#64748B";
    ctx.font = "12px sans-serif";
    ctx.fillText(String(r.sub || "").slice(0, 48), 40, y + 41);

    ctx.fillStyle = "#161412";
    ctx.font = "bold 14px monospace";
    ctx.fillText(r.qty, 435, y + 30);
    ctx.fillText(r.m2, 550, y + 30);
    if (!hidePrices) {
      ctx.textAlign = "right";
      ctx.font = "14px monospace";
      ctx.fillText(r.pu, 720, y + 30);
      ctx.font = "bold 15px monospace";
      ctx.fillText(r.total, width - 40, y + 30);
      ctx.textAlign = "left";
    }
    ctx.strokeStyle = "#E2E8F0";
    ctx.beginPath();
    ctx.moveTo(28, y + rowH);
    ctx.lineTo(width - 28, y + rowH);
    ctx.stroke();
    y += rowH;
  });

  // Footer
  y += 18;
  ctx.strokeStyle = "#142824";
  ctx.lineWidth = 2;
  ctx.strokeRect(28, y, width - 56, 128);
  const totalM2 = articles.reduce((s, a) => s + Number(a.m2Total || 0), 0);
  ctx.fillStyle = "#161412";
  ctx.font = "bold 16px sans-serif";
  ctx.fillText(`Surface totale : ${formatM2(totalM2)} m²`, 44, y + 32);
  ctx.font = "14px sans-serif";
  ctx.fillStyle = "#534D45";
  ctx.fillText(`Vendeur : ${invoice.vendeur || ""}${invoice.vendeurTel ? " (" + invoice.vendeurTel + ")" : ""}`, 44, y + 60);
  ctx.fillText("☐ Livraison Complète    ☐ Livraison Partielle", 44, y + 92);

  if (!hidePrices) {
    ctx.textAlign = "right";
    ctx.fillStyle = "#142824";
    ctx.font = "bold 24px monospace";
    ctx.fillText(`TOTAL NET : ${formatDa(invoice.totalNet)}`, width - 44, y + 40);
    ctx.font = "bold 16px monospace";
    ctx.fillStyle = "#0F766E";
    ctx.fillText(`Payé (${invoice.modePaiement || ""}) : ${formatDa(invoice.montantPaye)}`, width - 44, y + 72);
    if (Number(invoice.resteAPayer || 0) > 0.01) {
      ctx.fillStyle = "#DC2626";
      ctx.fillText(`Reste : ${formatDa(invoice.resteAPayer)}`, width - 44, y + 102);
    }
    ctx.textAlign = "left";
  }

  return new Promise((resolve) => canvas.toBlob((blob) => resolve(blob), "image/png"));
}

function triggerPngDownload(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 4000);
}

async function sendInvoicePhotoToBureauWhatsApp(invoice, phoneDigits, { downloadOnly = false } = {}) {
  const store = state.storeInfo;
  const blob = await renderInvoicePhotoBlob(invoice, store, Boolean(state.settings.hideBainTeinteFacture));
  if (blob) {
    if (downloadOnly) {
      triggerPngDownload(blob, `Facture_${invoice.reference}.png`);
      showToast(`Photo PNG Facture #${invoice.reference} téléchargée !`);
      return;
    }
    let copied = false;
    if (navigator.clipboard && window.ClipboardItem) {
      try {
        await navigator.clipboard.write([new ClipboardItem({ "image/png": blob })]);
        copied = true;
      } catch {}
    }
    if (!copied) {
      triggerPngDownload(blob, `Facture_${invoice.reference}.png`);
    }
    showToast(
      copied
        ? "Photo facture copiée ! Collez (Ctrl+V) dans WhatsApp Web."
        : "Photo facture téléchargée — joignez-la dans WhatsApp Web."
    );
  }

  const targetDigits = phoneDigits || "213676754124";
  const url = `https://wa.me/${targetDigits}`;
  window.open(url, "_blank", "noopener");

  state.invoices = state.invoices.map((i) => (i.id === invoice.id ? { ...i, whatsappSentBeforePrint: true } : i));
  if (state.modal?.snapshot && state.modal.snapshot.id === invoice.id) {
    state.modal.snapshot.whatsappSentBeforePrint = true;
  }
  if (state.mode !== "local_preview") {
    await runCloudWrite(() => syncClient.markInvoiceWhatsappSent(invoice.id));
  }
  renderModal();
}

function openWhatsAppInvoice(invoice, phoneDigits) {
  return sendInvoicePhotoToBureauWhatsApp(invoice, phoneDigits);
}

function exportJsonBackup() {
  const payload = {
    exportedAt: nowIso(),
    products: state.products,
    invoices: state.invoices,
    customers: state.customers,
    suppliers: state.suppliers,
    caisseMovements: state.caisseMovements,
    staffUsers: state.staffUsers,
    rolePolicies: state.rolePolicies,
    storeInfo: state.storeInfo,
    settings: state.settings,
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = `ceramica_backup_${new Date().toISOString().slice(0, 10)}.json`;
  a.click();
  URL.revokeObjectURL(a.href);
  showToast("Sauvegarde JSON téléchargée");
}

// --- Rendering ---

function render() {
  const root = document.getElementById("app-root");
  if (!root) return;

  if (state.mode === "setup") {
    root.innerHTML = renderFirebaseSetupScreen();
    bindSetupHandlers();
    renderModal();
    renderDrawer();
    renderToast();
    return;
  }

  if (state.mode === "auth") {
    root.innerHTML = renderGoogleLoginScreen();
    bindGoogleLoginHandlers();
    renderModal();
    renderDrawer();
    renderToast();
    return;
  }

  if (state.isPinLocked) {
    root.innerHTML = renderPinLockScreen();
    bindPinLockHandlers();
    renderModal();
    renderDrawer();
    renderToast();
    return;
  }

  const user = getCurrentUser();
  const perms = getEffectivePermissions();
  const unreadNotifs = state.notifications.filter((n) => !n.read).length;

  root.innerHTML = `
    <div class="app-shell">
      <header class="topbar">
        <div class="brand">
          <div class="brand-mark">CR</div>
          <div>
            <div class="brand-title">${esc(state.storeInfo.nom || "CERAMICA")} <span style="font-weight:500;opacity:0.7;font-size:12px">• PC Browser</span></div>
            <div class="brand-sub">${esc(state.googleUser?.email || "Mode Aperçu Local")}</div>
          </div>
        </div>

        <div class="topbar-right">
          <select id="top-store-select" class="store-select-top" title="Magasin actif">
            ${SITES.map(
              (s) => `<option value="${esc(s)}" ${s === state.settings.activeStore ? "selected" : ""}>🏪 ${esc(s)}</option>`
            ).join("")}
          </select>

          <button id="top-cloud-pill" class="cloud-pill" title="État de synchronisation Android & Cloud">
            <span class="dot ${
              state.mode === "local_preview"
                ? "offline"
                : state.cloudError
                ? "error"
                : state.isSyncing
                ? "syncing"
                : state.isOnline
                ? "online"
                : "offline"
            }"></span>
            <span>${
              state.mode === "local_preview"
                ? "APERÇU LOCAL"
                : state.cloudError
                ? "ERREUR SYNC"
                : state.isSyncing
                ? "SYNCHRO..."
                : state.isOnline
                ? "EN LIGNE • SYNC"
                : "HORS-LIGNE"
            }</span>
          </button>

          <button id="top-notif-btn" class="topbar-btn" title="Alertes Dépôt en direct">
            🔔 <span class="hidden-sm">Alertes</span>
            ${unreadNotifs > 0 ? `<span class="badge-count">${unreadNotifs}</span>` : ""}
          </button>

          <button id="top-lock-btn" class="topbar-btn" title="Changer d'utilisateur / Verrouiller PIN">
            👤 <span>${esc(user.name)}</span>
            <span class="chip chip-teal" style="padding:2px 7px;font-size:10.5px">${esc(user.role.toUpperCase())}</span>
          </button>
        </div>
      </header>

      <aside class="sidebar">
        ${TAB_META.filter((t) => perms.allowedTabs.includes(t.id))
          .map(
            (t) => `
          <button class="nav-item ${state.activeTab === t.id ? "active" : ""}" data-tab="${t.id}">
            <span class="nav-icon">${t.icon}</span>
            <span class="nav-label">${esc(t.label)}</span>
          </button>`
          )
          .join("")}

        <div class="sidebar-footer">
          <div>📦 ${state.products.length} références</div>
          <div>🧾 ${state.invoices.length} factures</div>
          ${
            state.mode === "ready"
              ? `<button id="sidebar-signout-btn" class="btn btn-secondary btn-sm" style="margin-top:4px">Déconnexion Google</button>`
              : `<button id="sidebar-connect-cloud-btn" class="btn btn-primary btn-sm" style="margin-top:4px">Connecter Cloud</button>`
          }
        </div>
      </aside>

      <main class="main-content">
        ${
          state.cloudError
            ? `<div class="alert-banner error">
                <span>⚠️ ${esc(state.cloudError)}</span>
                <button id="dismiss-cloud-err" class="btn btn-secondary btn-sm">Fermer</button>
              </div>`
            : ""
        }
        ${
          state.mode === "local_preview"
            ? `<div class="alert-banner warn">
                <span>Mode Aperçu Local (non synchronisé avec Android). Configurez Firebase Web pour activer la synchro temps réel.</span>
                <button id="banner-setup-cloud" class="btn btn-accent btn-sm">Connecter Firebase</button>
              </div>`
            : ""
        }
        ${renderActiveTab()}
      </main>
    </div>
  `;

  bindAppShellHandlers();
  bindActiveTabHandlers();
  renderModal();
  renderDrawer();
  renderToast();
}

function renderFirebaseSetupScreen() {
  return `
    <div class="auth-screen">
      <div class="auth-card">
        <div class="brand" style="margin-bottom:18px">
          <div class="brand-mark">CR</div>
          <div>
            <div class="brand-title" style="color:var(--ink-primary);font-size:20px">CERAMICA POS — Version Navigateur PC</div>
            <div class="brand-sub" style="color:var(--ink-muted)">Synchronisation temps réel avec l'application Android</div>
          </div>
        </div>

        <div class="alert-banner info" style="margin-bottom:16px">
          <span>Projet Firebase lié : <strong class="mono">${FIREBASE_PROJECT_ID}</strong></span>
        </div>

        <div style="background:var(--bg-canvas);border:1px solid var(--border);border-radius:12px;padding:14px;margin-bottom:16px;font-size:13px">
          <strong>Configuration en 2 étapes (une seule fois sur ce PC) :</strong>
          <ol style="margin:8px 0 6px;padding-left:18px;color:var(--ink-secondary)">
            <li>Ouvrez les paramètres Firebase du projet <code>${FIREBASE_PROJECT_ID}</code> et ajoutez une app <strong>Web (&lt;/&gt;)</strong> si ce n'est pas encore fait.</li>
            <li>Copiez le bloc <code>const firebaseConfig = { ... }</code> et collez-le ci-dessous.</li>
          </ol>
          <a href="https://console.firebase.google.com/project/${FIREBASE_PROJECT_ID}/settings/general" target="_blank" rel="noopener" class="btn btn-secondary btn-sm" style="text-decoration:none;margin-top:4px">
            Ouvrir Firebase Console (${FIREBASE_PROJECT_ID}) ↗
          </a>
        </div>

        <label class="field-label" for="fb-config-input">Coller firebaseConfig (JS ou JSON)</label>
        <textarea id="fb-config-input" class="textarea mono" rows="7" placeholder='const firebaseConfig = {\n  apiKey: "...",\n  authDomain: "${FIREBASE_PROJECT_ID}.firebaseapp.com",\n  projectId: "${FIREBASE_PROJECT_ID}",\n  ...\n};'>${
          state.firebaseConfig ? esc(JSON.stringify(state.firebaseConfig, null, 2)) : ""
        }</textarea>
        <div id="fb-config-err" style="color:var(--rose);font-size:12.5px;font-weight:700;margin-top:6px"></div>

        <div style="display:flex;gap:10px;flex-wrap:wrap;margin-top:16px">
          <button id="save-fb-config-btn" class="btn btn-primary" style="flex:1">Enregistrer &amp; Connecter Google</button>
          <button id="enter-local-preview-btn" class="btn btn-secondary">Aperçu Hors-Ligne</button>
        </div>
      </div>
    </div>
  `;
}

function renderGoogleLoginScreen() {
  return `
    <div class="auth-screen">
      <div class="auth-card">
        <div class="brand" style="margin-bottom:16px">
          <div class="brand-mark">CR</div>
          <div>
            <div class="brand-title" style="color:var(--ink-primary);font-size:20px">Connexion Cloud CERAMICA</div>
            <div class="brand-sub" style="color:var(--ink-muted)">Projet : ${esc(state.firebaseConfig?.projectId || FIREBASE_PROJECT_ID)}</div>
          </div>
        </div>

        <p style="color:var(--ink-secondary);margin-top:0">
          Connectez-vous avec <strong>le même compte Google que sur vos appareils Android</strong> pour retrouver en direct tout votre stock, vos factures, vos clients, votre caisse et les alertes dépôt.
        </p>

        ${
          state.cloudError
            ? `<div class="alert-banner error" style="margin-bottom:14px"><span>${esc(state.cloudError)}</span></div>`
            : ""
        }

        <div style="display:flex;flex-direction:column;gap:10px;margin-top:18px">
          <button id="google-signin-btn" class="btn btn-primary" style="min-height:46px;font-size:15px">
            Se connecter avec Google (En Ligne)
          </button>
          <div style="display:flex;gap:10px">
            <button id="edit-fb-config-btn" class="btn btn-secondary" style="flex:1">Modifier Config Firebase</button>
            <button id="enter-local-preview-btn" class="btn btn-secondary" style="flex:1">Aperçu Local</button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderPinLockScreen() {
  const users = state.staffUsers.filter((u) => u.active !== false);
  return `
    <div class="auth-screen">
      <div class="auth-card">
        <div class="brand" style="margin-bottom:16px">
          <div class="brand-mark">🔐</div>
          <div>
            <div class="brand-title" style="color:var(--ink-primary);font-size:19px">Poste Verrouillé — Choix Opérateur</div>
            <div class="brand-sub" style="color:var(--ink-muted)">Sélectionnez votre profil et entrez votre code PIN</div>
          </div>
        </div>

        <div class="pin-grid">
          ${users
            .map(
              (u) => `
            <button class="pin-user-btn ${state.selectedPinUserId === u.id ? "selected" : ""}" data-pin-user="${esc(u.id)}">
              <div style="font-weight:800">${esc(u.name)}</div>
              <div style="font-size:11.5px;color:var(--ink-secondary)">${esc(u.role.toUpperCase())} • ${esc(u.magasin)}</div>
            </button>`
            )
            .join("")}
        </div>

        <label class="field-label" for="pin-input">Code PIN</label>
        <input id="pin-input" type="password" inputmode="numeric" class="input mono" placeholder="Ex: 1234" autofocus />
        ${
          state.pinError
            ? `<div style="color:var(--rose);font-weight:700;font-size:12.5px;margin-top:6px">${esc(state.pinError)}</div>`
            : ""
        }
        <button id="pin-unlock-btn" class="btn btn-primary" style="width:100%;margin-top:14px;min-height:44px">
          Déverrouiller Session
        </button>
      </div>
    </div>
  `;
}

function renderActiveTab() {
  switch (state.activeTab) {
    case "pos":
      return renderPosTab();
    case "stock":
      return renderStockTab();
    case "invoices":
      return renderInvoicesTab();
    case "dashboard":
      return renderDashboardTab();
    case "customers":
      return renderCustomersTab();
    case "suppliers":
      return renderSuppliersTab();
    case "caisse":
      return renderCaisseTab();
    case "users":
      return renderUsersTab();
    case "settings":
      return renderSettingsTab();
    case "calculator":
      return renderCalculatorTab();
    default:
      return renderPosTab();
  }
}

// --- Tab 1: POS (Vente & Panier) ---

function renderPosTab() {
  const usines = ["Toutes", ...Array.from(new Set(state.products.map((p) => p.usine).filter(Boolean))).sort()];
  const dims = ["Toutes", ...Array.from(new Set(state.products.map((p) => p.dimension).filter(Boolean))).sort()];
  const q = state.posSearch.trim().toLowerCase();

  const filtered = state.products.filter((p) => {
    if (state.posUsine !== "Toutes" && p.usine !== state.posUsine) return false;
    if (state.posDimension !== "Toutes" && p.dimension !== state.posDimension) return false;
    if (state.posInStockOnly && p.stock <= 0) return false;
    if (q) {
      const hay = `${p.name} ${p.usine} ${p.dimension} ${p.colorName}`.toLowerCase();
      if (!hay.includes(q)) return false;
    }
    return true;
  });

  const cartBrut = round2(
    state.cart.reduce((s, a) => s + a.sousTotal, 0) +
      state.freeLines.reduce((s, l) => s + l.sousTotal, 0)
  );
  const totalM2 = round2(state.cart.reduce((s, a) => s + a.m2Total, 0));
  const totalCtn = state.cart.reduce((s, a) => s + a.cartons, 0);

  return `
    <div class="pos-layout">
      <div style="display:flex;flex-direction:column;gap:12px">
        <div class="card">
          <div class="filter-bar">
            <input id="pos-search-input" class="input" style="flex:1;min-width:200px" placeholder="Rechercher un modèle, usine, format (ex: 60/60 BATI)..." value="${esc(state.posSearch)}" />
            <select id="pos-usine-select" class="select" style="width:150px">
              ${usines.map((u) => `<option value="${esc(u)}" ${u === state.posUsine ? "selected" : ""}>Usine: ${esc(u)}</option>`).join("")}
            </select>
            <select id="pos-dim-select" class="select" style="width:140px">
              ${dims.map((d) => `<option value="${esc(d)}" ${d === state.posDimension ? "selected" : ""}>Format: ${esc(d)}</option>`).join("")}
            </select>
            <label style="display:inline-flex;align-items:center;gap:6px;font-weight:700;font-size:12.5px;cursor:pointer">
              <input type="checkbox" id="pos-stock-only" ${state.posInStockOnly ? "checked" : ""} /> En stock (&gt;0)
            </label>
          </div>
        </div>

        <div class="product-grid">
          ${
            filtered.length === 0
              ? `<div class="card" style="grid-column:1/-1;text-align:center;color:var(--ink-muted);padding:32px">Aucun produit trouvé.</div>`
              : filtered
                  .slice(0, 120)
                  .map(
                    (p) => `
              <button class="product-card" data-open-calc="${esc(p.id)}">
                <div class="swatch-row">
                  <div style="display:flex;align-items:center;gap:6px">
                    <span class="swatch" style="background:${esc(p.color || "#94a3b8")}"></span>
                    <span class="chip chip-neutral">${esc(p.usine || "N/A")}</span>
                  </div>
                  <span class="chip ${p.stock <= 0 ? "chip-bad" : p.stock <= 5 ? "chip-warn" : "chip-ok"} mono">
                    ${p.stock} ctn
                  </span>
                </div>
                <div class="product-name">${esc(p.name)}</div>
                <div class="product-meta">${esc(p.dimension)} • ${formatM2(p.m2Carton)} m²/ctn • ${p.pcs} pcs</div>
                <div style="display:flex;justify-content:space-between;align-items:baseline;margin-top:auto">
                  <span class="product-price mono">${formatDa(p.prix)}<span style="font-size:11px;font-weight:600;color:var(--ink-muted)">/m²</span></span>
                  <span style="font-size:11.5px;color:var(--ink-secondary)">${esc(p.choix)}</span>
                </div>
              </button>`
                  )
                  .join("")
          }
        </div>
      </div>

      <aside class="cart-panel">
        <div class="card-header" style="margin-bottom:0">
          <div>
            <h2 class="card-title">${state.editingInvoice ? `Modification #${esc(state.editingInvoice.reference)}` : "Panier Vente"}</h2>
            <div style="font-size:12px;color:var(--ink-muted)">${totalCtn} cartons • ${formatM2(totalM2)} m²</div>
          </div>
          <div style="display:flex;gap:6px">
            <button id="open-free-line-btn" class="btn btn-secondary btn-sm">+ Divers</button>
            ${
              state.cart.length || state.freeLines.length || state.editingInvoice
                ? `<button id="clear-cart-btn" class="btn btn-danger btn-sm">Vider</button>`
                : ""
            }
          </div>
        </div>

        <div class="cart-items-list">
          ${
            state.cart.length === 0 && state.freeLines.length === 0
              ? `<div style="text-align:center;padding:28px 10px;color:var(--ink-muted)">Cliquez sur un carrelage à gauche pour calculer les m² et l'ajouter au panier.</div>`
              : ""
          }
          ${state.cart
            .map(
              (c) => `
            <div class="cart-line">
              <div style="display:flex;justify-content:space-between;gap:6px">
                <strong style="font-size:12.5px">${esc(c.name)}</strong>
                <button class="btn btn-danger btn-sm" data-remove-cart="${esc(c.id)}" style="min-height:24px;padding:0 7px">✕</button>
              </div>
              <div style="display:flex;justify-content:space-between;align-items:center;font-size:12px;color:var(--ink-secondary)">
                <span>${c.cartons} ctn${c.pieces ? ` + ${c.pieces} pcs` : ""} (${formatM2(c.m2Total)} m²)</span>
                <strong class="mono" style="color:var(--teal)">${formatDa(c.sousTotal)}</strong>
              </div>
              <div style="display:flex;align-items:center;justify-content:space-between;gap:6px">
                <span style="font-size:11px;color:var(--ink-muted)">${c.bain ? `Bain: ${esc(c.bain)}` : ""} ${c.calibre ? `Cal: ${esc(c.calibre)}` : ""}</span>
                <div style="display:flex;gap:4px">
                  <button class="btn btn-secondary btn-sm" data-cart-delta="${esc(c.id)}:-1">-1</button>
                  <button class="btn btn-secondary btn-sm" data-cart-delta="${esc(c.id)}:+1">+1</button>
                </div>
              </div>
            </div>`
            )
            .join("")}
          ${state.freeLines
            .map(
              (l) => `
            <div class="cart-line">
              <div style="display:flex;justify-content:space-between;gap:6px">
                <strong style="font-size:12.5px">${esc(l.designation)}</strong>
                <button class="btn btn-danger btn-sm" data-remove-free="${esc(l.id)}" style="min-height:24px;padding:0 7px">✕</button>
              </div>
              <div style="display:flex;justify-content:space-between;font-size:12px;color:var(--ink-secondary)">
                <span>${l.quantite} ${esc(l.unite)} × ${formatDa(l.prixUnitaire)}</span>
                <strong class="mono">${formatDa(l.sousTotal)}</strong>
              </div>
            </div>`
            )
            .join("")}
        </div>

        <div style="border-top:1px solid var(--border);padding-top:12px;display:flex;flex-direction:column;gap:10px">
          <div style="display:flex;justify-content:space-between;align-items:baseline">
            <span style="font-weight:700;color:var(--ink-secondary)">Total Brut</span>
            <span class="mono" style="font-size:20px;font-weight:800;color:var(--ink-primary)">${formatDa(cartBrut)}</span>
          </div>
          <button id="open-checkout-btn" class="btn btn-primary" style="width:100%;min-height:44px;font-size:14.5px" ${
            state.cart.length === 0 && state.freeLines.length === 0 ? "disabled" : ""
          }>
            ${state.editingInvoice ? "Enregistrer Modifications" : "Valider &amp; Facturer"}
          </button>
        </div>
      </aside>
    </div>
  `;
}

// --- Tab 2: Stock & Prix ---

function renderStockTab() {
  const perms = getEffectivePermissions();
  const usines = ["Toutes", ...Array.from(new Set(state.products.map((p) => p.usine).filter(Boolean))).sort()];
  const q = state.stockSearch.trim().toLowerCase();

  const filtered = state.products.filter((p) => {
    if (state.stockUsine !== "Toutes" && p.usine !== state.stockUsine) return false;
    if (state.stockOnlyAlerts && p.stock > 5) return false;
    if (q && !`${p.name} ${p.usine} ${p.dimension}`.toLowerCase().includes(q)) return false;
    return true;
  });

  const ruptures = state.products.filter((p) => p.stock <= 0).length;
  const critiques = state.products.filter((p) => p.stock > 0 && p.stock <= 5).length;
  const totalValeurAchat = round2(
    state.products.reduce((s, p) => s + Math.max(0, p.stock) * p.m2Carton * p.prixAchat, 0)
  );
  const totalValeurVente = round2(
    state.products.reduce((s, p) => s + Math.max(0, p.stock) * p.m2Carton * p.prix, 0)
  );

  return `
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">Références Catalogue</div>
        <div class="kpi-value mono">${state.products.length}</div>
        <div class="kpi-sub">Toutes usines</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Ruptures (≤ 0 ctn)</div>
        <div class="kpi-value mono" style="color:var(--rose)">${ruptures}</div>
        <div class="kpi-sub">${critiques} en stock critique (1-5 ctn)</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Valeur Stock Vente</div>
        <div class="kpi-value mono" style="color:var(--teal)">${formatDa(totalValeurVente)}</div>
      </div>
      ${
        perms.canViewPrixAchat
          ? `<div class="kpi-card">
              <div class="kpi-label">Valeur Stock Achat</div>
              <div class="kpi-value mono">${formatDa(totalValeurAchat)}</div>
            </div>`
          : ""
      }
    </div>

    <div class="card">
      <div class="card-header" style="flex-wrap:wrap">
        <div class="filter-bar" style="flex:1">
          <input id="stock-search-input" class="input" style="max-width:300px" placeholder="Rechercher référence stock..." value="${esc(state.stockSearch)}" />
          <select id="stock-usine-select" class="select" style="width:160px">
            ${usines.map((u) => `<option value="${esc(u)}" ${u === state.stockUsine ? "selected" : ""}>Usine: ${esc(u)}</option>`).join("")}
          </select>
          <label style="display:inline-flex;align-items:center;gap:6px;font-weight:700;font-size:12.5px">
            <input type="checkbox" id="stock-alerts-only" ${state.stockOnlyAlerts ? "checked" : ""} /> Alertes ≤ 5 ctn
          </label>
        </div>
        ${
          perms.canModifyStock
            ? `<button id="new-product-btn" class="btn btn-primary">+ Nouvel Article</button>`
            : `<span class="chip chip-neutral">Stock en lecture seule</span>`
        }
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Article</th>
              <th>Usine / Format</th>
              <th>Colisage</th>
              <th>Prix Vente</th>
              ${perms.canViewPrixAchat ? "<th>Prix Achat</th>" : ""}
              <th>Stock</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${filtered
              .slice(0, 200)
              .map(
                (p) => `
              <tr>
                <td>
                  <div style="display:flex;align-items:center;gap:8px">
                    <span class="swatch" style="background:${esc(p.color || "#94a3b8")}"></span>
                    <div>
                      <strong>${esc(p.name)}</strong>
                      <div style="font-size:11.5px;color:var(--ink-muted)">${esc(p.choix)} • ${esc(p.finition)}</div>
                    </div>
                  </div>
                </td>
                <td>${esc(p.usine)}<br><span style="font-size:11.5px;color:var(--ink-muted)">${esc(p.dimension)}</span></td>
                <td class="mono">${formatM2(p.m2Carton)} m²<br><span style="font-size:11.5px;color:var(--ink-muted)">${p.pcs} pcs/ctn</span></td>
                <td class="mono" style="font-weight:800;color:var(--teal)">${formatDa(p.prix)}/m²</td>
                ${perms.canViewPrixAchat ? `<td class="mono">${formatDa(p.prixAchat)}/m²</td>` : ""}
                <td>
                  <span class="chip ${p.stock <= 0 ? "chip-bad" : p.stock <= 5 ? "chip-warn" : "chip-ok"} mono">
                    ${p.stock} ctn (${formatM2(Math.max(0, p.stock) * p.m2Carton)} m²)
                  </span>
                </td>
                <td>
                  <div style="display:flex;gap:5px;flex-wrap:wrap">
                    ${
                      perms.canModifyStock
                        ? `
                      <button class="btn btn-secondary btn-sm" data-stock-delta="${esc(p.id)}:-1">-1</button>
                      <button class="btn btn-secondary btn-sm" data-stock-delta="${esc(p.id)}:+1">+1</button>
                      <button class="btn btn-secondary btn-sm" data-edit-product="${esc(p.id)}">Modifier</button>`
                        : ""
                    }
                    <button class="btn btn-accent btn-sm" data-dispo-product="${esc(p.id)}">Dispo Dépôt</button>
                  </div>
                </td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

// --- Tab 3: Factures & Bons ---

function renderInvoicesTab() {
  const perms = getEffectivePermissions();
  const q = state.invoiceSearch.trim().toLowerCase();
  const filtered = state.invoices.filter((inv) => {
    if (state.invoiceStatusFilter !== "Tous" && inv.statut !== state.invoiceStatusFilter) return false;
    if (q) {
      const hay = `${inv.reference} ${inv.client?.nom} ${inv.vendeur} ${inv.magasin}`.toLowerCase();
      if (!hay.includes(q)) return false;
    }
    return true;
  });

  return `
    <div class="card">
      <div class="card-header" style="flex-wrap:wrap">
        <h2 class="card-title">Historique Factures &amp; Bons (${filtered.length})</h2>
        <div class="filter-bar">
          <input id="inv-search-input" class="input" style="width:260px" placeholder="N° facture, client, vendeur..." value="${esc(state.invoiceSearch)}" />
          ${["Tous", "Payé", "Partiel", "Crédit"]
            .map(
              (st) => `
            <button class="pill-tab ${state.invoiceStatusFilter === st ? "active" : ""}" data-inv-status="${st}">${st}</button>`
            )
            .join("")}
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Référence</th>
              <th>Date</th>
              <th>Client</th>
              <th>Vendeur / Site</th>
              <th>Total Net</th>
              <th>Reste</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${
              filtered.length === 0
                ? `<tr><td colspan="8" style="text-align:center;color:var(--ink-muted);padding:28px">Aucune facture enregistrée.</td></tr>`
                : filtered
                    .map(
                      (inv) => `
                <tr>
                  <td class="mono"><strong>${esc(inv.reference)}</strong></td>
                  <td style="font-size:12px">${esc(formatDateTimeFr(inv.date))}</td>
                  <td><strong>${esc(inv.client?.nom || "Client Comptoir")}</strong><br><span style="font-size:11.5px;color:var(--ink-muted)">${esc(inv.client?.tel || "")}</span></td>
                  <td>${esc(inv.vendeur)}<br><span style="font-size:11.5px;color:var(--ink-muted)">${esc(inv.magasin)}</span></td>
                  <td class="mono"><strong>${formatDa(inv.totalNet)}</strong></td>
                  <td class="mono" style="color:${inv.resteAPayer > 0 ? "var(--rose)" : "var(--ink-muted)"}">${formatDa(inv.resteAPayer)}</td>
                  <td>
                    <span class="chip ${inv.statut === "Payé" ? "chip-ok" : inv.statut === "Partiel" ? "chip-warn" : "chip-bad"}">${esc(inv.statut)}</span>
                  </td>
                  <td>
                    <div style="display:flex;gap:6px">
                      <button class="btn btn-primary btn-sm" data-view-inv="${esc(inv.id)}">Voir / Imprimer</button>
                      ${
                        perms.canEditInvoices
                          ? `<button class="btn btn-secondary btn-sm" data-edit-inv="${esc(inv.id)}">Éditer</button>`
                          : ""
                      }
                      ${
                        perms.canDeleteInvoices
                          ? `<button class="btn btn-danger btn-sm" data-del-inv="${esc(inv.id)}">Suppr.</button>`
                          : ""
                      }
                    </div>
                  </td>
                </tr>`
                    )
                    .join("")
            }
          </tbody>
        </table>
      </div>
    </div>
  `;
}

// --- Tab 4: Statistiques ---

function renderDashboardTab() {
  const perms = getEffectivePermissions();
  const todayPrefix = new Date().toISOString().slice(0, 10);
  const monthPrefix = todayPrefix.slice(0, 7);

  const scopedInvoices = state.invoices.filter((i) => {
    if (state.statsPeriod === "today") return String(i.date).startsWith(todayPrefix);
    if (state.statsPeriod === "month") return String(i.date).startsWith(monthPrefix);
    return true;
  });

  const caNet = round2(scopedInvoices.reduce((s, i) => s + (Number(i.totalNet) || 0), 0));
  const encaisse = round2(scopedInvoices.reduce((s, i) => s + (Number(i.montantPaye) || 0), 0));
  const credits = round2(scopedInvoices.reduce((s, i) => s + (Number(i.resteAPayer) || 0), 0));
  const m2Vendus = round2(
    scopedInvoices.reduce((s, i) => s + (i.articles || []).reduce((a, x) => a + (Number(x.m2Total) || 0), 0), 0)
  );
  const margeEstimee = round2(
    scopedInvoices.reduce(
      (s, i) =>
        s +
        (i.articles || []).reduce(
          (a, x) => a + (Number(x.sousTotal) || 0) - (Number(x.prixAchat) || 0) * (Number(x.m2Total) || 0),
          0
        ) -
        (Number(i.remise) || 0),
      0
    )
  );

  const byStore = {};
  const bySeller = {};
  for (const inv of scopedInvoices) {
    byStore[inv.magasin || "Autre"] = (byStore[inv.magasin || "Autre"] || 0) + (Number(inv.totalNet) || 0);
    bySeller[inv.vendeur || "Vendeur"] = (bySeller[inv.vendeur || "Vendeur"] || 0) + (Number(inv.totalNet) || 0);
  }

  return `
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Tableau de Bord Commercial</h2>
        <div class="filter-bar">
          ${[
            ["today", "Aujourd'hui"],
            ["month", "Ce Mois"],
            ["all", "Tout l'historique"],
          ]
            .map(
              ([id, label]) => `<button class="pill-tab ${state.statsPeriod === id ? "active" : ""}" data-stats-period="${id}">${label}</button>`
            )
            .join("")}
        </div>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">Chiffre d'Affaires Net</div>
          <div class="kpi-value mono" style="color:var(--teal)">${formatDa(caNet)}</div>
          <div class="kpi-sub">${scopedInvoices.length} factures • ${formatM2(m2Vendus)} m²</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Montant Encaissé</div>
          <div class="kpi-value mono" style="color:var(--jade)">${formatDa(encaisse)}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Crédits Accordés</div>
          <div class="kpi-value mono" style="color:var(--rose)">${formatDa(credits)}</div>
        </div>
        ${
          perms.canViewPrixAchat
            ? `<div class="kpi-card">
                <div class="kpi-label">Marge Brute Estimée</div>
                <div class="kpi-value mono">${formatDa(margeEstimee)}</div>
              </div>`
            : ""
        }
      </div>
    </div>

    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:16px">
      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px">Ventes par Site / Magasin</h3>
        ${
          Object.keys(byStore).length === 0
            ? `<div style="color:var(--ink-muted)">Aucune donnée sur la période.</div>`
            : Object.entries(byStore)
                .map(
                  ([site, val]) => `
              <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--border)">
                <strong>${esc(site)}</strong>
                <span class="mono">${formatDa(val)}</span>
              </div>`
                )
                .join("")
        }
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px">Performance par Vendeur</h3>
        ${
          Object.keys(bySeller).length === 0
            ? `<div style="color:var(--ink-muted)">Aucune donnée sur la période.</div>`
            : Object.entries(bySeller)
                .sort((a, b) => b[1] - a[1])
                .map(
                  ([name, val]) => `
              <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--border)">
                <strong>${esc(name)}</strong>
                <span class="mono">${formatDa(val)}</span>
              </div>`
                )
                .join("")
        }
      </div>
    </div>
  `;
}

// --- Tab 5: Clients & Crédits ---

function renderCustomersTab() {
  const q = state.customerSearch.trim().toLowerCase();
  const list = state.customers.filter((c) => !q || `${c.nom} ${c.tel} ${c.adresse}`.toLowerCase().includes(q));
  const totalCreances = round2(state.customers.reduce((s, c) => s + (Number(c.dette) || 0), 0));

  return `
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">Clients Enregistrés</div>
        <div class="kpi-value mono">${state.customers.length}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Total Créances Clients</div>
        <div class="kpi-value mono" style="color:var(--rose)">${formatDa(totalCreances)}</div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <input id="cust-search-input" class="input" style="max-width:320px" placeholder="Rechercher client..." value="${esc(state.customerSearch)}" />
        <button id="new-customer-btn" class="btn btn-primary">+ Nouveau Client</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Client</th>
              <th>Téléphone</th>
              <th>Adresse</th>
              <th>Total Achats</th>
              <th>Dette / Crédit</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${list
              .map(
                (c) => `
              <tr>
                <td><strong>${esc(c.nom)}</strong></td>
                <td class="mono">${esc(c.tel)}</td>
                <td>${esc(c.adresse)}</td>
                <td class="mono">${formatDa(c.totalAchats)}</td>
                <td class="mono" style="font-weight:800;color:${c.dette > 0 ? "var(--rose)" : "var(--jade)"}">${formatDa(c.dette)}</td>
                <td>
                  <div style="display:flex;gap:6px">
                    ${c.dette > 0 ? `<button class="btn btn-primary btn-sm" data-pay-debt="${esc(c.id)}">Encaisser</button>` : ""}
                    <button class="btn btn-secondary btn-sm" data-edit-cust="${esc(c.id)}">Modifier</button>
                  </div>
                </td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

// --- Tab 6: Usines & Fournisseurs ---

function renderSuppliersTab() {
  const totalDette = round2(state.suppliers.reduce((s, f) => s + (Number(f.soldeDette) || 0), 0));
  const selected = state.suppliers.find((s) => s.id === state.selectedSupplierId) || state.suppliers[0] || null;
  const movs = selected ? state.supplierMovements.filter((m) => m.supplierId === selected.id) : [];

  return `
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">Usines / Fournisseurs</div>
        <div class="kpi-value mono">${state.suppliers.length}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Total Dettes Usines</div>
        <div class="kpi-value mono" style="color:var(--terracotta)">${formatDa(totalDette)}</div>
      </div>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">Usines &amp; Fournisseurs</h2>
          <button id="new-supplier-btn" class="btn btn-primary btn-sm">+ Nouveau</button>
        </div>
        ${state.suppliers
          .map(
            (s) => `
          <div style="padding:12px;border:1px solid ${selected?.id === s.id ? "var(--teal)" : "var(--border)"};background:${selected?.id === s.id ? "var(--teal-soft)" : "var(--bg-canvas)"};border-radius:10px;margin-bottom:8px;cursor:pointer" data-select-sup="${esc(s.id)}">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <strong>${esc(s.nom)}</strong>
              <span class="chip chip-neutral">${esc(s.usineRepresentee)}</span>
            </div>
            <div style="font-size:12px;color:var(--ink-secondary);margin-top:4px">${esc(s.contact)} • ${esc(s.tel)}</div>
            <div style="display:flex;justify-content:space-between;margin-top:8px">
              <span class="mono" style="font-weight:800;color:var(--terracotta)">Dette: ${formatDa(s.soldeDette)}</span>
              <span class="mono" style="font-size:12px">Achats: ${formatDa(s.totalAchats)}</span>
            </div>
          </div>`
          )
          .join("")}
      </div>

      <div class="card">
        ${
          selected
            ? `
          <div class="card-header">
            <div>
              <h2 class="card-title">${esc(selected.nom)}</h2>
              <div style="font-size:12px;color:var(--ink-muted)">Solde dû : <strong class="mono">${formatDa(selected.soldeDette)}</strong></div>
            </div>
            <div style="display:flex;gap:6px">
              <button class="btn btn-primary btn-sm" data-sup-mov="versement:${esc(selected.id)}">Versement</button>
              <button class="btn btn-accent btn-sm" data-sup-mov="achat:${esc(selected.id)}">+ Facture Usine</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Date</th><th>Type</th><th>Motif / Réf</th><th>Montant</th></tr></thead>
              <tbody>
                ${
                  movs.length === 0
                    ? `<tr><td colspan="4" style="text-align:center;color:var(--ink-muted)">Aucun mouvement enregistré.</td></tr>`
                    : movs
                        .map(
                          (m) => `
                    <tr>
                      <td style="font-size:12px">${esc(formatDateTimeFr(m.date))}</td>
                      <td><span class="chip ${m.type === "versement" ? "chip-ok" : "chip-warn"}">${m.type === "versement" ? "Versement" : "Achat"}</span></td>
                      <td>${esc(m.motif)} ${m.pieceJointeRef ? `<br><span class="mono" style="font-size:11px">${esc(m.pieceJointeRef)}</span>` : ""}</td>
                      <td class="mono"><strong>${formatDa(m.montant)}</strong></td>
                    </tr>`
                        )
                        .join("")
                }
              </tbody>
            </table>
          </div>`
            : `<div style="color:var(--ink-muted)">Sélectionnez un fournisseur</div>`
        }
      </div>
    </div>
  `;
}

// --- Tab 7: Caisse & Clôture ---

function renderCaisseTab() {
  const entrees = round2(state.caisseMovements.filter((m) => m.type === "entree").reduce((s, m) => s + m.montant, 0));
  const sorties = round2(state.caisseMovements.filter((m) => m.type === "sortie").reduce((s, m) => s + m.montant, 0));
  const solde = round2(entrees - sorties);

  return `
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">Solde Caisse Actuel</div>
        <div class="kpi-value mono" style="color:var(--teal)">${formatDa(solde)}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Total Entrées</div>
        <div class="kpi-value mono" style="color:var(--jade)">${formatDa(entrees)}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Total Sorties / Dépenses</div>
        <div class="kpi-value mono" style="color:var(--rose)">${formatDa(sorties)}</div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Journal des Mouvements de Caisse</h2>
        <div style="display:flex;gap:8px">
          <button id="add-caisse-entree-btn" class="btn btn-primary btn-sm">+ Entrée</button>
          <button id="add-caisse-sortie-btn" class="btn btn-danger btn-sm">- Dépense / Sortie</button>
          <button id="open-daily-count-btn" class="btn btn-secondary btn-sm">Pointage Fin de Journée</button>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead><tr><th>Date</th><th>Type</th><th>Motif</th><th>Auteur</th><th>Montant</th></tr></thead>
          <tbody>
            ${state.caisseMovements
              .map(
                (m) => `
              <tr>
                <td style="font-size:12px">${esc(formatDateTimeFr(m.date))}</td>
                <td><span class="chip ${m.type === "entree" ? "chip-ok" : "chip-bad"}">${m.type === "entree" ? "Entrée" : "Sortie"}</span></td>
                <td>${esc(m.motif)} ${m.referenceFacture ? `<span class="mono">(${esc(m.referenceFacture)})</span>` : ""}</td>
                <td>${esc(m.auteur)}</td>
                <td class="mono" style="font-weight:800;color:${m.type === "entree" ? "var(--jade)" : "var(--rose)"}">${m.type === "entree" ? "+" : "-"}${formatDa(m.montant)}</td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>
    </div>

    ${
      state.dailyCaisseCounts.length > 0
        ? `
      <div class="card">
        <h3 class="card-title" style="margin-bottom:10px">Historique Clôtures Journalières</h3>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Date</th><th>Site</th><th>Auteur</th><th>Théorique</th><th>Compté Réel</th><th>Écart</th></tr></thead>
            <tbody>
              ${state.dailyCaisseCounts
                .map(
                  (c) => `
                <tr>
                  <td style="font-size:12px">${esc(formatDateTimeFr(c.date))}</td>
                  <td>${esc(c.magasin)}</td>
                  <td>${esc(c.auteur)}</td>
                  <td class="mono">${formatDa(c.theorique)}</td>
                  <td class="mono">${formatDa(c.compteReel)}</td>
                  <td class="mono" style="font-weight:800;color:${c.ecart >= 0 ? "var(--jade)" : "var(--rose)"}">${formatDa(c.ecart)}</td>
                </tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>
      </div>`
        : ""
    }
  `;
}

// --- Tab 8: Équipe & Droits ---

function renderUsersTab() {
  return `
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Membres de l'Équipe (${state.staffUsers.length})</h2>
        <button id="new-user-btn" class="btn btn-primary">+ Nouvel Utilisateur</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Nom</th><th>Rôle</th><th>Site</th><th>PIN</th><th>Droits Clés</th><th>Actions</th></tr></thead>
          <tbody>
            ${state.staffUsers
              .map(
                (u) => `
              <tr>
                <td><strong>${esc(u.name)}</strong><br><span style="font-size:11.5px;color:var(--ink-muted)">${esc(u.phone || "")}</span></td>
                <td><span class="chip chip-teal">${esc(u.role.toUpperCase())}</span></td>
                <td>${esc(u.magasin)}</td>
                <td class="mono">••••</td>
                <td style="font-size:11.5px;color:var(--ink-secondary)">
                  ${u.canModifyStock ? "Stock ✓ " : "Stock 🔒 "}
                  ${u.canModifyPrices ? "Prix ✓ " : "Prix 🔒 "}
                  ${u.canViewPrixAchat ? "Achat ✓" : "Achat 🔒"}
                </td>
                <td>
                  <div style="display:flex;gap:6px">
                    <button class="btn btn-secondary btn-sm" data-edit-user="${esc(u.id)}">Modifier</button>
                    ${u.role !== "admin" ? `<button class="btn btn-danger btn-sm" data-del-user="${esc(u.id)}">Suppr.</button>` : ""}
                  </div>
                </td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Politiques Globales par Rôle</h2>
      </div>
      <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:12px">
        ${state.rolePolicies
          .map(
            (p) => `
          <div style="padding:14px;border:1px solid var(--border);border-radius:12px;background:var(--bg-canvas)">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <strong>${esc(p.displayName)}</strong>
              <span class="chip chip-neutral mono">${esc(p.roleId)}</span>
            </div>
            <p style="font-size:12px;color:var(--ink-secondary)">${esc(p.description)}</p>
            <div style="font-size:12px;color:var(--ink-primary);display:flex;flex-direction:column;gap:3px;margin-bottom:10px">
              <span>• Prix d'achat visible : <strong>${p.canViewPrixAchat ? "Oui" : "Non"}</strong></span>
              <span>• Modifier tarifs / remises : <strong>${p.canModifyPrices ? "Oui" : "Non"}</strong></span>
              <span>• Modifier stock : <strong>${p.canModifyStock ? "Oui" : "Non"}</strong></span>
              <span>• Supprimer factures : <strong>${p.canDeleteInvoices ? "Oui" : "Non"}</strong></span>
            </div>
            ${
              p.roleId !== "admin"
                ? `<button class="btn btn-secondary btn-sm" data-edit-policy="${esc(p.roleId)}">Configurer Rôle</button>`
                : `<span style="font-size:11.5px;color:var(--ink-muted)">Admin protégé</span>`
            }
          </div>`
          )
          .join("")}
      </div>
    </div>
  `;
}

// --- Tab 9: Réglages ---

function renderSettingsTab() {
  const s = state.settings;
  const info = state.storeInfo;
  return `
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Informations Magasin &amp; Facture</h2>
        <button id="save-store-info-btn" class="btn btn-primary">Enregistrer Magasin</button>
      </div>
      <div class="form-grid">
        <div><label class="field-label">Nom Commercial</label><input id="si-nom" class="input" value="${esc(info.nom)}" /></div>
        <div><label class="field-label">Nom Arabe</label><input id="si-arabe" class="input" value="${esc(info.nomArabe)}" /></div>
        <div><label class="field-label">Téléphone Principal (WhatsApp)</label><input id="si-tel" class="input" value="${esc(info.tel)}" /></div>
        <div><label class="field-label">Téléphone Dépôt</label><input id="si-tel2" class="input" value="${esc(info.tel2)}" /></div>
        <div><label class="field-label">Adresse</label><input id="si-adresse" class="input" value="${esc(info.adresse)}" /></div>
        <div><label class="field-label">RC</label><input id="si-rc" class="input mono" value="${esc(info.rc)}" /></div>
        <div><label class="field-label">NIF</label><input id="si-nif" class="input mono" value="${esc(info.nif)}" /></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Préférences Poste &amp; Impression</h2>
        <button id="save-settings-btn" class="btn btn-primary">Enregistrer Préférences</button>
      </div>
      <div class="form-grid">
        <div>
          <label class="field-label">Format Impression</label>
          <select id="st-print" class="select">
            <option value="A4" ${s.printFormat === "A4" ? "selected" : ""}>A4 Standard</option>
            <option value="A5" ${s.printFormat === "A5" ? "selected" : ""}>A5 Compact</option>
          </select>
        </div>
        <div>
          <label class="field-label">Site Actif par Défaut</label>
          <select id="st-store" class="select">
            ${SITES.map((site) => `<option value="${esc(site)}" ${s.activeStore === site ? "selected" : ""}>${esc(site)}</option>`).join("")}
          </select>
        </div>
        <div>
          <label class="field-label">Nom Vendeur sur ce PC (optionnel)</label>
          <input id="st-seller-name" class="input" value="${esc(s.deviceSellerName)}" />
        </div>
        <div>
          <label class="field-label">Tél Vendeur sur ce PC</label>
          <input id="st-seller-phone" class="input" value="${esc(s.deviceSellerPhone)}" />
        </div>
      </div>
      <div style="display:flex;gap:18px;flex-wrap:wrap;margin-top:14px">
        <label style="display:flex;align-items:center;gap:8px;font-weight:700">
          <input type="checkbox" id="st-sound" ${s.enableSoundAlerts ? "checked" : ""} /> Sons d'alerte Dépôt
        </label>
        <label style="display:flex;align-items:center;gap:8px;font-weight:700">
          <input type="checkbox" id="st-hide-bain" ${s.hideBainTeinteFacture ? "checked" : ""} /> Masquer Bain/Calibre à l'impression
        </label>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title" style="margin-bottom:10px">Outils Cloud &amp; Sauvegarde PC</h2>
      <div style="display:flex;gap:10px;flex-wrap:wrap">
        <button id="req-browser-notif-btn" class="btn btn-secondary">Activer Notifications Bureau Windows</button>
        <button id="export-json-btn" class="btn btn-secondary">Exporter Sauvegarde JSON</button>
        <button id="reset-fb-config-btn" class="btn btn-danger">Changer Configuration Firebase Web</button>
      </div>
    </div>
  `;
}

// --- Tab 10: Calculateur m² ---

function renderCalculatorTab() {
  const c = state.calcState;
  const plan = TileCalculatorMath.calculateRoomTilePlan(
    c.roomL,
    c.roomW,
    c.tileL,
    c.tileW,
    c.tileUnit,
    c.m2Carton,
    c.marginPct
  );
  const linked = state.products.find((p) => p.id === c.linkedProductId) || null;
  const estTotal = linked ? round2(plan.deliveredAreaM2 * linked.prix) : 0;

  return `
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Calculateur Surface &amp; Nombre de Cartons</h2>
      </div>

      <div class="form-grid">
        <div><label class="field-label">Longueur pièce (m)</label><input id="calc-rl" type="number" step="0.01" class="input mono" value="${c.roomL}" /></div>
        <div><label class="field-label">Largeur pièce (m)</label><input id="calc-rw" type="number" step="0.01" class="input mono" value="${c.roomW}" /></div>
        <div><label class="field-label">Format carreau Longueur (cm)</label><input id="calc-tl" type="number" step="1" class="input mono" value="${c.tileL}" /></div>
        <div><label class="field-label">Format carreau Largeur (cm)</label><input id="calc-tw" type="number" step="1" class="input mono" value="${c.tileW}" /></div>
        <div><label class="field-label">m² par Carton</label><input id="calc-m2c" type="number" step="0.01" class="input mono" value="${c.m2Carton}" /></div>
        <div>
          <label class="field-label">Marge de chute (%)</label>
          <select id="calc-margin" class="select">
            ${TileCalculatorMath.MARGIN_PRESETS.map((m) => `<option value="${m}" ${m === c.marginPct ? "selected" : ""}>+${m}%</option>`).join("")}
          </select>
        </div>
      </div>

      <div style="margin-top:14px">
        <label class="field-label">Lier à un produit du catalogue (optionnel pour ajout rapide au panier)</label>
        <select id="calc-product-link" class="select">
          <option value="">-- Calcul libre sans produit --</option>
          ${state.products
            .slice(0, 150)
            .map(
              (p) =>
                `<option value="${esc(p.id)}" ${p.id === c.linkedProductId ? "selected" : ""}>${esc(p.name)} (${formatM2(p.m2Carton)} m²/ctn • ${formatDa(p.prix)}/m²)</option>`
            )
            .join("")}
        </select>
      </div>

      <div class="kpi-grid" style="margin-top:16px">
        <div class="kpi-card">
          <div class="kpi-label">Surface Nette</div>
          <div class="kpi-value mono">${formatM2(plan.totalAreaM2)} m²</div>
          <div class="kpi-sub">Avec +${plan.marginPercent}% : ${formatM2(plan.targetAreaWithMarginM2)} m²</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Cartons Nécessaires</div>
          <div class="kpi-value mono" style="color:var(--teal)">${plan.cartonsNeeded} cartons</div>
          <div class="kpi-sub">${plan.tilesNeeded} carreaux estimés</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Surface Livrée</div>
          <div class="kpi-value mono">${formatM2(plan.deliveredAreaM2)} m²</div>
          <div class="kpi-sub">Surplus: ${formatM2(plan.surplusAreaM2)} m²</div>
        </div>
        ${
          linked
            ? `<div class="kpi-card">
                <div class="kpi-label">Montant Estimé</div>
                <div class="kpi-value mono" style="color:var(--terracotta)">${formatDa(estTotal)}</div>
                <button id="calc-add-cart-btn" class="btn btn-primary btn-sm" style="margin-top:8px">Ajouter ${plan.cartonsNeeded} ctn au panier</button>
              </div>`
            : ""
        }
      </div>
    </div>
  `;
}

// --- Modals & Drawers ---

function renderModal() {
  const mRoot = document.getElementById("modal-root");
  if (!mRoot) return;
  const m = state.modal;
  if (!m) {
    mRoot.innerHTML = "";
    return;
  }

  let html = "";
  const perms = getEffectivePermissions();

  if (m.type === "tile_calc") {
    const p = m.product;
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header">
            <div>
              <h2 class="card-title">${esc(p.name)}</h2>
              <div style="font-size:12px;color:var(--ink-muted)">${esc(p.usine)} • ${esc(p.dimension)} • ${formatM2(p.m2Carton)} m²/ctn • ${p.pcs} pcs/ctn • Stock: ${p.stock} ctn</div>
            </div>
            <button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button>
          </div>

          <div class="form-grid" style="margin-bottom:12px">
            <div>
              <label class="field-label">Surface souhaitée (m²) — optionnel</label>
              <input id="mc-surface" type="number" step="0.01" class="input mono" placeholder="Ex: 24.5" />
            </div>
            <div>
              <label class="field-label">Marge perte (%)</label>
              <select id="mc-margin" class="select">
                ${[0, 5, 10, 15].map((pct) => `<option value="${pct}" ${pct === 0 ? "selected" : ""}>+${pct}%</option>`).join("")}
              </select>
            </div>
            <div>
              <label class="field-label">Cartons</label>
              <input id="mc-cartons" type="number" min="0" class="input mono" value="1" />
            </div>
            <div>
              <label class="field-label">Pièces détachées</label>
              <input id="mc-pieces" type="number" min="0" class="input mono" value="0" />
            </div>
            <div>
              <label class="field-label">Prix / m² (DA) ${perms.canModifyPrices ? "" : "🔒"}</label>
              <input id="mc-prix" type="number" step="1" class="input mono" value="${p.prix}" ${perms.canModifyPrices ? "" : "disabled"} />
            </div>
            <div>
              <label class="field-label">Bain / Calibre</label>
              <div style="display:flex;gap:6px">
                <input id="mc-bain" class="input" placeholder="Bain" />
                <input id="mc-calibre" class="input" placeholder="Calibre" />
              </div>
            </div>
          </div>

          <div id="mc-summary" class="kpi-card" style="background:var(--bg-canvas);margin-bottom:14px"></div>

          <div style="display:flex;gap:10px;justify-content:flex-end">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="mc-confirm-btn" class="btn btn-primary">Ajouter au Panier</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "free_line") {
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header">
            <h2 class="card-title">Ajouter Article Divers / Service</h2>
            <button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button>
          </div>
          <div style="display:flex;flex-wrap:wrap;gap:6px;margin-bottom:14px">
            ${DIVERS_PRESETS.map(
              (pr, idx) => `<button class="pill-tab" data-preset-idx="${idx}">${esc(pr.designation)} (${formatDa(pr.prix)})</button>`
            ).join("")}
          </div>
          <div class="form-grid">
            <div style="grid-column:1/-1"><label class="field-label">Désignation</label><input id="fl-desig" class="input" placeholder="Ex: Colle C1 25kg" /></div>
            <div><label class="field-label">Quantité</label><input id="fl-qty" type="number" step="0.1" class="input mono" value="1" /></div>
            <div><label class="field-label">Unité</label><input id="fl-unite" class="input" value="sac" /></div>
            <div><label class="field-label">Prix Unitaire (DA)</label><input id="fl-prix" type="number" class="input mono" value="750" /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="fl-confirm-btn" class="btn btn-primary">Ajouter Ligne</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "checkout") {
    const brut = round2(
      state.cart.reduce((s, a) => s + a.sousTotal, 0) + state.freeLines.reduce((s, l) => s + l.sousTotal, 0)
    );
    const ed = state.editingInvoice;
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header">
            <h2 class="card-title">${ed ? `Enregistrer Modification #${esc(ed.reference)}` : "Finaliser la Vente"}</h2>
            <button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button>
          </div>
          <div class="form-grid">
            <div style="grid-column:1/-1">
              <label class="field-label">Client Enregistré</label>
              <select id="co-client-select" class="select">
                <option value="">-- Client Comptoir / Saisie libre --</option>
                ${state.customers
                  .map(
                    (c) => `<option value="${esc(c.id)}" ${ed?.client?.id === c.id ? "selected" : ""}>${esc(c.nom)} (${esc(c.tel)}) — Dette: ${formatDa(c.dette)}</option>`
                  )
                  .join("")}
              </select>
            </div>
            <div><label class="field-label">Nom Client</label><input id="co-client-nom" class="input" value="${esc(ed?.client?.nom || "Client Comptoir")}" /></div>
            <div><label class="field-label">Téléphone</label><input id="co-client-tel" class="input" value="${esc(ed?.client?.tel || "")}" /></div>
            <div>
              <label class="field-label">Mode de Paiement</label>
              <select id="co-mode" class="select">
                ${["Espèces", "Partiel", "Crédit"].map((mp) => `<option value="${mp}" ${ed?.modePaiement === mp ? "selected" : ""}>${mp}</option>`).join("")}
              </select>
            </div>
            <div>
              <label class="field-label">Remise (DA) ${perms.canModifyPrices ? "" : "🔒"}</label>
              <input id="co-remise" type="number" min="0" class="input mono" value="${ed?.remise ?? 0}" ${perms.canModifyPrices ? "" : "disabled"} />
            </div>
            <div>
              <label class="field-label">Montant Payé (DA)</label>
              <input id="co-paye" type="number" min="0" class="input mono" value="${ed ? ed.montantPaye : brut}" />
            </div>
            <div>
              <label class="field-label">Total Net</label>
              <div id="co-net-display" class="mono" style="font-size:20px;font-weight:800;padding-top:6px;color:var(--teal)">${formatDa(brut)}</div>
            </div>
            <div style="grid-column:1/-1">
              <label class="field-label">Notes / Observation livraison</label>
              <input id="co-notes" class="input" value="${esc(ed?.notes || "")}" />
            </div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="co-submit-btn" class="btn btn-primary">Valider Facture &amp; Synchroniser</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "invoice_preview") {
    const inv = state.invoices.find((i) => i.id === m.invoiceId) || m.snapshot;
    if (!inv) {
      state.modal = null;
      mRoot.innerHTML = "";
      return;
    }
    const store = state.storeInfo;
    const hidePrices = Boolean(inv.hidePrices);
    const hideBain = Boolean(state.settings.hideBainTeinteFacture);

    const waSent = Boolean(inv.whatsappSentBeforePrint);

    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box wide">
          <div class="card-header no-print">
            <h2 class="card-title">Facture #${esc(inv.reference)}</h2>
            <div style="display:flex;gap:8px;flex-wrap:wrap">
              ${
                perms.canHideInvoicePrices
                  ? `<button id="inv-toggle-prices-btn" class="btn btn-secondary btn-sm">${hidePrices ? "Afficher les Prix" : "Masquer Prix (Bon Livraison)"}</button>`
                  : ""
              }
              <button id="inv-png-btn" class="btn btn-secondary btn-sm">🖼️ Photo PNG</button>
              <button id="inv-wa-btn" class="btn btn-accent btn-sm">📸 Photo WhatsApp (0676754124)</button>
              <button id="inv-print-btn" class="btn btn-primary btn-sm" ${waSent ? "" : "disabled"}>${waSent ? `🖨️ Imprimer (${esc(state.settings.printFormat)})` : "🔒 Photo WA requise avant impression"}</button>
              <button id="modal-close-btn" class="btn btn-secondary btn-sm">Fermer</button>
            </div>
          </div>
          <div class="alert-banner ${waSent ? "info" : "warn"} no-print" style="margin-bottom:12px">
            <span>${
              waSent
                ? "✓ Photo envoyée sur WhatsApp Bureau — Impression déverrouillée."
                : "Cliquez sur « 📸 Photo WhatsApp » pour copier/envoyer la photo de la facture au Bureau avant impression (Ctrl+V dans WhatsApp Web)."
            }</span>
          </div>

          <div class="invoice-sheet">
            <div class="invoice-sheet-header">
              <div>
                <h1 style="margin:0;font-size:22px;color:#142824">${esc(store.nom)}</h1>
                ${store.nomArabe ? `<div style="font-size:13px;color:#4D5E59">${esc(store.nomArabe)}</div>` : ""}
                <div style="font-size:12px;color:#4D5E59;margin-top:4px">${esc(store.adresse)}</div>
                <div style="font-size:12px" class="mono">Tél: ${esc(store.tel)} ${store.tel2 ? `/ ${esc(store.tel2)}` : ""}</div>
              </div>
              <div style="text-align:right">
                <div style="font-size:17px;font-weight:800" class="mono">${hidePrices ? "BON DE LIVRAISON" : "FACTURE"} #${esc(inv.reference)}</div>
                <div style="font-size:12px">Date : ${esc(formatDateTimeFr(inv.date))}</div>
                <div style="font-size:12px">Site : <strong>${esc(inv.magasin)}</strong> • Vendeur : ${esc(inv.vendeur)}</div>
              </div>
            </div>

            <div style="background:#F5F2EC;padding:10px 14px;border-radius:8px;margin-bottom:14px;display:flex;justify-content:space-between;flex-wrap:wrap;gap:8px">
              <div><strong>Client :</strong> ${esc(inv.client?.nom || "Client Comptoir")} ${inv.client?.tel ? `(${esc(inv.client.tel)})` : ""}</div>
              <div><strong>Statut :</strong> ${esc(inv.statut)} (${esc(inv.modePaiement)})</div>
            </div>

            <table>
              <thead>
                <tr>
                  <th>Désignation</th>
                  <th>Cartons / Pcs</th>
                  <th>Surface</th>
                  ${hidePrices ? "" : "<th>P.U</th><th>Sous-Total</th>"}
                </tr>
              </thead>
              <tbody>
                ${(inv.articles || [])
                  .map(
                    (a) => `
                  <tr>
                    <td>
                      <strong>${esc(a.name)}</strong>
                      <div style="font-size:11px;color:#6b7280">${esc(a.usine)} • ${esc(a.dimension)} ${!hideBain && (a.bain || a.calibre) ? `• Bain ${esc(a.bain)} Cal ${esc(a.calibre)}` : ""}</div>
                    </td>
                    <td class="mono">${a.cartons} ctn${a.pieces ? ` + ${a.pieces} pcs` : ""}</td>
                    <td class="mono">${formatM2(a.m2Total)} m²</td>
                    ${hidePrices ? "" : `<td class="mono">${formatDa(a.prixUnitaire)}</td><td class="mono"><strong>${formatDa(a.sousTotal)}</strong></td>`}
                  </tr>`
                  )
                  .join("")}
                ${(inv.lignesLibres || [])
                  .map(
                    (l) => `
                  <tr>
                    <td><strong>${esc(l.designation)}</strong></td>
                    <td class="mono">${l.quantite} ${esc(l.unite)}</td>
                    <td>—</td>
                    ${hidePrices ? "" : `<td class="mono">${formatDa(l.prixUnitaire)}</td><td class="mono"><strong>${formatDa(l.sousTotal)}</strong></td>`}
                  </tr>`
                  )
                  .join("")}
              </tbody>
            </table>

            ${
              hidePrices
                ? `<p style="margin-top:16px;font-size:12px;color:#4D5E59">Bon de livraison sans tarification. Merci de votre confiance.</p>`
                : `
              <div style="margin-top:16px;display:flex;justify-content:flex-end">
                <div style="min-width:260px;display:flex;flex-direction:column;gap:5px">
                  <div style="display:flex;justify-content:space-between"><span>Total Brut :</span><strong class="mono">${formatDa(inv.totalBrut)}</strong></div>
                  ${inv.remise > 0 ? `<div style="display:flex;justify-content:space-between"><span>Remise :</span><strong class="mono">-${formatDa(inv.remise)}</strong></div>` : ""}
                  <div style="display:flex;justify-content:space-between;font-size:17px;border-top:2px solid #142824;padding-top:6px"><span>TOTAL NET :</span><strong class="mono">${formatDa(inv.totalNet)}</strong></div>
                  <div style="display:flex;justify-content:space-between"><span>Payé :</span><span class="mono">${formatDa(inv.montantPaye)}</span></div>
                  ${inv.resteAPayer > 0 ? `<div style="display:flex;justify-content:space-between;color:#DC2626"><span>Reste à payer :</span><strong class="mono">${formatDa(inv.resteAPayer)}</strong></div>` : ""}
                </div>
              </div>`
            }
          </div>
        </div>
      </div>`;
  } else if (m.type === "product_edit") {
    const p = m.product || {
      id: makeId("p"),
      name: "",
      usine: "BATI",
      dimension: "60×60",
      m2Carton: 1.44,
      pcs: 4,
      prix: 1200,
      prixAchat: 900,
      stock: 0,
      color: "#94a3b8",
      colorName: "Gris Minéral",
      finition: "brillant",
      choix: "1er Choix",
      categorie: "sol",
      description: "",
      pricingUnit: "carton",
      prixPiece: 0,
    };
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header">
            <h2 class="card-title">${m.product ? "Modifier Produit" : "Nouvel Article"}</h2>
            <button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button>
          </div>
          <div class="form-grid">
            <div style="grid-column:1/-1"><label class="field-label">Nom / Référence</label><input id="pe-name" class="input" value="${esc(p.name)}" /></div>
            <div><label class="field-label">Usine</label><input id="pe-usine" class="input" value="${esc(p.usine)}" /></div>
            <div><label class="field-label">Dimension</label><input id="pe-dim" class="input" value="${esc(p.dimension)}" /></div>
            <div><label class="field-label">m² / Carton</label><input id="pe-m2" type="number" step="0.01" class="input mono" value="${p.m2Carton}" /></div>
            <div><label class="field-label">Pièces / Carton</label><input id="pe-pcs" type="number" class="input mono" value="${p.pcs}" /></div>
            <div><label class="field-label">Prix Vente / m²</label><input id="pe-prix" type="number" class="input mono" value="${p.prix}" /></div>
            <div><label class="field-label">Prix Achat / m²</label><input id="pe-achat" type="number" class="input mono" value="${p.prixAchat}" /></div>
            <div><label class="field-label">Stock (cartons)</label><input id="pe-stock" type="number" class="input mono" value="${p.stock}" /></div>
            <div><label class="field-label">Choix</label><input id="pe-choix" class="input" value="${esc(p.choix)}" /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="pe-save-btn" class="btn btn-primary">Enregistrer Article</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "customer_edit") {
    const c = m.customer || { id: makeId("c"), nom: "", tel: "", adresse: "", dette: 0, totalAchats: 0 };
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">${m.customer ? "Modifier Client" : "Nouveau Client"}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div style="grid-column:1/-1"><label class="field-label">Nom complet</label><input id="ce-nom" class="input" value="${esc(c.nom)}" /></div>
            <div><label class="field-label">Téléphone</label><input id="ce-tel" class="input" value="${esc(c.tel)}" /></div>
            <div><label class="field-label">Adresse</label><input id="ce-addr" class="input" value="${esc(c.adresse)}" /></div>
            <div><label class="field-label">Dette actuelle (DA)</label><input id="ce-dette" type="number" min="0" class="input mono" value="${c.dette}" /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="ce-save-btn" class="btn btn-primary">Enregistrer Client</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "pay_debt") {
    const c = m.customer;
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">Encaisser Crédit — ${esc(c.nom)}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <p>Dette actuelle : <strong class="mono">${formatDa(c.dette)}</strong></p>
          <label class="field-label">Montant encaissé (DA)</label>
          <input id="pd-amount" type="number" min="1" class="input mono" value="${c.dette}" />
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="pd-save-btn" class="btn btn-primary">Encaisser en Caisse</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "supplier_edit") {
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">Nouveau Fournisseur / Usine</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div style="grid-column:1/-1"><label class="field-label">Raison Sociale</label><input id="se-nom" class="input" /></div>
            <div><label class="field-label">Usine Représentée</label><input id="se-usine" class="input" /></div>
            <div><label class="field-label">Contact</label><input id="se-contact" class="input" /></div>
            <div><label class="field-label">Téléphone</label><input id="se-tel" class="input" /></div>
            <div><label class="field-label">Adresse</label><input id="se-addr" class="input" /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="se-save-btn" class="btn btn-primary">Enregistrer</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "supplier_mov") {
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">${m.movType === "versement" ? "Versement Usine (Sortie Caisse)" : "Réception Facture Usine (+ Dette)"}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div><label class="field-label">Montant (DA)</label><input id="sm-montant" type="number" min="1" class="input mono" /></div>
            <div><label class="field-label">N° Pièce / BL</label><input id="sm-ref" class="input" /></div>
            <div style="grid-column:1/-1"><label class="field-label">Motif</label><input id="sm-motif" class="input" value="${m.movType === "versement" ? "Acompte règlement usine" : "Réception lot carrelage"}" /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="sm-save-btn" class="btn btn-primary">Valider</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "caisse_mov") {
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">${m.movType === "entree" ? "Nouvelle Entrée de Caisse" : "Enregistrer Dépense / Sortie"}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div><label class="field-label">Montant (DA)</label><input id="cm-montant" type="number" min="1" class="input mono" /></div>
            <div><label class="field-label">Motif</label><input id="cm-motif" class="input" placeholder="Ex: Transport, Frais, Apport..." /></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="cm-save-btn" class="btn btn-primary">Valider Mouvement</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "daily_count") {
    const todayPrefix = new Date().toISOString().slice(0, 10);
    const todaySales = round2(
      state.invoices.filter((i) => String(i.date).startsWith(todayPrefix)).reduce((s, i) => s + (Number(i.montantPaye) || 0), 0)
    );
    const todayOut = round2(
      state.caisseMovements.filter((x) => x.type === "sortie" && String(x.date).startsWith(todayPrefix)).reduce((s, x) => s + x.montant, 0)
    );
    const theo = round2(todaySales - todayOut);
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">Pointage Caisse Fin de Journée</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div><label class="field-label">Fond d'ouverture (DA)</label><input id="dc-fond" type="number" class="input mono" value="0" /></div>
            <div><label class="field-label">Espèces comptées réel (DA)</label><input id="dc-reel" type="number" class="input mono" value="${theo}" /></div>
            <div style="grid-column:1/-1"><label class="field-label">Notes clôture</label><input id="dc-notes" class="input" /></div>
          </div>
          <p style="font-size:12.5px;color:var(--ink-secondary)">Ventes encaissées aujourd'hui : <strong class="mono">${formatDa(todaySales)}</strong> • Sorties : <strong class="mono">${formatDa(todayOut)}</strong></p>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:12px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="dc-save-btn" class="btn btn-primary">Sauvegarder Clôture</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "user_edit") {
    const u = m.user || {
      id: makeId("u"),
      name: "",
      role: "vendeur",
      pin: "0000",
      magasin: "Depot Remchi",
      phone: "",
      canViewPrixAchat: false,
      canModifyPrices: false,
      canModifyStock: false,
      canEditInvoices: true,
      canDeleteInvoices: false,
      canViewSuppliers: false,
      canViewCaisse: false,
      canManageUsers: false,
      active: true,
    };
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">${m.user ? "Modifier Utilisateur" : "Nouvel Utilisateur"}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div class="form-grid">
            <div><label class="field-label">Nom</label><input id="ue-name" class="input" value="${esc(u.name)}" /></div>
            <div>
              <label class="field-label">Rôle</label>
              <select id="ue-role" class="select">
                <option value="vendeur" ${u.role === "vendeur" ? "selected" : ""}>Vendeur</option>
                <option value="gerant" ${u.role === "gerant" ? "selected" : ""}>Gérant / Manager</option>
                <option value="admin" ${u.role === "admin" ? "selected" : ""}>Admin</option>
              </select>
            </div>
            <div><label class="field-label">PIN</label><input id="ue-pin" class="input mono" value="${esc(u.pin)}" /></div>
            <div>
              <label class="field-label">Magasin</label>
              <select id="ue-mag" class="select">
                ${SITES.map((s) => `<option value="${esc(s)}" ${u.magasin === s ? "selected" : ""}>${esc(s)}</option>`).join("")}
              </select>
            </div>
            <div><label class="field-label">Téléphone</label><input id="ue-phone" class="input" value="${esc(u.phone || "")}" /></div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:12px">
            <label><input type="checkbox" id="ue-p-achat" ${u.canViewPrixAchat ? "checked" : ""} /> Voir Prix Achat</label>
            <label><input type="checkbox" id="ue-p-prix" ${u.canModifyPrices ? "checked" : ""} /> Modifier Prix / Remise</label>
            <label><input type="checkbox" id="ue-p-stock" ${u.canModifyStock ? "checked" : ""} /> Modifier Stock</label>
            <label><input type="checkbox" id="ue-p-del" ${u.canDeleteInvoices ? "checked" : ""} /> Supprimer Factures</label>
            <label><input type="checkbox" id="ue-p-sup" ${u.canViewSuppliers ? "checked" : ""} /> Accès Usines</label>
            <label><input type="checkbox" id="ue-p-cai" ${u.canViewCaisse ? "checked" : ""} /> Accès Caisse</label>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="ue-save-btn" class="btn btn-primary">Enregistrer Compte</button>
          </div>
        </div>
      </div>`;
  } else if (m.type === "policy_edit") {
    const p = m.policy;
    html = `
      <div class="modal-backdrop" id="modal-backdrop">
        <div class="modal-box">
          <div class="card-header"><h2 class="card-title">Politique Rôle : ${esc(p.displayName)}</h2><button class="btn btn-secondary btn-sm" id="modal-close-btn">✕</button></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
            <label><input type="checkbox" id="rp-achat" ${p.canViewPrixAchat ? "checked" : ""} /> Voir Prix d'Achat</label>
            <label><input type="checkbox" id="rp-prix" ${p.canModifyPrices ? "checked" : ""} /> Modifier Tarifs &amp; Remises</label>
            <label><input type="checkbox" id="rp-stock" ${p.canModifyStock ? "checked" : ""} /> Modifier Stock</label>
            <label><input type="checkbox" id="rp-edit" ${p.canEditInvoices ? "checked" : ""} /> Modifier Factures</label>
            <label><input type="checkbox" id="rp-del" ${p.canDeleteInvoices ? "checked" : ""} /> Supprimer Factures</label>
            <label><input type="checkbox" id="rp-sup" ${p.canViewSuppliers ? "checked" : ""} /> Accès Usines</label>
            <label><input type="checkbox" id="rp-cai" ${p.canViewCaisse ? "checked" : ""} /> Accès Caisse</label>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:16px">
            <button id="modal-close-btn-2" class="btn btn-secondary">Annuler</button>
            <button id="rp-save-btn" class="btn btn-primary">Appliquer Politique</button>
          </div>
        </div>
      </div>`;
  }

  mRoot.innerHTML = html;
  bindModalHandlers();
}

function renderDrawer() {
  const dRoot = document.getElementById("drawer-root");
  if (!dRoot) return;
  if (!state.notificationsOpen) {
    dRoot.innerHTML = "";
    return;
  }

  dRoot.innerHTML = `
    <div class="drawer-backdrop" id="drawer-backdrop">
      <div class="drawer-panel">
        <div class="card-header" style="margin-bottom:4px">
          <h2 class="card-title">Alertes Dépôt en Direct (${state.notifications.length})</h2>
          <button id="drawer-close-btn" class="btn btn-secondary btn-sm">✕</button>
        </div>
        <button id="mark-all-read-btn" class="btn btn-primary btn-sm">Tout marquer comme lu</button>
        ${
          state.notifications.length === 0
            ? `<div style="color:var(--ink-muted);text-align:center;padding:30px">Aucune notification.</div>`
            : state.notifications
                .map(
                  (n) => `
              <div style="padding:10px 12px;border-radius:10px;border:1px solid ${n.read ? "var(--border)" : "var(--terracotta)"};background:${n.read ? "var(--bg-canvas)" : "var(--terracotta-soft)"}">
                <div style="display:flex;justify-content:space-between;gap:8px">
                  <strong style="font-size:12.5px">${esc(n.reference)}</strong>
                  <span style="font-size:11px;color:var(--ink-muted)">${esc(formatDateTimeFr(n.date))}</span>
                </div>
                <div style="font-size:12px;margin-top:4px">${esc(n.articlesSummary)}</div>
                <div style="font-size:11px;color:var(--ink-secondary);margin-top:4px">${esc(n.vendeur)} • ${esc(n.magasin)}</div>
              </div>`
                )
                .join("")
        }
      </div>
    </div>
  `;

  document.getElementById("drawer-backdrop")?.addEventListener("click", (e) => {
    if (e.target.id === "drawer-backdrop") {
      state.notificationsOpen = false;
      renderDrawer();
    }
  });
  document.getElementById("drawer-close-btn")?.addEventListener("click", () => {
    state.notificationsOpen = false;
    renderDrawer();
  });
  document.getElementById("mark-all-read-btn")?.addEventListener("click", async () => {
    const unreadIds = state.notifications.filter((n) => !n.read).map((n) => n.id);
    if (!unreadIds.length) return;
    if (state.mode === "local_preview") {
      state.notifications = state.notifications.map((n) => ({ ...n, read: true }));
      renderDrawer();
      render();
      return;
    }
    await runCloudWrite(() => syncClient.markAllNotificationsRead(unreadIds), "Notifications marquées lues");
  });
}

function renderToast() {
  const tRoot = document.getElementById("toast-root");
  if (!tRoot) return;
  tRoot.innerHTML = state.statusToast ? `<div class="toast">${esc(state.statusToast)}</div>` : "";
}

// --- Event Bindings ---

function bindSetupHandlers() {
  document.getElementById("save-fb-config-btn")?.addEventListener("click", async () => {
    const raw = document.getElementById("fb-config-input")?.value || "";
    const parsed = parseFirebaseConfigInput(raw);
    const errEl = document.getElementById("fb-config-err");
    if (!parsed.ok) {
      if (errEl) errEl.textContent = parsed.error;
      return;
    }
    if (parsed.projectMismatch) {
      const ok = confirm(
        `Attention : projectId est "${parsed.config.projectId}" alors que l'app Android utilise "${FIREBASE_PROJECT_ID}". Continuer quand même ?`
      );
      if (!ok) return;
    }
    saveFirebaseConfig(parsed.config);
    state.firebaseConfig = parsed.config;
    await bootFirebase(parsed.config);
  });
  document.getElementById("enter-local-preview-btn")?.addEventListener("click", enterLocalPreviewMode);
}

function bindGoogleLoginHandlers() {
  document.getElementById("google-signin-btn")?.addEventListener("click", async () => {
    try {
      state.cloudError = null;
      await syncClient.signInWithGoogle();
    } catch (err) {
      state.cloudError = err?.message || String(err);
      render();
    }
  });
  document.getElementById("edit-fb-config-btn")?.addEventListener("click", () => {
    state.mode = "setup";
    render();
  });
  document.getElementById("enter-local-preview-btn")?.addEventListener("click", enterLocalPreviewMode);
}

function bindPinLockHandlers() {
  document.querySelectorAll("[data-pin-user]").forEach((btn) => {
    btn.addEventListener("click", () => {
      state.selectedPinUserId = btn.dataset.pinUser;
      state.pinError = "";
      render();
    });
  });
  const tryUnlock = () => {
    const pin = document.getElementById("pin-input")?.value || "";
    const target = state.staffUsers.find((u) => u.id === state.selectedPinUserId);
    if (!target) return;
    if (target.pin === pin) {
      state.currentUserId = target.id;
      state.isPinLocked = false;
      state.pinError = "";
      ensureValidActiveTab();
      render();
    } else {
      state.pinError = "Code PIN incorrect.";
      render();
    }
  };
  document.getElementById("pin-unlock-btn")?.addEventListener("click", tryUnlock);
  document.getElementById("pin-input")?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") tryUnlock();
  });
}

function bindAppShellHandlers() {
  document.querySelectorAll("[data-tab]").forEach((btn) => {
    btn.addEventListener("click", () => {
      state.activeTab = btn.dataset.tab;
      render();
    });
  });

  document.getElementById("top-store-select")?.addEventListener("change", (e) => {
    const nextSettings = { ...state.settings, activeStore: e.target.value };
    state.settings = nextSettings;
    if (state.mode !== "local_preview") {
      runCloudWrite(() => syncClient.saveAppSettings(nextSettings));
    }
    render();
  });

  document.getElementById("top-cloud-pill")?.addEventListener("click", async () => {
    if (state.mode === "local_preview") {
      state.mode = state.firebaseConfig ? "auth" : "setup";
      render();
      return;
    }
    await runCloudWrite(() => syncClient.awaitPendingSync(10000), "Synchronisation à jour avec Android !");
  });

  document.getElementById("top-notif-btn")?.addEventListener("click", () => {
    state.notificationsOpen = !state.notificationsOpen;
    renderDrawer();
  });

  document.getElementById("top-lock-btn")?.addEventListener("click", () => {
    state.selectedPinUserId = state.currentUserId;
    state.pinError = "";
    state.isPinLocked = true;
    render();
  });

  document.getElementById("sidebar-signout-btn")?.addEventListener("click", () => syncClient.signOutUser());
  document.getElementById("sidebar-connect-cloud-btn")?.addEventListener("click", () => {
    state.mode = state.firebaseConfig ? "auth" : "setup";
    render();
  });
  document.getElementById("banner-setup-cloud")?.addEventListener("click", () => {
    state.mode = state.firebaseConfig ? "auth" : "setup";
    render();
  });
  document.getElementById("dismiss-cloud-err")?.addEventListener("click", () => {
    state.cloudError = null;
    render();
  });
}

function bindActiveTabHandlers() {
  if (state.activeTab === "pos") {
    const sInput = document.getElementById("pos-search-input");
    sInput?.addEventListener("input", (e) => {
      state.posSearch = e.target.value;
      render();
      const again = document.getElementById("pos-search-input");
      if (again) {
        again.focus();
        again.setSelectionRange(again.value.length, again.value.length);
      }
    });
    document.getElementById("pos-usine-select")?.addEventListener("change", (e) => {
      state.posUsine = e.target.value;
      render();
    });
    document.getElementById("pos-dim-select")?.addEventListener("change", (e) => {
      state.posDimension = e.target.value;
      render();
    });
    document.getElementById("pos-stock-only")?.addEventListener("change", (e) => {
      state.posInStockOnly = e.target.checked;
      render();
    });
    document.querySelectorAll("[data-open-calc]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const prod = state.products.find((p) => p.id === btn.dataset.openCalc);
        if (prod) {
          state.modal = { type: "tile_calc", product: prod };
          renderModal();
        }
      });
    });
    document.querySelectorAll("[data-remove-cart]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.cart = state.cart.filter((c) => c.id !== btn.dataset.removeCart);
        render();
      });
    });
    document.querySelectorAll("[data-cart-delta]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const [id, dStr] = btn.dataset.cartDelta.split(":");
        const delta = parseInt(dStr, 10);
        state.cart = state.cart
          .map((c) => {
            if (c.id !== id) return c;
            const nextCtn = Math.max(0, c.cartons + delta);
            if (nextCtn === 0 && c.pieces === 0) return null;
            const t = TileCalculatorMath.calculateCartItemTotals(
              nextCtn,
              c.pieces,
              c.m2Carton,
              c.pcs,
              c.prixUnitaire,
              c.prixPiece
            );
            return { ...c, cartons: nextCtn, m2Total: t.m2Total, sousTotal: t.sousTotal };
          })
          .filter(Boolean);
        render();
      });
    });
    document.querySelectorAll("[data-remove-free]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.freeLines = state.freeLines.filter((l) => l.id !== btn.dataset.removeFree);
        render();
      });
    });
    document.getElementById("open-free-line-btn")?.addEventListener("click", () => {
      state.modal = { type: "free_line" };
      renderModal();
    });
    document.getElementById("clear-cart-btn")?.addEventListener("click", () => {
      state.cart = [];
      state.freeLines = [];
      state.editingInvoice = null;
      render();
    });
    document.getElementById("open-checkout-btn")?.addEventListener("click", () => {
      state.modal = { type: "checkout" };
      renderModal();
    });
  } else if (state.activeTab === "stock") {
    const sInput = document.getElementById("stock-search-input");
    sInput?.addEventListener("input", (e) => {
      state.stockSearch = e.target.value;
      render();
      const again = document.getElementById("stock-search-input");
      if (again) {
        again.focus();
        again.setSelectionRange(again.value.length, again.value.length);
      }
    });
    document.getElementById("stock-usine-select")?.addEventListener("change", (e) => {
      state.stockUsine = e.target.value;
      render();
    });
    document.getElementById("stock-alerts-only")?.addEventListener("change", (e) => {
      state.stockOnlyAlerts = e.target.checked;
      render();
    });
    document.getElementById("new-product-btn")?.addEventListener("click", () => {
      state.modal = { type: "product_edit", product: null };
      renderModal();
    });
    document.querySelectorAll("[data-edit-product]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const p = state.products.find((x) => x.id === btn.dataset.editProduct);
        if (p) {
          state.modal = { type: "product_edit", product: p };
          renderModal();
        }
      });
    });
    document.querySelectorAll("[data-stock-delta]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const [id, dStr] = btn.dataset.stockDelta.split(":");
        const delta = parseInt(dStr, 10);
        if (state.mode === "local_preview") {
          state.products = state.products.map((p) => (p.id === id ? { ...p, stock: p.stock + delta } : p));
          render();
          return;
        }
        await runCloudWrite(() => syncClient.adjustProductStockDelta(id, delta));
      });
    });
    document.querySelectorAll("[data-dispo-product]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const p = state.products.find((x) => x.id === btn.dataset.dispoProduct);
        if (p) requestDepotDispo(p);
      });
    });
  } else if (state.activeTab === "invoices") {
    document.getElementById("inv-search-input")?.addEventListener("input", (e) => {
      state.invoiceSearch = e.target.value;
      render();
      const again = document.getElementById("inv-search-input");
      if (again) {
        again.focus();
        again.setSelectionRange(again.value.length, again.value.length);
      }
    });
    document.querySelectorAll("[data-inv-status]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.invoiceStatusFilter = btn.dataset.invStatus;
        render();
      });
    });
    document.querySelectorAll("[data-view-inv]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const inv = state.invoices.find((i) => i.id === btn.dataset.viewInv);
        if (inv) {
          state.modal = { type: "invoice_preview", invoiceId: inv.id, snapshot: inv };
          renderModal();
        }
      });
    });
    document.querySelectorAll("[data-edit-inv]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const inv = state.invoices.find((i) => i.id === btn.dataset.editInv);
        if (inv) {
          state.cart = [...(inv.articles || [])];
          state.freeLines = [...(inv.lignesLibres || [])];
          state.editingInvoice = inv;
          state.activeTab = "pos";
          showToast(`Modification de #${inv.reference}`);
          render();
        }
      });
    });
    document.querySelectorAll("[data-del-inv]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const inv = state.invoices.find((i) => i.id === btn.dataset.delInv);
        if (inv) deleteInvoice(inv);
      });
    });
  } else if (state.activeTab === "dashboard") {
    document.querySelectorAll("[data-stats-period]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.statsPeriod = btn.dataset.statsPeriod;
        render();
      });
    });
  } else if (state.activeTab === "customers") {
    document.getElementById("cust-search-input")?.addEventListener("input", (e) => {
      state.customerSearch = e.target.value;
      render();
      const again = document.getElementById("cust-search-input");
      if (again) {
        again.focus();
        again.setSelectionRange(again.value.length, again.value.length);
      }
    });
    document.getElementById("new-customer-btn")?.addEventListener("click", () => {
      state.modal = { type: "customer_edit", customer: null };
      renderModal();
    });
    document.querySelectorAll("[data-edit-cust]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const c = state.customers.find((x) => x.id === btn.dataset.editCust);
        if (c) {
          state.modal = { type: "customer_edit", customer: c };
          renderModal();
        }
      });
    });
    document.querySelectorAll("[data-pay-debt]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const c = state.customers.find((x) => x.id === btn.dataset.payDebt);
        if (c) {
          state.modal = { type: "pay_debt", customer: c };
          renderModal();
        }
      });
    });
  } else if (state.activeTab === "suppliers") {
    document.querySelectorAll("[data-select-sup]").forEach((el) => {
      el.addEventListener("click", () => {
        state.selectedSupplierId = el.dataset.selectSup;
        render();
      });
    });
    document.getElementById("new-supplier-btn")?.addEventListener("click", () => {
      state.modal = { type: "supplier_edit" };
      renderModal();
    });
    document.querySelectorAll("[data-sup-mov]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const [movType, supId] = btn.dataset.supMov.split(":");
        const sup = state.suppliers.find((s) => s.id === supId);
        if (sup) {
          state.modal = { type: "supplier_mov", movType, supplier: sup };
          renderModal();
        }
      });
    });
  } else if (state.activeTab === "caisse") {
    document.getElementById("add-caisse-entree-btn")?.addEventListener("click", () => {
      state.modal = { type: "caisse_mov", movType: "entree" };
      renderModal();
    });
    document.getElementById("add-caisse-sortie-btn")?.addEventListener("click", () => {
      state.modal = { type: "caisse_mov", movType: "sortie" };
      renderModal();
    });
    document.getElementById("open-daily-count-btn")?.addEventListener("click", () => {
      state.modal = { type: "daily_count" };
      renderModal();
    });
  } else if (state.activeTab === "users") {
    document.getElementById("new-user-btn")?.addEventListener("click", () => {
      state.modal = { type: "user_edit", user: null };
      renderModal();
    });
    document.querySelectorAll("[data-edit-user]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const u = state.staffUsers.find((x) => x.id === btn.dataset.editUser);
        if (u) {
          state.modal = { type: "user_edit", user: u };
          renderModal();
        }
      });
    });
    document.querySelectorAll("[data-del-user]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!confirm("Supprimer ce compte ?")) return;
        if (state.mode === "local_preview") {
          state.staffUsers = state.staffUsers.filter((x) => x.id !== btn.dataset.delUser);
          render();
          return;
        }
        await runCloudWrite(() => syncClient.deleteStaffUser(btn.dataset.delUser), "Compte supprimé");
      });
    });
    document.querySelectorAll("[data-edit-policy]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const p = state.rolePolicies.find((x) => x.roleId === btn.dataset.editPolicy);
        if (p) {
          state.modal = { type: "policy_edit", policy: p };
          renderModal();
        }
      });
    });
  } else if (state.activeTab === "settings") {
    document.getElementById("save-store-info-btn")?.addEventListener("click", async () => {
      const next = {
        ...state.storeInfo,
        nom: document.getElementById("si-nom")?.value || "CERAMICA",
        nomArabe: document.getElementById("si-arabe")?.value || "",
        tel: document.getElementById("si-tel")?.value || "0676754124",
        tel2: document.getElementById("si-tel2")?.value || "0676754124",
        adresse: document.getElementById("si-adresse")?.value || "",
        email: "",
        rc: document.getElementById("si-rc")?.value || "",
        nif: document.getElementById("si-nif")?.value || "",
      };
      state.storeInfo = next;
      if (state.mode === "local_preview") return showToast("Enregistré localement");
      await runCloudWrite(() => syncClient.saveStoreInfo(next), "Informations magasin synchronisées");
    });
    document.getElementById("save-settings-btn")?.addEventListener("click", async () => {
      const next = {
        ...state.settings,
        printFormat: document.getElementById("st-print")?.value || "A4",
        activeStore: document.getElementById("st-store")?.value || "Depot Remchi",
        deviceSellerName: document.getElementById("st-seller-name")?.value || "",
        deviceSellerPhone: document.getElementById("st-seller-phone")?.value || "",
        enableSoundAlerts: Boolean(document.getElementById("st-sound")?.checked),
        hideBainTeinteFacture: Boolean(document.getElementById("st-hide-bain")?.checked),
      };
      state.settings = next;
      if (state.mode === "local_preview") return showToast("Préférences enregistrées");
      await runCloudWrite(() => syncClient.saveAppSettings(next), "Préférences enregistrées");
    });
    document.getElementById("req-browser-notif-btn")?.addEventListener("click", async () => {
      if (!("Notification" in window)) return showToast("Notifications non supportées par ce navigateur.");
      const perm = await Notification.requestPermission();
      showToast(perm === "granted" ? "Notifications Bureau activées !" : "Permission refusée dans le navigateur.");
    });
    document.getElementById("export-json-btn")?.addEventListener("click", exportJsonBackup);
    document.getElementById("reset-fb-config-btn")?.addEventListener("click", () => {
      if (confirm("Modifier / réinitialiser la configuration Firebase Web ?")) {
        clearFirebaseConfig();
        state.mode = "setup";
        render();
      }
    });
  } else if (state.activeTab === "calculator") {
    const syncCalc = (id, key, isNum = true) => {
      document.getElementById(id)?.addEventListener("input", (e) => {
        state.calcState[key] = isNum ? Number(e.target.value) || 0 : e.target.value;
        const val = e.target.value;
        render();
        const again = document.getElementById(id);
        if (again) {
          again.value = val;
          again.focus();
        }
      });
    };
    syncCalc("calc-rl", "roomL");
    syncCalc("calc-rw", "roomW");
    syncCalc("calc-tl", "tileL");
    syncCalc("calc-tw", "tileW");
    syncCalc("calc-m2c", "m2Carton");
    document.getElementById("calc-margin")?.addEventListener("change", (e) => {
      state.calcState.marginPct = parseInt(e.target.value, 10) || 0;
      render();
    });
    document.getElementById("calc-product-link")?.addEventListener("change", (e) => {
      const p = state.products.find((x) => x.id === e.target.value);
      state.calcState.linkedProductId = e.target.value;
      if (p) state.calcState.m2Carton = p.m2Carton;
      render();
    });
    document.getElementById("calc-add-cart-btn")?.addEventListener("click", () => {
      const p = state.products.find((x) => x.id === state.calcState.linkedProductId);
      if (!p) return;
      const plan = TileCalculatorMath.calculateRoomTilePlan(
        state.calcState.roomL,
        state.calcState.roomW,
        state.calcState.tileL,
        state.calcState.tileW,
        "cm",
        state.calcState.m2Carton,
        state.calcState.marginPct
      );
      addOrUpdateCartItemFromModal(p, {
        cartons: plan.cartonsNeeded,
        loosePieces: 0,
        prixUnitaire: p.prix,
        prixPiece: p.prixPiece,
        bain: "",
        calibre: "",
      });
      state.activeTab = "pos";
      render();
    });
  }
}

function bindModalHandlers() {
  const m = state.modal;
  if (!m) return;
  const close = () => {
    state.modal = null;
    renderModal();
  };
  document.getElementById("modal-backdrop")?.addEventListener("click", (e) => {
    if (e.target.id === "modal-backdrop") close();
  });
  document.getElementById("modal-close-btn")?.addEventListener("click", close);
  document.getElementById("modal-close-btn-2")?.addEventListener("click", close);

  if (m.type === "tile_calc") {
    const p = m.product;
    const surfEl = document.getElementById("mc-surface");
    const marginEl = document.getElementById("mc-margin");
    const ctnEl = document.getElementById("mc-cartons");
    const pcsEl = document.getElementById("mc-pieces");
    const prixEl = document.getElementById("mc-prix");
    const sumEl = document.getElementById("mc-summary");

    const updateSummary = () => {
      const res = TileCalculatorMath.calculateFromCartonsAndPieces(
        Number(ctnEl.value),
        Number(pcsEl.value),
        p.m2Carton,
        p.pcs,
        Number(prixEl.value),
        p.prixPiece
      );
      if (sumEl) {
        sumEl.innerHTML = `
          <div style="display:flex;justify-content:space-between">
            <span>Surface livrée : <strong class="mono">${formatM2(res.deliveredM2)} m²</strong></span>
            <span>Sous-total : <strong class="mono" style="color:var(--teal);font-size:17px">${formatDa(res.totalDa)}</strong></span>
          </div>`;
      }
    };
    const recalcFromSurface = () => {
      const s = Number(surfEl.value);
      if (s > 0) {
        const res = TileCalculatorMath.calculateFromSurface(s, Number(marginEl.value), p.m2Carton, p.pcs, Number(prixEl.value));
        ctnEl.value = res.cartons;
        pcsEl.value = 0;
      }
      updateSummary();
    };
    surfEl?.addEventListener("input", recalcFromSurface);
    marginEl?.addEventListener("change", recalcFromSurface);
    ctnEl?.addEventListener("input", updateSummary);
    pcsEl?.addEventListener("input", updateSummary);
    prixEl?.addEventListener("input", updateSummary);
    updateSummary();

    document.getElementById("mc-confirm-btn")?.addEventListener("click", () => {
      addOrUpdateCartItemFromModal(p, {
        cartons: Number(ctnEl.value),
        loosePieces: Number(pcsEl.value),
        prixUnitaire: Number(prixEl.value),
        prixPiece: p.prixPiece,
        bain: document.getElementById("mc-bain")?.value || "",
        calibre: document.getElementById("mc-calibre")?.value || "",
      });
    });
  } else if (m.type === "free_line") {
    document.querySelectorAll("[data-preset-idx]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const pr = DIVERS_PRESETS[parseInt(btn.dataset.presetIdx, 10)];
        if (!pr) return;
        document.getElementById("fl-desig").value = pr.designation;
        document.getElementById("fl-unite").value = pr.unite;
        document.getElementById("fl-prix").value = pr.prix;
      });
    });
    document.getElementById("fl-confirm-btn")?.addEventListener("click", () => {
      addFreeLineItem({
        designation: document.getElementById("fl-desig").value,
        quantite: Number(document.getElementById("fl-qty").value),
        unite: document.getElementById("fl-unite").value,
        prixUnitaire: Number(document.getElementById("fl-prix").value),
      });
      close();
    });
  } else if (m.type === "checkout") {
    const brut = round2(
      state.cart.reduce((s, a) => s + a.sousTotal, 0) + state.freeLines.reduce((s, l) => s + l.sousTotal, 0)
    );
    const selEl = document.getElementById("co-client-select");
    const nomEl = document.getElementById("co-client-nom");
    const telEl = document.getElementById("co-client-tel");
    const remiseEl = document.getElementById("co-remise");
    const modeEl = document.getElementById("co-mode");
    const payeEl = document.getElementById("co-paye");
    const netEl = document.getElementById("co-net-display");

    selEl?.addEventListener("change", () => {
      const c = state.customers.find((x) => x.id === selEl.value);
      if (c) {
        nomEl.value = c.nom;
        telEl.value = c.tel;
      }
    });
    const refreshNet = () => {
      const rem = Math.min(brut, Math.max(0, Number(remiseEl.value) || 0));
      const net = round2(brut - rem);
      if (netEl) netEl.textContent = formatDa(net);
      if (modeEl.value === "Espèces") payeEl.value = net;
      if (modeEl.value === "Crédit") payeEl.value = 0;
    };
    remiseEl?.addEventListener("input", refreshNet);
    modeEl?.addEventListener("change", refreshNet);

    document.getElementById("co-submit-btn")?.addEventListener("click", () => {
      const c = state.customers.find((x) => x.id === selEl.value);
      submitSaleCheckout({
        clientId: selEl.value || null,
        clientNom: nomEl.value,
        clientTel: telEl.value,
        clientAdresse: c?.adresse || "",
        remise: Number(remiseEl.value) || 0,
        modePaiement: modeEl.value,
        montantPaye: Number(payeEl.value) || 0,
        notes: document.getElementById("co-notes")?.value || "",
      });
    });
  } else if (m.type === "invoice_preview") {
    const inv = state.invoices.find((i) => i.id === m.invoiceId) || m.snapshot;
    document.getElementById("inv-print-btn")?.addEventListener("click", () => window.print());
    document.getElementById("inv-png-btn")?.addEventListener("click", () => {
      sendInvoicePhotoToBureauWhatsApp(inv, null, { downloadOnly: true });
    });
    document.getElementById("inv-wa-btn")?.addEventListener("click", () => {
      sendInvoicePhotoToBureauWhatsApp(inv, "213676754124");
    });
    document.getElementById("inv-toggle-prices-btn")?.addEventListener("click", async () => {
      const next = !inv.hidePrices;
      state.invoices = state.invoices.map((i) => (i.id === inv.id ? { ...i, hidePrices: next } : i));
      if (m.snapshot) m.snapshot.hidePrices = next;
      if (state.mode !== "local_preview") {
        await runCloudWrite(() => syncClient.updateInvoiceHidePrices(inv.id, next));
      }
      renderModal();
    });
  } else if (m.type === "product_edit") {
    document.getElementById("pe-save-btn")?.addEventListener("click", async () => {
      const base = m.product || { id: makeId("p"), color: "#94a3b8", colorName: "Gris Minéral", image: "", finition: "brillant", categorie: "sol", description: "", pricingUnit: "carton", prixPiece: 0 };
      const prod = {
        ...base,
        name: document.getElementById("pe-name").value.trim(),
        usine: document.getElementById("pe-usine").value.trim(),
        dimension: document.getElementById("pe-dim").value.trim(),
        m2Carton: Math.max(0.01, Number(document.getElementById("pe-m2").value) || 1),
        pcs: Math.max(1, Math.trunc(Number(document.getElementById("pe-pcs").value) || 1)),
        prix: Math.max(0, Number(document.getElementById("pe-prix").value) || 0),
        prixAchat: Math.max(0, Number(document.getElementById("pe-achat").value) || 0),
        stock: Math.trunc(Number(document.getElementById("pe-stock").value) || 0),
        choix: document.getElementById("pe-choix").value.trim() || "1er Choix",
      };
      if (!prod.name) return showToast("Le nom de l'article est obligatoire.");
      if (state.mode === "local_preview") {
        const idx = state.products.findIndex((x) => x.id === prod.id);
        if (idx >= 0) state.products[idx] = prod;
        else state.products.unshift(prod);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.upsertProduct(prod), "Article enregistré");
      if (ok) close();
    });
  } else if (m.type === "customer_edit") {
    document.getElementById("ce-save-btn")?.addEventListener("click", async () => {
      const base = m.customer || { id: makeId("c"), totalAchats: 0 };
      const cust = {
        ...base,
        nom: document.getElementById("ce-nom").value.trim(),
        tel: document.getElementById("ce-tel").value.trim(),
        adresse: document.getElementById("ce-addr").value.trim(),
        dette: Math.max(0, Number(document.getElementById("ce-dette").value) || 0),
      };
      if (!cust.nom) return showToast("Nom client obligatoire.");
      if (state.mode === "local_preview") {
        const idx = state.customers.findIndex((x) => x.id === cust.id);
        if (idx >= 0) state.customers[idx] = cust;
        else state.customers.push(cust);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.upsertCustomer(cust), "Client enregistré");
      if (ok) close();
    });
  } else if (m.type === "pay_debt") {
    document.getElementById("pd-save-btn")?.addEventListener("click", async () => {
      const amt = Math.max(0, Number(document.getElementById("pd-amount").value) || 0);
      if (amt <= 0) return;
      const updatedCust = { ...m.customer, dette: round2(Math.max(0, m.customer.dette - amt)) };
      const caisseMov = {
        id: makeId("caisse"),
        date: nowIso(),
        type: "entree",
        motif: `Remboursement crédit client - ${m.customer.nom}`,
        montant: amt,
        auteur: getCurrentUser().name,
        referenceFacture: null,
      };
      if (state.mode === "local_preview") {
        state.customers = state.customers.map((x) => (x.id === updatedCust.id ? updatedCust : x));
        state.caisseMovements.unshift(caisseMov);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.payCustomerDebt(updatedCust, caisseMov), "Remboursement encaissé");
      if (ok) close();
    });
  } else if (m.type === "supplier_edit") {
    document.getElementById("se-save-btn")?.addEventListener("click", async () => {
      const sup = {
        id: makeId("f"),
        nom: document.getElementById("se-nom").value.trim(),
        usineRepresentee: document.getElementById("se-usine").value.trim(),
        contact: document.getElementById("se-contact").value.trim(),
        tel: document.getElementById("se-tel").value.trim(),
        adresse: document.getElementById("se-addr").value.trim(),
        soldeDette: 0,
        totalAchats: 0,
      };
      if (!sup.nom) return showToast("Nom usine obligatoire.");
      if (state.mode === "local_preview") {
        state.suppliers.push(sup);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.upsertSupplier(sup), "Fournisseur enregistré");
      if (ok) close();
    });
  } else if (m.type === "supplier_mov") {
    document.getElementById("sm-save-btn")?.addEventListener("click", async () => {
      const amt = Math.max(0, Number(document.getElementById("sm-montant").value) || 0);
      if (amt <= 0) return showToast("Montant invalide");
      const motif = document.getElementById("sm-motif").value.trim();
      const ref = document.getElementById("sm-ref").value.trim() || null;
      const sup = m.supplier;
      const author = getCurrentUser().name;
      const isVers = m.movType === "versement";
      const updatedSup = {
        ...sup,
        soldeDette: isVers ? round2(Math.max(0, sup.soldeDette - amt)) : round2(sup.soldeDette + amt),
        totalAchats: isVers ? sup.totalAchats : round2(sup.totalAchats + amt),
      };
      const supMov = {
        id: makeId("sup_mov"),
        supplierId: sup.id,
        date: nowIso(),
        type: isVers ? "versement" : "achat",
        montant: amt,
        motif: motif || (isVers ? "Versement usine" : "Réception marchandise"),
        pieceJointeRef: ref,
        auteur: author,
      };
      const caisseMov = isVers
        ? {
            id: makeId("caisse"),
            date: nowIso(),
            type: "sortie",
            motif: `Règlement usine ${sup.nom} : ${motif}`,
            montant: amt,
            auteur: author,
            referenceFacture: ref,
          }
        : null;
      if (state.mode === "local_preview") {
        state.suppliers = state.suppliers.map((x) => (x.id === sup.id ? updatedSup : x));
        state.supplierMovements.unshift(supMov);
        if (caisseMov) state.caisseMovements.unshift(caisseMov);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.commitSupplierMovement(updatedSup, supMov, caisseMov), "Mouvement usine validé");
      if (ok) close();
    });
  } else if (m.type === "caisse_mov") {
    document.getElementById("cm-save-btn")?.addEventListener("click", async () => {
      const amt = Math.max(0, Number(document.getElementById("cm-montant").value) || 0);
      if (amt <= 0) return showToast("Montant > 0 requis");
      const mov = {
        id: makeId("caisse"),
        date: nowIso(),
        type: m.movType,
        motif: document.getElementById("cm-motif").value.trim() || (m.movType === "entree" ? "Entrée diverse" : "Dépense"),
        montant: amt,
        auteur: getCurrentUser().name,
        referenceFacture: null,
      };
      if (state.mode === "local_preview") {
        state.caisseMovements.unshift(mov);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.createCaisseMovement(mov), "Mouvement caisse enregistré");
      if (ok) close();
    });
  } else if (m.type === "daily_count") {
    document.getElementById("dc-save-btn")?.addEventListener("click", async () => {
      const todayPrefix = new Date().toISOString().slice(0, 10);
      const fond = Number(document.getElementById("dc-fond").value) || 0;
      const reel = Number(document.getElementById("dc-reel").value) || 0;
      const ventes = round2(state.invoices.filter((i) => String(i.date).startsWith(todayPrefix)).reduce((s, i) => s + (Number(i.montantPaye) || 0), 0));
      const sorties = round2(state.caisseMovements.filter((x) => x.type === "sortie" && String(x.date).startsWith(todayPrefix)).reduce((s, x) => s + x.montant, 0));
      const theo = round2(fond + ventes - sorties);
      const cnt = {
        id: makeId("count"),
        date: nowIso(),
        magasin: state.settings.activeStore,
        auteur: getCurrentUser().name,
        fondOuverture: fond,
        ventesEspecesJour: ventes,
        entreesDiversJour: 0,
        sortiesJour: sorties,
        theorique: theo,
        compteReel: reel,
        ecart: round2(reel - theo),
        notes: document.getElementById("dc-notes").value.trim(),
      };
      if (state.mode === "local_preview") {
        state.dailyCaisseCounts.unshift(cnt);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.createDailyCaisseCount(cnt), "Clôture enregistrée");
      if (ok) close();
    });
  } else if (m.type === "user_edit") {
    document.getElementById("ue-save-btn")?.addEventListener("click", async () => {
      const role = document.getElementById("ue-role").value;
      const base = m.user || { id: makeId("u"), active: true };
      const u = {
        ...base,
        name: document.getElementById("ue-name").value.trim(),
        role,
        pin: document.getElementById("ue-pin").value.trim() || "0000",
        magasin: document.getElementById("ue-mag").value,
        phone: document.getElementById("ue-phone").value.trim(),
        canViewPrixAchat: document.getElementById("ue-p-achat").checked,
        canModifyPrices: document.getElementById("ue-p-prix").checked,
        canModifyStock: document.getElementById("ue-p-stock").checked,
        canDeleteInvoices: document.getElementById("ue-p-del").checked,
        canViewSuppliers: document.getElementById("ue-p-sup").checked,
        canViewCaisse: document.getElementById("ue-p-cai").checked,
        canEditInvoices: true,
        canManageUsers: role === "admin",
        allowedTabs: role === "admin" ? ALL_TABS : ALL_TABS.filter((t) => t !== "users"),
      };
      if (!u.name) return showToast("Nom obligatoire.");
      if (state.mode === "local_preview") {
        const idx = state.staffUsers.findIndex((x) => x.id === u.id);
        if (idx >= 0) state.staffUsers[idx] = u;
        else state.staffUsers.push(u);
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.upsertStaffUser(u), `Profil ${u.name} enregistré`);
      if (ok) close();
    });
  } else if (m.type === "policy_edit") {
    document.getElementById("rp-save-btn")?.addEventListener("click", async () => {
      const next = {
        ...m.policy,
        canViewPrixAchat: document.getElementById("rp-achat").checked,
        canModifyPrices: document.getElementById("rp-prix").checked,
        canModifyStock: document.getElementById("rp-stock").checked,
        canEditInvoices: document.getElementById("rp-edit").checked,
        canDeleteInvoices: document.getElementById("rp-del").checked,
        canViewSuppliers: document.getElementById("rp-sup").checked,
        canViewCaisse: document.getElementById("rp-cai").checked,
      };
      if (state.mode === "local_preview") {
        state.rolePolicies = state.rolePolicies.map((x) => (x.roleId === next.roleId ? next : x));
        close();
        render();
        return;
      }
      const ok = await runCloudWrite(() => syncClient.upsertRolePolicy(next), "Politique mise à jour");
      if (ok) close();
    });
  }
}

// --- Network status & Startup ---

window.addEventListener("online", () => {
  state.isOnline = true;
  render();
});
window.addEventListener("offline", () => {
  state.isOnline = false;
  render();
});

if (window.location.protocol !== "file:") {
  if (state.firebaseConfig) {
    bootFirebase(state.firebaseConfig);
  } else {
    render();
  }
}
