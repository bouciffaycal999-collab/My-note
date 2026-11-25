// Pure domain logic, constants, RBAC permissions, TileCalculatorMath, and
// firestore.rules-compliant payload builders shared between browser & Node tests.

export const FIREBASE_PROJECT_ID = "ace-project-498620";
export const FIRESTORE_DATABASE_ID = "ai-studio-android-82d1216f-37fc-468a-a33f-aa7c2903aaec";

export const SITES = ["Depot Remchi", "Magasin Ihab", "Magasin Zohir"];

export const ALL_TABS = [
  "pos",
  "stock",
  "invoices",
  "dashboard",
  "customers",
  "suppliers",
  "caisse",
  "users",
  "settings",
  "calculator",
];

export const MANAGER_TABS = ALL_TABS.filter((t) => t !== "users");
export const SELLER_TABS = ["pos", "stock", "invoices", "customers", "calculator"];
export const VALID_ROLES = ["admin", "gerant", "vendeur"];

export const DEFAULT_ROLE_POLICIES = [
  {
    roleId: "admin",
    displayName: "Admin",
    description: "Accès complet : stock, prix d'achat, marges, tarifs et gestion équipe",
    canViewPrixAchat: true,
    canModifyPrices: true,
    canModifyStock: true,
    canEditInvoices: true,
    canDeleteInvoices: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: true,
    allowedTabs: [...ALL_TABS],
  },
  {
    roleId: "gerant",
    displayName: "Manager (Owner & Gérant)",
    description: "Gestion commerciale : stock, prix d'achat, tarifs, caisse et usines",
    canViewPrixAchat: true,
    canModifyPrices: true,
    canModifyStock: true,
    canEditInvoices: true,
    canDeleteInvoices: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: false,
    allowedTabs: [...MANAGER_TABS],
  },
  {
    roleId: "vendeur",
    displayName: "Seller (Vendeur)",
    description: "Vente au comptoir & devis : prix d'achat masqué, tarifs et stock verrouillés",
    canViewPrixAchat: false,
    canModifyPrices: false,
    canModifyStock: false,
    canEditInvoices: true,
    canDeleteInvoices: false,
    canViewSuppliers: false,
    canViewCaisse: false,
    canManageUsers: false,
    allowedTabs: [...SELLER_TABS],
  },
];

export const DEFAULT_INVOICE_WA_RECIPIENTS = [
  { name: "Bureau / Kader (0676754124)", phone: "0676754124" },
];

export const DEFAULT_DISPO_WA_RECIPIENTS = [
  { name: "Bureau / Kader (0676754124)", phone: "0676754124" },
];

export const DEFAULT_STORE_INFO = {
  nom: "CERAMICA",
  nomArabe: "سيراميكا — بيع الخزف وبلاط الأرضيات",
  slogan: "Dalle de Sol, Faïence Murale, Parquet Céramique & Accessoires de Pose",
  adresse: "ZONE INDUSTRIELLE REMCHI المنطقة الصناعية",
  tel: "0676754124",
  tel2: "0676754124",
  email: "",
  rc: "16/00-1284560B24",
  nif: "002416012845690",
  nis: "002416012800145",
  ai: "16041285098",
  invoiceWaRecipients: DEFAULT_INVOICE_WA_RECIPIENTS,
  dispoWaRecipients: DEFAULT_DISPO_WA_RECIPIENTS,
};

export const DEFAULT_SETTINGS = {
  printFormat: "A4",
  a5Density: "compact",
  hideBainTeinteFacture: true,
  colorTheme: "ceramica_showroom",
  touchFriendlyMode: true,
  activeStore: "Depot Remchi",
  enableSoundAlerts: true,
  deviceSellerName: "",
  deviceSellerPhone: "",
};

export const DEFAULT_USERS = [
  {
    id: "u_kader",
    name: "Kader",
    role: "admin",
    pin: "1234",
    magasin: "Depot Remchi",
    allowedTabs: [...ALL_TABS],
    canEditInvoices: true,
    canDeleteInvoices: true,
    canModifyStock: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: true,
    active: true,
    phone: "0676754124",
    canViewPrixAchat: true,
    canModifyPrices: true,
  },
  {
    id: "u_walid",
    name: "Walid",
    role: "gerant",
    pin: "2222",
    magasin: "Depot Remchi",
    allowedTabs: [...MANAGER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: true,
    canModifyStock: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: true,
    canModifyPrices: true,
  },
  {
    id: "u_toufik",
    name: "Toufik",
    role: "gerant",
    pin: "3333",
    magasin: "Depot Remchi",
    allowedTabs: [...MANAGER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: true,
    canModifyStock: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: true,
    canModifyPrices: true,
  },
  {
    id: "u_ihab",
    name: "Ihab",
    role: "gerant",
    pin: "4444",
    magasin: "Magasin Ihab",
    allowedTabs: [...MANAGER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: true,
    canModifyStock: true,
    canViewSuppliers: true,
    canViewCaisse: true,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: true,
    canModifyPrices: true,
  },
  {
    id: "u_zohir",
    name: "Zohir",
    role: "vendeur",
    pin: "0001",
    magasin: "Magasin Zohir",
    allowedTabs: [...SELLER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: false,
    canModifyStock: false,
    canViewSuppliers: false,
    canViewCaisse: false,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: false,
    canModifyPrices: false,
  },
  {
    id: "u_celia",
    name: "Celia",
    role: "vendeur",
    pin: "0002",
    magasin: "Depot Remchi",
    allowedTabs: [...SELLER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: false,
    canModifyStock: false,
    canViewSuppliers: false,
    canViewCaisse: false,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: false,
    canModifyPrices: false,
  },
  {
    id: "u_alae",
    name: "Alae",
    role: "vendeur",
    pin: "0003",
    magasin: "Depot Remchi",
    allowedTabs: [...SELLER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: false,
    canModifyStock: false,
    canViewSuppliers: false,
    canViewCaisse: false,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: false,
    canModifyPrices: false,
  },
  {
    id: "u_amin",
    name: "Amine",
    role: "vendeur",
    pin: "0004",
    magasin: "Depot Remchi",
    allowedTabs: [...SELLER_TABS],
    canEditInvoices: true,
    canDeleteInvoices: false,
    canModifyStock: false,
    canViewSuppliers: false,
    canViewCaisse: false,
    canManageUsers: false,
    active: true,
    phone: "",
    canViewPrixAchat: false,
    canModifyPrices: false,
  },
];

export const DEFAULT_CUSTOMERS = [
  { id: "c1", nom: "Ahmed Benali", tel: "0550 12 34 56", adresse: "Remchi Centre", dette: 45000, totalAchats: 320000 },
  { id: "c2", nom: "Youcef Carreleur Professionnel", tel: "0661 88 99 00", adresse: "Tlemcen Mansourah", dette: 18500, totalAchats: 480000 },
  { id: "c3", nom: "Mohamed Chérif", tel: "0770 44 55 66", adresse: "Hennaya", dette: 0, totalAchats: 145000 },
  { id: "c4", nom: "Khaled Berrached", tel: "0555 90 80 70", adresse: "Ain Temouchent", dette: 62000, totalAchats: 750000 },
];

export const DEFAULT_SUPPLIERS = [
  { id: "f1", nom: "Sarl Usine BCRAM Céramique", contact: "Direction Commerciale", tel: "041 55 66 77", adresse: "Zone Industrielle Oran", usineRepresentee: "BCRAM", soldeDette: 320000, totalAchats: 1850000 },
  { id: "f2", nom: "NOVA Céramique Oran", contact: "M. Kaddour", tel: "0550 77 88 99", adresse: "Es-Sénia, Oran", usineRepresentee: "NOVA", soldeDette: 150000, totalAchats: 1200000 },
  { id: "f3", nom: "CEVICO Céramique Tlemcen", contact: "Service Vente Usine", tel: "043 22 33 44", adresse: "Zone Industrielle Remchi", usineRepresentee: "CEVICO", soldeDette: 0, totalAchats: 890000 },
  { id: "f4", nom: "Comptoir National Céramique NCM", contact: "M. Boumediene", tel: "0662 11 22 33", adresse: "Sidi Bel Abbès", usineRepresentee: "NCM", soldeDette: 85000, totalAchats: 650000 },
  { id: "f5", nom: "BATI Céramique Alger", contact: "Représentant Ouest", tel: "0771 33 44 55", adresse: "Rouiba, Alger", usineRepresentee: "BATI", soldeDette: 210000, totalAchats: 2400000 },
];

export const DIVERS_PRESETS = [
  { designation: "Colle Céramique C1 (25kg)", unite: "sac", prix: 750 },
  { designation: "Colle Porcelaine C2 Haute Adhérence (25kg)", unite: "sac", prix: 1250 },
  { designation: "Joint de Carrelage Hydrofuge (5kg)", unite: "sac", prix: 450 },
  { designation: "Croisillons 2mm (Paquet 200u)", unite: "u", prix: 600 },
  { designation: "Baguette Aluminium Chrome 2.5m", unite: "u", prix: 850 },
  { designation: "Forfait Livraison & Transport Camion", unite: "forfait", prix: 3000 },
  { designation: "Main d'Œuvre / Manutention Déchargement", unite: "forfait", prix: 2000 },
];

export const WHATSAPP_MANAGEMENT = "213676754124";
export const WHATSAPP_DEPOT = "213676754124";

export const round2 = (v) => Math.round((Number(v) || 0) * 100) / 100;
export const round4 = (v) => Math.round((Number(v) || 0) * 10000) / 10000;

export function formatDa(amount) {
  const n = Math.round(Number(amount) || 0);
  const formatted = n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
  return `${formatted} DA`;
}

export function formatNumber(amount) {
  const n = Math.round(Number(amount) || 0);
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

export function formatM2(m2) {
  const clean = Math.round((Number(m2) || 0) * 100) / 100;
  return String(clean);
}

export function formatDateTimeFr(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  return d.toLocaleString("fr-FR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function nowIso() {
  return new Date().toISOString();
}

export function sanitizeRuleId(raw, fallback = "doc_1") {
  const clean = String(raw || "")
    .replace(/[^a-zA-Z0-9_\-]/g, "_")
    .slice(0, 120);
  return clean.length > 0 ? clean : fallback;
}

export function makeId(prefix = "id") {
  const rand = Math.random().toString(36).slice(2, 8);
  return sanitizeRuleId(`${prefix}_${Date.now()}_${rand}`, `${prefix}_${Date.now()}`);
}

export function toDzWhatsAppDigits(rawPhone) {
  const digits = String(rawPhone || "").replace(/\D/g, "");
  if (!digits) return null;
  if (digits.startsWith("213")) return digits;
  if (digits.startsWith("0")) return "213" + digits.slice(1);
  return "213" + digits;
}

// --- Tile Calculator Math (mirrors TileCalculatorMath.kt) ---

export const TileCalculatorMath = {
  MARGIN_PRESETS: [0, 5, 10, 15],
  TILE_FORMAT_PRESETS: [
    { label: "60×60", lengthCm: 60, widthCm: 60, piecesPerCarton: 4 },
    { label: "45×45", lengthCm: 45, widthCm: 45, piecesPerCarton: 7 },
    { label: "30×60", lengthCm: 30, widthCm: 60, piecesPerCarton: 6 },
    { label: "80×80", lengthCm: 80, widthCm: 80, piecesPerCarton: 3 },
    { label: "120×60", lengthCm: 120, widthCm: 60, piecesPerCarton: 2 },
  ],

  calculateFromSurface(surfaceM2, marginPercent, m2PerCarton, pcsPerCarton, prixPerM2) {
    const cleanSurface = Math.max(0, Number(surfaceM2) || 0);
    const safeM2Ctn = Math.max(0.01, Number(m2PerCarton) || 1);
    const safePcsCtn = Math.max(1, Math.trunc(Number(pcsPerCarton) || 1));
    const cleanMargin = Math.min(50, Math.max(0, Math.trunc(Number(marginPercent) || 0)));
    const m2Piece = safeM2Ctn / safePcsCtn;
    const targetM2 = cleanSurface * (1 + cleanMargin / 100);
    const cartons = targetM2 <= 0.0001 ? 0 : Math.max(1, Math.ceil(targetM2 / safeM2Ctn - 1e-9));
    const delivered = round2(cartons * safeM2Ctn);
    const surplus = round2(Math.max(0, delivered - cleanSurface));
    const total = round2(delivered * Math.max(0, Number(prixPerM2) || 0));

    return {
      requestedM2: round2(cleanSurface),
      marginPercent: cleanMargin,
      targetM2WithMargin: round2(targetM2),
      cartons,
      loosePieces: 0,
      deliveredM2: delivered,
      surplusM2: surplus,
      totalDa: total,
      m2PerCarton: safeM2Ctn,
      pcsPerCarton: safePcsCtn,
      m2PerPiece: round2(m2Piece),
      prixParPiece: 0,
      piecesTotalDa: 0,
    };
  },

  calculateFromDimensions(lengthMeters, widthMeters, marginPercent, m2PerCarton, pcsPerCarton, prixPerM2) {
    const area = Math.max(0, Number(lengthMeters) || 0) * Math.max(0, Number(widthMeters) || 0);
    return this.calculateFromSurface(area, marginPercent, m2PerCarton, pcsPerCarton, prixPerM2);
  },

  calculateFromCartonsAndPieces(cartons, loosePieces, m2PerCarton, pcsPerCarton, prixPerM2, prixParPiece = 0) {
    const safeM2Ctn = Math.max(0.01, Number(m2PerCarton) || 1);
    const safePcsCtn = Math.max(1, Math.trunc(Number(pcsPerCarton) || 1));
    const cleanCartons = Math.max(0, Math.trunc(Number(cartons) || 0));
    const cleanPieces = Math.max(0, Math.trunc(Number(loosePieces) || 0));
    const cleanPrixM2 = Math.max(0, Number(prixPerM2) || 0);
    const cleanPrixPiece = Math.max(0, Number(prixParPiece) || 0);
    const m2Piece = safeM2Ctn / safePcsCtn;
    const cartonsM2 = cleanCartons * safeM2Ctn;
    const delivered = round2(cartonsM2 + cleanPieces * m2Piece);
    const usePieceCost = cleanPieces > 0 && cleanPrixPiece > 0;
    const piecesDa = usePieceCost ? round2(cleanPieces * cleanPrixPiece) : round2(cleanPieces * m2Piece * cleanPrixM2);
    const total = usePieceCost
      ? round2(cartonsM2 * cleanPrixM2 + cleanPieces * cleanPrixPiece)
      : round2(delivered * cleanPrixM2);

    return {
      requestedM2: delivered,
      marginPercent: 0,
      targetM2WithMargin: delivered,
      cartons: cleanCartons,
      loosePieces: cleanPieces,
      deliveredM2: delivered,
      surplusM2: 0,
      totalDa: total,
      m2PerCarton: safeM2Ctn,
      pcsPerCarton: safePcsCtn,
      m2PerPiece: round2(m2Piece),
      prixParPiece: cleanPrixPiece,
      piecesTotalDa: piecesDa,
    };
  },

  calculateCartItemTotals(cartons, pieces, m2PerCarton, pcsPerCarton, prixPerM2, prixParPiece = 0) {
    const res = this.calculateFromCartonsAndPieces(cartons, pieces, m2PerCarton, pcsPerCarton, prixPerM2, prixParPiece);
    return { m2Total: res.deliveredM2, sousTotal: res.totalDa };
  },

  calculateFromPiecesOnly(pieces, m2PerCarton, pcsPerCarton, prixParPiece, prixPerM2 = 0) {
    const safeM2Ctn = Math.max(0.01, Number(m2PerCarton) || 1);
    const safePcsCtn = Math.max(1, Math.trunc(Number(pcsPerCarton) || 1));
    const cleanPieces = Math.max(0, Math.trunc(Number(pieces) || 0));
    const m2Piece = safeM2Ctn / safePcsCtn;
    const delivered = round2(cleanPieces * m2Piece);
    const effPiecePrice = (Number(prixParPiece) || 0) > 0 ? Number(prixParPiece) : Math.max(0, Number(prixPerM2) || 0) * m2Piece;
    const piecesDa = round2(cleanPieces * effPiecePrice);
    return {
      requestedM2: delivered,
      marginPercent: 0,
      targetM2WithMargin: delivered,
      cartons: 0,
      loosePieces: cleanPieces,
      deliveredM2: delivered,
      surplusM2: 0,
      totalDa: piecesDa,
      m2PerCarton: safeM2Ctn,
      pcsPerCarton: safePcsCtn,
      m2PerPiece: round2(m2Piece),
      prixParPiece: effPiecePrice,
      piecesTotalDa: piecesDa,
    };
  },

  calculateRoomTilePlan(roomLengthM, roomWidthM, tileLength, tileWidth, tileUnit, m2PerCarton, marginPercent = 0) {
    const cleanRoomL = Math.max(0, Number(roomLengthM) || 0);
    const cleanRoomW = Math.max(0, Number(roomWidthM) || 0);
    const cleanTileL = Math.max(0, Number(tileLength) || 0);
    const cleanTileW = Math.max(0, Number(tileWidth) || 0);
    const cleanM2PerCarton = Math.max(0, Number(m2PerCarton) || 0);
    const cleanMargin = Math.min(50, Math.max(0, Math.trunc(Number(marginPercent) || 0)));

    const totalArea = cleanRoomL * cleanRoomW;
    const targetArea = totalArea * (1 + cleanMargin / 100);
    const tileLM = tileUnit === "cm" ? cleanTileL / 100 : cleanTileL;
    const tileWM = tileUnit === "cm" ? cleanTileW / 100 : cleanTileW;
    const singleTileArea = tileLM * tileWM;

    const tilesNeeded =
      singleTileArea <= 0.00001 || targetArea <= 0.00001
        ? 0
        : Math.max(1, Math.ceil(targetArea / singleTileArea - 1e-9));

    const cartonsNeeded =
      cleanM2PerCarton <= 0.00001 || targetArea <= 0.00001
        ? 0
        : Math.max(1, Math.ceil(targetArea / cleanM2PerCarton - 1e-9));

    const deliveredArea = cartonsNeeded * cleanM2PerCarton;
    const surplusArea = Math.max(0, deliveredArea - totalArea);

    return {
      roomLengthM: round2(cleanRoomL),
      roomWidthM: round2(cleanRoomW),
      totalAreaM2: round2(totalArea),
      marginPercent: cleanMargin,
      targetAreaWithMarginM2: round2(targetArea),
      tileAreaM2: round4(singleTileArea),
      tilesNeeded,
      m2PerCarton: round4(cleanM2PerCarton),
      cartonsNeeded,
      deliveredAreaM2: round2(deliveredArea),
      surplusAreaM2: round2(surplusArea),
    };
  },
};

// --- RBAC Permissions (mirrors PosSeeds.resolveEffectivePermissions) ---

export function defaultPolicyForRole(role) {
  return DEFAULT_ROLE_POLICIES.find((p) => p.roleId === role) || DEFAULT_ROLE_POLICIES[2];
}

export function resolveEffectivePermissions(user, policies = DEFAULT_ROLE_POLICIES) {
  if (!user) {
    return {
      roleId: "vendeur",
      canViewPrixAchat: false,
      canModifyPrices: false,
      canModifyStock: false,
      canEditInvoices: false,
      canDeleteInvoices: false,
      canViewSuppliers: false,
      canViewCaisse: false,
      canManageUsers: false,
      canHideInvoicePrices: false,
      allowedTabs: ["pos", "calculator"],
    };
  }
  const policy = (policies || []).find((p) => p.roleId === user.role) || defaultPolicyForRole(user.role);
  if (user.role === "admin") {
    return {
      roleId: "admin",
      canViewPrixAchat: true,
      canModifyPrices: true,
      canModifyStock: true,
      canEditInvoices: true,
      canDeleteInvoices: true,
      canViewSuppliers: true,
      canViewCaisse: true,
      canManageUsers: true,
      canHideInvoicePrices: true,
      allowedTabs: [...ALL_TABS],
    };
  }
  const policyTabs =
    policy.allowedTabs && policy.allowedTabs.length > 0
      ? policy.allowedTabs
      : user.role === "gerant"
      ? MANAGER_TABS
      : SELLER_TABS;
  const baseUserTabs = user.allowedTabs && user.allowedTabs.length > 0 ? user.allowedTabs : policyTabs;
  const filtered = baseUserTabs.filter((t) => policyTabs.includes(t));
  const resolvedTabs = Array.from(new Set([...filtered, "calculator"]));
  const canViewCost = Boolean(policy.canViewPrixAchat) && Boolean(user.canViewPrixAchat);

  return {
    roleId: user.role,
    canViewPrixAchat: canViewCost,
    canModifyPrices: Boolean(policy.canModifyPrices) && Boolean(user.canModifyPrices),
    canModifyStock: Boolean(policy.canModifyStock) && Boolean(user.canModifyStock),
    canEditInvoices: Boolean(policy.canEditInvoices) && Boolean(user.canEditInvoices),
    canDeleteInvoices: Boolean(policy.canDeleteInvoices) && Boolean(user.canDeleteInvoices),
    canViewSuppliers: Boolean(policy.canViewSuppliers) && Boolean(user.canViewSuppliers),
    canViewCaisse: Boolean(policy.canViewCaisse) && Boolean(user.canViewCaisse),
    canManageUsers: Boolean(policy.canManageUsers) && Boolean(user.canManageUsers),
    canHideInvoicePrices: canViewCost,
    allowedTabs: resolvedTabs.length > 0 ? resolvedTabs : ["pos", "calculator"],
  };
}

// --- Firebase Web Config Paste Parser ---

export function parseFirebaseConfigInput(rawText) {
  const text = String(rawText || "").trim();
  if (!text) return { ok: false, error: "Collez la configuration Firebase Web." };

  let objText = text;
  const braceStart = text.indexOf("{");
  const braceEnd = text.lastIndexOf("}");
  if (braceStart !== -1 && braceEnd > braceStart) {
    objText = text.slice(braceStart, braceEnd + 1);
  }

  let parsed = null;
  try {
    parsed = JSON.parse(objText);
  } catch {
    // Support JS object literal format copied directly from Firebase Console
    try {
      const jsonified = objText
        .replace(/\/\/.*$/gm, "")
        .replace(/\/\*[\s\S]*?\*\//g, "")
        .replace(/,\s*([}\]])/g, "$1")
        .replace(/([{,]\s*)([a-zA-Z0-9_]+)\s*:/g, '$1"$2":');
      parsed = JSON.parse(jsonified);
    } catch {
      return {
        ok: false,
        error: "Format invalide. Copiez le bloc firebaseConfig = { ... } depuis Firebase Console.",
      };
    }
  }

  if (!parsed || typeof parsed !== "object") {
    return { ok: false, error: "Configuration vide ou invalide." };
  }

  const required = ["apiKey", "authDomain", "projectId"];
  const missing = required.filter((k) => !parsed[k] || String(parsed[k]).trim() === "");
  if (missing.length > 0) {
    return { ok: false, error: `Champs manquants : ${missing.join(", ")}` };
  }

  const config = {
    apiKey: String(parsed.apiKey).trim(),
    authDomain: String(parsed.authDomain).trim(),
    projectId: String(parsed.projectId).trim(),
    storageBucket: parsed.storageBucket ? String(parsed.storageBucket).trim() : "",
    messagingSenderId: parsed.messagingSenderId ? String(parsed.messagingSenderId).trim() : "",
    appId: parsed.appId ? String(parsed.appId).trim() : "",
  };

  return {
    ok: true,
    config,
    projectMismatch: config.projectId !== FIREBASE_PROJECT_ID,
  };
}

// --- Strict firestore.rules Payload Builders ---

const clip = (v, max) => String(v ?? "").slice(0, max);
const clipNonEmpty = (v, max, fallback) => {
  const s = clip(v, max).trim();
  return s.length > 0 ? s : clip(fallback, max);
};
const nonNegNum = (v) => Math.max(0, Number(v) || 0);
const numOrZero = (v) => (Number.isFinite(Number(v)) ? Number(v) : 0);

export function buildUserAccountPayload(userId, email, displayName, serverTs, existingCreatedAt = null) {
  const base = {
    userId: clipNonEmpty(userId, 128, "user"),
    email: email == null ? null : clip(email, 200),
    displayName: displayName == null ? null : clip(displayName, 120),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
  return base;
}

export function buildProductPayload(product, serverTs, existingCreatedAt = null) {
  return {
    name: clipNonEmpty(product.name, 160, "Article Sans Nom"),
    usine: clip(product.usine, 80),
    dimension: clip(product.dimension, 40),
    m2Carton: Math.max(0.01, Number(product.m2Carton ?? product.m2_carton) || 1),
    pcs: Math.max(1, Math.trunc(Number(product.pcs) || 1)),
    prix: nonNegNum(product.prix),
    prixAchat: nonNegNum(product.prixAchat),
    stock: Math.trunc(Number(product.stock) || 0),
    color: clip(product.color || "#94a3b8", 32),
    colorName: clip(product.colorName || "Gris Minéral", 80),
    image: clip(product.image || "", 500),
    finition: clip(product.finition || "brillant", 40),
    choix: clip(product.choix || "1er Choix", 40),
    categorie: clip(product.categorie || "sol", 40),
    description: clip(product.description || "", 500),
    pricingUnit: product.pricingUnit === "piece" ? "piece" : "carton",
    prixPiece: nonNegNum(product.prixPiece),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildInvoicePayload(invoice, serverTs, existingCreatedAt = null) {
  const validStatut = ["Payé", "Partiel", "Crédit"].includes(invoice.statut) ? invoice.statut : "Payé";
  return {
    reference: clipNonEmpty(invoice.reference, 60, `CRM-${Date.now()}`),
    date: clipNonEmpty(invoice.date, 60, nowIso()),
    vendeur: clip(invoice.vendeur, 80),
    vendeurTel: clip(invoice.vendeurTel || "", 40),
    magasin: clip(invoice.magasin || "Depot Remchi", 80),
    client: {
      id: invoice.client?.id ? clip(invoice.client.id, 128) : null,
      nom: clipNonEmpty(invoice.client?.nom, 120, "Client Comptoir"),
      tel: clip(invoice.client?.tel || "", 40),
      adresse: clip(invoice.client?.adresse || "", 200),
    },
    articles: (invoice.articles || []).slice(0, 200).map((a) => ({
      id: clip(a.id || makeId("ci"), 128),
      productId: clip(a.productId, 128),
      name: clip(a.name, 160),
      usine: clip(a.usine, 80),
      dimension: clip(a.dimension, 40),
      m2Carton: Math.max(0.01, Number(a.m2Carton) || 1),
      pcs: Math.max(1, Math.trunc(Number(a.pcs) || 1)),
      prixUnitaire: nonNegNum(a.prixUnitaire),
      prixAchat: nonNegNum(a.prixAchat),
      cartons: Math.max(0, Math.trunc(Number(a.cartons) || 0)),
      pieces: Math.max(0, Math.trunc(Number(a.pieces) || 0)),
      m2Total: nonNegNum(a.m2Total),
      sousTotal: nonNegNum(a.sousTotal),
      bain: clip(a.bain || "", 40),
      calibre: clip(a.calibre || "", 40),
      color: clip(a.color || "#94a3b8", 32),
      colorName: clip(a.colorName || "", 80),
      image: clip(a.image || "", 500),
      prixPiece: nonNegNum(a.prixPiece),
      pricingUnit: a.pricingUnit === "piece" ? "piece" : "carton",
    })),
    lignesLibres: (invoice.lignesLibres || []).slice(0, 100).map((l) => ({
      id: clip(l.id || makeId("fl"), 128),
      designation: clip(l.designation, 160),
      quantite: nonNegNum(l.quantite),
      unite: clip(l.unite || "u", 24),
      prixUnitaire: nonNegNum(l.prixUnitaire),
      sousTotal: nonNegNum(l.sousTotal),
    })),
    totalBrut: nonNegNum(invoice.totalBrut),
    remise: nonNegNum(invoice.remise),
    totalNet: nonNegNum(invoice.totalNet),
    montantPaye: nonNegNum(invoice.montantPaye),
    resteAPayer: nonNegNum(invoice.resteAPayer),
    modePaiement: clip(invoice.modePaiement || "Espèces", 40),
    statut: validStatut,
    notes: clip(invoice.notes || "", 500),
    hidePrices: Boolean(invoice.hidePrices),
    whatsappSentBeforePrint: Boolean(invoice.whatsappSentBeforePrint),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildCustomerPayload(customer, serverTs, existingCreatedAt = null) {
  return {
    nom: clipNonEmpty(customer.nom, 120, "Client"),
    tel: clip(customer.tel || "", 40),
    adresse: clip(customer.adresse || "", 200),
    dette: nonNegNum(customer.dette),
    totalAchats: nonNegNum(customer.totalAchats),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildSupplierPayload(supplier, serverTs, existingCreatedAt = null) {
  return {
    nom: clipNonEmpty(supplier.nom, 140, "Fournisseur"),
    contact: clip(supplier.contact || "", 120),
    tel: clip(supplier.tel || "", 40),
    adresse: clip(supplier.adresse || "", 200),
    usineRepresentee: clip(supplier.usineRepresentee || "", 80),
    soldeDette: nonNegNum(supplier.soldeDette),
    totalAchats: nonNegNum(supplier.totalAchats),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildSupplierMovementPayload(mov, serverTs, existingCreatedAt = null) {
  return {
    supplierId: sanitizeRuleId(mov.supplierId, "f1"),
    date: clipNonEmpty(mov.date, 60, nowIso()),
    type: mov.type === "achat" ? "achat" : "versement",
    montant: Math.max(0.01, Number(mov.montant) || 0.01),
    motif: clip(mov.motif || "", 300),
    pieceJointeRef: mov.pieceJointeRef ? clip(mov.pieceJointeRef, 120) : null,
    auteur: clip(mov.auteur || "Gérant", 80),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildCaisseMovementPayload(mov, serverTs, existingCreatedAt = null) {
  return {
    date: clipNonEmpty(mov.date, 60, nowIso()),
    type: mov.type === "sortie" ? "sortie" : "entree",
    motif: clip(mov.motif || "", 300),
    montant: Math.max(0.01, Number(mov.montant) || 0.01),
    auteur: clip(mov.auteur || "Gérant", 80),
    referenceFacture: mov.referenceFacture ? clip(mov.referenceFacture, 120) : null,
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildDailyCaisseCountPayload(cnt, serverTs, existingCreatedAt = null) {
  return {
    date: clipNonEmpty(cnt.date, 60, nowIso()),
    magasin: clip(cnt.magasin || "Depot Remchi", 80),
    auteur: clip(cnt.auteur || "Gérant", 80),
    fondOuverture: numOrZero(cnt.fondOuverture),
    ventesEspecesJour: numOrZero(cnt.ventesEspecesJour),
    entreesDiversJour: numOrZero(cnt.entreesDiversJour),
    sortiesJour: numOrZero(cnt.sortiesJour),
    theorique: numOrZero(cnt.theorique),
    compteReel: numOrZero(cnt.compteReel),
    ecart: numOrZero(cnt.ecart),
    notes: clip(cnt.notes || "", 500),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildStaffUserPayload(user, serverTs, existingCreatedAt = null) {
  const role = VALID_ROLES.includes(user.role) ? user.role : "vendeur";
  const isAdmin = role === "admin";
  return {
    name: clipNonEmpty(user.name, 100, "Utilisateur"),
    role,
    pin: clipNonEmpty(user.pin, 12, "0000"),
    magasin: clip(user.magasin || "Depot Remchi", 80),
    allowedTabs: (isAdmin ? ALL_TABS : user.allowedTabs || SELLER_TABS).slice(0, 20).map((t) => String(t)),
    canEditInvoices: isAdmin ? true : Boolean(user.canEditInvoices),
    canDeleteInvoices: isAdmin ? true : Boolean(user.canDeleteInvoices),
    canModifyStock: isAdmin ? true : Boolean(user.canModifyStock),
    canViewSuppliers: isAdmin ? true : Boolean(user.canViewSuppliers),
    canViewCaisse: isAdmin ? true : Boolean(user.canViewCaisse),
    canManageUsers: isAdmin ? true : Boolean(user.canManageUsers),
    active: user.active !== false,
    phone: clip(user.phone || "", 40),
    canViewPrixAchat: isAdmin ? true : Boolean(user.canViewPrixAchat),
    canModifyPrices: isAdmin ? true : Boolean(user.canModifyPrices),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildRolePolicyPayload(policy, serverTs, existingCreatedAt = null) {
  const roleId = VALID_ROLES.includes(policy.roleId) ? policy.roleId : "vendeur";
  const isAdmin = roleId === "admin";
  return {
    roleId,
    displayName: clipNonEmpty(policy.displayName, 80, roleId),
    description: clip(policy.description || "", 300),
    canViewPrixAchat: isAdmin ? true : Boolean(policy.canViewPrixAchat),
    canModifyPrices: isAdmin ? true : Boolean(policy.canModifyPrices),
    canModifyStock: isAdmin ? true : Boolean(policy.canModifyStock),
    canEditInvoices: isAdmin ? true : Boolean(policy.canEditInvoices),
    canDeleteInvoices: isAdmin ? true : Boolean(policy.canDeleteInvoices),
    canViewSuppliers: isAdmin ? true : Boolean(policy.canViewSuppliers),
    canViewCaisse: isAdmin ? true : Boolean(policy.canViewCaisse),
    canManageUsers: isAdmin ? true : Boolean(policy.canManageUsers),
    allowedTabs: (isAdmin ? ALL_TABS : policy.allowedTabs || SELLER_TABS).slice(0, 20).map((t) => String(t)),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildDepotNotificationPayload(notif, serverTs, existingCreatedAt = null) {
  return {
    invoiceId: sanitizeRuleId(notif.invoiceId, "inv_0"),
    reference: clipNonEmpty(notif.reference, 60, "[INFO]"),
    date: clipNonEmpty(notif.date, 60, nowIso()),
    vendeur: clip(notif.vendeur || "", 80),
    magasin: clip(notif.magasin || "", 80),
    client: clip(notif.client || "", 120),
    clientTel: clip(notif.clientTel || "", 40),
    totalNet: nonNegNum(notif.totalNet),
    m2Total: nonNegNum(notif.m2Total),
    cartonsTotal: Math.max(0, Math.trunc(Number(notif.cartonsTotal) || 0)),
    articlesSummary: clip(notif.articlesSummary || "", 1000),
    read: Boolean(notif.read),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildStoreInfoPayload(info, serverTs, existingCreatedAt = null) {
  const mapRecipients = (list) =>
    (list || [])
      .slice(0, 20)
      .map((r) => ({ name: clip(r.name || "", 80), phone: clip(r.phone || "", 40) }));
  return {
    nom: clipNonEmpty(info.nom, 120, "CERAMICA"),
    nomArabe: clip(info.nomArabe || "", 160),
    slogan: clip(info.slogan || "", 200),
    adresse: clip(info.adresse || "", 200),
    tel: clip(info.tel || "", 40),
    tel2: clip(info.tel2 || "", 40),
    email: clip(info.email || "", 160),
    rc: clip(info.rc || "", 80),
    nif: clip(info.nif || "", 80),
    nis: clip(info.nis || "", 80),
    ai: clip(info.ai || "", 80),
    invoiceWaRecipients: mapRecipients(info.invoiceWaRecipients || DEFAULT_INVOICE_WA_RECIPIENTS),
    dispoWaRecipients: mapRecipients(info.dispoWaRecipients || DEFAULT_DISPO_WA_RECIPIENTS),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

export function buildAppSettingsPayload(settings, serverTs, existingCreatedAt = null) {
  return {
    printFormat: clipNonEmpty(settings.printFormat, 16, "A4"),
    a5Density: clipNonEmpty(settings.a5Density, 24, "compact"),
    hideBainTeinteFacture: Boolean(settings.hideBainTeinteFacture ?? true),
    colorTheme: clipNonEmpty(settings.colorTheme, 40, "ceramica_showroom"),
    touchFriendlyMode: Boolean(settings.touchFriendlyMode ?? true),
    activeStore: clipNonEmpty(settings.activeStore, 80, "Depot Remchi"),
    enableSoundAlerts: Boolean(settings.enableSoundAlerts ?? true),
    deviceSellerName: clip(settings.deviceSellerName || "", 80),
    deviceSellerPhone: clip(settings.deviceSellerPhone || "", 40),
    createdAt: existingCreatedAt || serverTs,
    updatedAt: serverTs,
  };
}

// --- Transaction Plan Builders (mirrors PosRepository & PosViewModel) ---

export function itemStockCartons(item) {
  const c = Math.trunc(Number(item.cartons) || 0);
  if (c > 0) return c;
  const p = Math.trunc(Number(item.pieces) || 0);
  if (p > 0) {
    return Math.max(1, Math.ceil(p / Math.max(1, Math.trunc(Number(item.pcs) || 1))));
  }
  return 0;
}

export function computeSaleCommitPlan({
  cart,
  freeLines,
  client,
  remise,
  montantPaye,
  modePaiement,
  notes,
  editingInvoice = null,
  existingInvoicesCount = 0,
  sellerName = "Vendeur",
  sellerPhone = "",
  magasin = "Depot Remchi",
  canModifyPrices = true,
  products = [],
  customers = [],
}) {
  const articles = cart || [];
  const libres = freeLines || [];
  const totalBrut = round2(
    articles.reduce((s, a) => s + nonNegNum(a.sousTotal), 0) +
      libres.reduce((s, l) => s + nonNegNum(l.sousTotal), 0)
  );
  const requestedRemise = canModifyPrices ? nonNegNum(remise) : nonNegNum(editingInvoice?.remise);
  const cleanRemise = Math.min(totalBrut, Math.max(0, requestedRemise));
  const totalNet = round2(Math.max(0, totalBrut - cleanRemise));
  const cleanPaye =
    modePaiement === "Crédit" ? 0 : Math.min(totalNet, Math.max(0, nonNegNum(montantPaye)));
  const reste = round2(Math.max(0, totalNet - cleanPaye));
  const statut = reste <= 0.01 ? "Payé" : cleanPaye > 0 ? "Partiel" : "Crédit";

  const d = new Date();
  const dateCode =
    String(d.getFullYear()).slice(-2) +
    String(d.getMonth() + 1).padStart(2, "0") +
    String(d.getDate()).padStart(2, "0");
  const seq = String(existingInvoicesCount + 1).padStart(3, "0");
  const now = nowIso();

  const invoice = editingInvoice
    ? {
        ...editingInvoice,
        vendeur: editingInvoice.vendeur || sellerName,
        vendeurTel: editingInvoice.vendeurTel || sellerPhone,
        client,
        articles,
        lignesLibres: libres,
        totalBrut,
        remise: cleanRemise,
        totalNet,
        montantPaye: cleanPaye,
        resteAPayer: reste,
        modePaiement,
        statut,
        notes: notes || "",
      }
    : {
        id: makeId("inv"),
        reference: `CRM-${dateCode}-${seq}`,
        date: now,
        vendeur: sellerName,
        vendeurTel: sellerPhone,
        magasin,
        client,
        articles,
        lignesLibres: libres,
        totalBrut,
        remise: cleanRemise,
        totalNet,
        montantPaye: cleanPaye,
        resteAPayer: reste,
        modePaiement,
        statut,
        notes: notes || "",
        hidePrices: false,
        whatsappSentBeforePrint: false,
      };

  // Compute stock deltas
  const stockDeltas = {};
  if (editingInvoice) {
    for (const oldItem of editingInvoice.articles || []) {
      const c = itemStockCartons(oldItem);
      if (oldItem.productId && c > 0) {
        stockDeltas[oldItem.productId] = (stockDeltas[oldItem.productId] || 0) + c;
      }
    }
  }
  for (const newItem of articles) {
    const c = itemStockCartons(newItem);
    if (newItem.productId && c > 0) {
      stockDeltas[newItem.productId] = (stockDeltas[newItem.productId] || 0) - c;
    }
  }

  // Updated customer
  let updatedCustomer = null;
  if (client?.id) {
    const existingCust = customers.find((c) => c.id === client.id);
    if (existingCust) {
      if (editingInvoice && editingInvoice.client?.id === client.id) {
        const debtDelta = invoice.resteAPayer - nonNegNum(editingInvoice.resteAPayer);
        const totalDelta = invoice.totalNet - nonNegNum(editingInvoice.totalNet);
        updatedCustomer = {
          ...existingCust,
          dette: round2(Math.max(0, nonNegNum(existingCust.dette) + debtDelta)),
          totalAchats: round2(Math.max(0, nonNegNum(existingCust.totalAchats) + totalDelta)),
        };
      } else {
        updatedCustomer = {
          ...existingCust,
          dette: round2(nonNegNum(existingCust.dette) + invoice.resteAPayer),
          totalAchats: round2(nonNegNum(existingCust.totalAchats) + invoice.totalNet),
        };
      }
    }
  }

  // Caisse movement on new sale if paid > 0 (or delta payment on edit)
  let caisseMovement = null;
  const paymentDelta = editingInvoice
    ? round2(invoice.montantPaye - nonNegNum(editingInvoice.montantPaye))
    : invoice.montantPaye;
  if (paymentDelta > 0) {
    caisseMovement = {
      id: makeId("caisse"),
      date: now,
      type: "entree",
      motif: editingInvoice
        ? `Complément Facture #${invoice.reference} (${invoice.client.nom})`
        : `Vente Facture #${invoice.reference} (${invoice.client.nom})`,
      montant: paymentDelta,
      auteur: invoice.vendeur || sellerName,
      referenceFacture: invoice.reference,
    };
  }

  const m2Total = round2(articles.reduce((s, a) => s + nonNegNum(a.m2Total), 0));
  const cartonsTotal = articles.reduce((s, a) => s + Math.max(0, Math.trunc(Number(a.cartons) || 0)), 0);
  const summaryParts = [
    ...articles.map((a) => `${a.name} (${a.cartons} ctn / ${formatM2(a.m2Total)} m²)`),
    ...libres.map((l) => `${l.designation} (x${l.quantite})`),
  ];

  const mainNotification = {
    id: makeId("notif"),
    invoiceId: invoice.id,
    reference: (editingInvoice ? `[MODIF] ${invoice.reference}` : `[VENTE] ${invoice.reference}`).slice(0, 60),
    date: now,
    vendeur: invoice.vendeur,
    magasin: invoice.magasin,
    client: invoice.client.nom,
    clientTel: invoice.client.tel || "",
    totalNet: invoice.totalNet,
    m2Total,
    cartonsTotal,
    articlesSummary: summaryParts.join(" • ").slice(0, 980),
    read: false,
  };

  const extraNotifications = [];
  for (const [prodId, delta] of Object.entries(stockDeltas)) {
    if (delta < 0) {
      const prod = products.find((p) => p.id === prodId);
      if (prod) {
        const nextStock = Math.trunc(Number(prod.stock) || 0) + delta;
        if (nextStock <= 5) {
          const statusText =
            nextStock <= 0
              ? `RUPTURE DE STOCK (${nextStock} ctn)`
              : `STOCK CRITIQUE (${nextStock} ctn restants)`;
          extraNotifications.push({
            id: makeId("notif"),
            invoiceId: sanitizeRuleId(prod.id, "alert_prod"),
            reference: `[ALERTE] ${prod.name}`.slice(0, 60),
            date: now,
            vendeur: sellerName,
            magasin,
            client: `${prod.usine} • ${prod.dimension}`.slice(0, 120),
            clientTel: "",
            totalNet: nonNegNum(prod.prix),
            m2Total: Math.max(0, nextStock) * Math.max(0.01, Number(prod.m2Carton) || 1),
            cartonsTotal: Math.max(0, nextStock),
            articlesSummary: `${statusText} — ${prod.name} (${prod.usine})`.slice(0, 980),
            read: false,
          });
        }
      }
    }
  }

  return {
    invoice,
    stockDeltas,
    updatedCustomer,
    caisseMovement,
    notifications: [mainNotification, ...extraNotifications],
    isEdit: Boolean(editingInvoice),
  };
}

export function computeDeleteInvoicePlan({ invoice, actorName = "Admin", customers = [] }) {
  const restoredStockDeltas = {};
  for (const item of invoice.articles || []) {
    const c = itemStockCartons(item);
    if (item.productId && c > 0) {
      restoredStockDeltas[item.productId] = (restoredStockDeltas[item.productId] || 0) + c;
    }
  }
  let updatedCustomer = null;
  if (invoice.client?.id) {
    const existingCust = customers.find((c) => c.id === invoice.client.id);
    if (existingCust) {
      updatedCustomer = {
        ...existingCust,
        dette: round2(Math.max(0, nonNegNum(existingCust.dette) - nonNegNum(invoice.resteAPayer))),
        totalAchats: round2(Math.max(0, nonNegNum(existingCust.totalAchats) - nonNegNum(invoice.totalNet))),
      };
    }
  }
  const m2Total = round2((invoice.articles || []).reduce((s, a) => s + nonNegNum(a.m2Total), 0));
  const cartonsTotal = (invoice.articles || []).reduce(
    (s, a) => s + Math.max(0, Math.trunc(Number(a.cartons) || 0)),
    0
  );
  const notification = {
    id: makeId("notif"),
    invoiceId: sanitizeRuleId(invoice.id, "del_inv"),
    reference: `[SUPPR] ${invoice.reference}`.slice(0, 60),
    date: nowIso(),
    vendeur: String(actorName || "Admin").slice(0, 80),
    magasin: String(invoice.magasin || "").slice(0, 80),
    client: String(invoice.client?.nom || "").slice(0, 120),
    clientTel: String(invoice.client?.tel || "").slice(0, 40),
    totalNet: nonNegNum(invoice.totalNet),
    m2Total,
    cartonsTotal,
    articlesSummary: `Facture #${invoice.reference} supprimée par ${actorName} (Stock restitué)`.slice(0, 980),
    read: false,
  };
  return { restoredStockDeltas, updatedCustomer, notification };
}
