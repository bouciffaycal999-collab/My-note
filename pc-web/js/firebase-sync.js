import { initializeApp, getApps, deleteApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
  signOut,
  onAuthStateChanged,
  connectAuthEmulator,
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-auth.js";
import {
  getFirestore,
  initializeFirestore,
  persistentLocalCache,
  persistentMultipleTabManager,
  connectFirestoreEmulator,
  collection,
  doc,
  getDoc,
  getDocs,
  limit,
  query,
  setDoc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  writeBatch,
  increment,
  serverTimestamp,
  waitForPendingWrites,
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-firestore.js";

import {
  FIRESTORE_DATABASE_ID,
  DEFAULT_ROLE_POLICIES,
  DEFAULT_USERS,
  DEFAULT_CUSTOMERS,
  DEFAULT_SUPPLIERS,
  DEFAULT_STORE_INFO,
  DEFAULT_SETTINGS,
  buildUserAccountPayload,
  buildProductPayload,
  buildInvoicePayload,
  buildCustomerPayload,
  buildSupplierPayload,
  buildSupplierMovementPayload,
  buildCaisseMovementPayload,
  buildDailyCaisseCountPayload,
  buildStaffUserPayload,
  buildRolePolicyPayload,
  buildDepotNotificationPayload,
  buildStoreInfoPayload,
  buildAppSettingsPayload,
  nowIso,
} from "./pos-domain.js";

const STORAGE_CONFIG_KEY = "ceramica_pc_firebase_config_v1";

export function loadSavedFirebaseConfig() {
  try {
    const raw = localStorage.getItem(STORAGE_CONFIG_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed && parsed.apiKey && parsed.authDomain && parsed.projectId) {
      return parsed;
    }
  } catch {
    // ignore corrupt localStorage
  }
  return null;
}

export function saveFirebaseConfig(config) {
  localStorage.setItem(STORAGE_CONFIG_KEY, JSON.stringify(config));
}

export function clearFirebaseConfig() {
  localStorage.removeItem(STORAGE_CONFIG_KEY);
}

export function playAlertChime() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    const now = ctx.currentTime;
    [880, 1174.66].forEach((freq, idx) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(freq, now + idx * 0.12);
      gain.gain.setValueAtTime(0.001, now + idx * 0.12);
      gain.gain.exponentialRampToValueAtTime(0.14, now + idx * 0.12 + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + idx * 0.12 + 0.24);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now + idx * 0.12);
      osc.stop(now + idx * 0.12 + 0.26);
    });
  } catch {
    // ignore audio autoplay restriction
  }
}

export function showBrowserDesktopNotification(notif) {
  try {
    if (!("Notification" in window) || Notification.permission !== "granted") return;
    new Notification(`CERAMICA — ${notif.reference}`, {
      body: `${notif.vendeur} (${notif.magasin}) • ${notif.client}\n${notif.articlesSummary || ""}`,
      tag: notif.id,
    });
  } catch {
    // ignore browser notification failure
  }
}

export class FirebasePosSyncClient {
  constructor() {
    this.app = null;
    this.auth = null;
    this.db = null;
    this.uid = null;
    this.unsubscribers = [];
    this.createdAtCache = new Map();
    this.bootstrappedUid = null;
  }

  async init(firebaseConfig, { useEmulator = false } = {}) {
    this.stopListeners();
    for (const existing of getApps()) {
      await deleteApp(existing);
    }

    this.app = initializeApp(firebaseConfig);
    this.auth = getAuth(this.app);

    try {
      this.db = initializeFirestore(
        this.app,
        {
          localCache: persistentLocalCache({
            tabManager: persistentMultipleTabManager(),
          }),
        },
        FIRESTORE_DATABASE_ID
      );
    } catch {
      this.db = getFirestore(this.app, FIRESTORE_DATABASE_ID);
    }

    if (useEmulator) {
      connectAuthEmulator(this.auth, "http://127.0.0.1:9099", { disableWarnings: true });
      connectFirestoreEmulator(this.db, "127.0.0.1", 8085);
    }

    // Handle redirect fallback if popup had fallen back to redirect
    try {
      await getRedirectResult(this.auth);
    } catch {
      // ignore if no pending redirect
    }
  }

  onAuthChange(cb) {
    if (!this.auth) return () => {};
    return onAuthStateChanged(this.auth, (user) => {
      this.uid = user ? user.uid : null;
      cb(user);
    });
  }

  async signInWithGoogle() {
    if (!this.auth) throw new Error("Firebase non initialisé.");
    const provider = new GoogleAuthProvider();
    provider.setCustomParameters({ prompt: "select_account" });
    try {
      const cred = await signInWithPopup(this.auth, provider);
      return cred.user;
    } catch (err) {
      if (err?.code === "auth/popup-blocked") {
        await signInWithRedirect(this.auth, provider);
        return null;
      }
      throw new Error(translateFirebaseError(err));
    }
  }

  async signOutUser() {
    this.stopListeners();
    this.bootstrappedUid = null;
    if (this.auth) {
      await signOut(this.auth);
    }
  }

  userDocRef(uid = this.uid) {
    return doc(this.db, "users", uid);
  }

  colRef(name, uid = this.uid) {
    return collection(this.db, "users", uid, name);
  }

  itemRef(name, id, uid = this.uid) {
    return doc(this.db, "users", uid, name, id);
  }

  cacheCreatedAt(path, data) {
    if (data && data.createdAt) {
      this.createdAtCache.set(path, data.createdAt);
    }
  }

  async resolveCreatedAt(ref) {
    if (this.createdAtCache.has(ref.path)) {
      return this.createdAtCache.get(ref.path);
    }
    try {
      const snap = await getDoc(ref);
      if (snap.exists()) {
        const ts = snap.data()?.createdAt || null;
        if (ts) this.createdAtCache.set(ref.path, ts);
        return ts;
      }
    } catch {
      // ignore
    }
    return null;
  }

  async ensureAccountAndSeedIfEmpty(user) {
    const uid = user.uid;
    const uRef = this.userDocRef(uid);
    const snap = await getDoc(uRef);
    const ts = serverTimestamp();

    if (snap.exists()) {
      const existing = snap.data();
      this.cacheCreatedAt(uRef.path, existing);
      await updateDoc(uRef, {
        email: user.email || null,
        displayName: user.displayName || null,
        updatedAt: ts,
      });
    } else {
      await setDoc(uRef, buildUserAccountPayload(uid, user.email || null, user.displayName || null, ts));
    }

    if (this.bootstrappedUid === uid) return;
    this.bootstrappedUid = uid;

    // Seed defaults ONLY when remote collections are completely empty (same rule as Android)
    const policiesSnap = await getDocs(query(this.colRef("role_policies", uid), limit(1)));
    if (policiesSnap.empty) {
      const b = writeBatch(this.db);
      for (const p of DEFAULT_ROLE_POLICIES) {
        b.set(this.itemRef("role_policies", p.roleId, uid), buildRolePolicyPayload(p, ts));
      }
      await b.commit();
    }

    const staffSnap = await getDocs(query(this.colRef("staff_users", uid), limit(1)));
    if (staffSnap.empty) {
      const b = writeBatch(this.db);
      for (const u of DEFAULT_USERS) {
        b.set(this.itemRef("staff_users", u.id, uid), buildStaffUserPayload(u, ts));
      }
      await b.commit();
    }

    const custSnap = await getDocs(query(this.colRef("customers", uid), limit(1)));
    if (custSnap.empty) {
      const b = writeBatch(this.db);
      for (const c of DEFAULT_CUSTOMERS) {
        b.set(this.itemRef("customers", c.id, uid), buildCustomerPayload(c, ts));
      }
      await b.commit();
    }

    const supSnap = await getDocs(query(this.colRef("suppliers", uid), limit(1)));
    if (supSnap.empty) {
      const b = writeBatch(this.db);
      for (const s of DEFAULT_SUPPLIERS) {
        b.set(this.itemRef("suppliers", s.id, uid), buildSupplierPayload(s, ts));
      }
      await b.commit();
    }

    const caisseSnap = await getDocs(query(this.colRef("caisse_movements", uid), limit(1)));
    if (caisseSnap.empty) {
      await setDoc(
        this.itemRef("caisse_movements", "c_init", uid),
        buildCaisseMovementPayload(
          {
            id: "c_init",
            date: nowIso(),
            type: "entree",
            motif: "Fond de caisse ouverture",
            montant: 50000,
            auteur: "Walid",
            referenceFacture: null,
          },
          ts
        )
      );
    }

    const storeSnap = await getDoc(this.itemRef("store_info", "default", uid));
    if (!storeSnap.exists()) {
      await setDoc(this.itemRef("store_info", "default", uid), buildStoreInfoPayload(DEFAULT_STORE_INFO, ts));
    }

    const settingsSnap = await getDoc(this.itemRef("app_settings", "default", uid));
    if (!settingsSnap.exists()) {
      await setDoc(this.itemRef("app_settings", "default", uid), buildAppSettingsPayload(DEFAULT_SETTINGS, ts));
    }

    const prodSnap = await getDocs(query(this.colRef("products", uid), limit(1)));
    if (prodSnap.empty) {
      try {
        const resp = await fetch("./data/catalog_seed.json");
        if (resp.ok) {
          const seedList = await resp.json();
          for (let i = 0; i < seedList.length; i += 250) {
            const chunk = seedList.slice(i, i + 250);
            const b = writeBatch(this.db);
            for (const p of chunk) {
              b.set(this.itemRef("products", p.id, uid), buildProductPayload(p, ts));
            }
            await b.commit();
          }
        }
      } catch {
        // ignore if optional seed json unavailable
      }
    }
  }

  startListeners(uid, handlers) {
    this.stopListeners();
    this.uid = uid;

    const watchCollection = (name, mapper, onData) => {
      const unsub = onSnapshot(
        this.colRef(name, uid),
        (snap) => {
          const items = snap.docs.map((d) => {
            const data = d.data({ serverTimestamps: "estimate" });
            this.cacheCreatedAt(d.ref.path, data);
            return mapper(d.id, data);
          });
          onData(items, snap.metadata);
        },
        (err) => handlers.onError?.(translateFirebaseError(err))
      );
      this.unsubscribers.push(unsub);
    };

    const watchDoc = (colName, docId, onData) => {
      const unsub = onSnapshot(
        this.itemRef(colName, docId, uid),
        (snap) => {
          if (!snap.exists()) {
            onData(null);
            return;
          }
          const data = snap.data({ serverTimestamps: "estimate" });
          this.cacheCreatedAt(snap.ref.path, data);
          onData(data);
        },
        (err) => handlers.onError?.(translateFirebaseError(err))
      );
      this.unsubscribers.push(unsub);
    };

    watchCollection(
      "products",
      (id, d) => ({
        ...d,
        id,
        pricingUnit: d.pricingUnit || "carton",
        prixPiece: Number(d.prixPiece) || 0,
      }),
      handlers.onProducts
    );

    watchCollection(
      "invoices",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onInvoices(list.sort((a, b) => String(b.date).localeCompare(String(a.date))))
    );

    watchCollection(
      "customers",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onCustomers(list.sort((a, b) => String(a.nom).localeCompare(String(b.nom))))
    );

    watchCollection(
      "suppliers",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onSuppliers(list.sort((a, b) => String(a.nom).localeCompare(String(b.nom))))
    );

    watchCollection(
      "supplier_movements",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onSupplierMovements(list.sort((a, b) => String(b.date).localeCompare(String(a.date))))
    );

    watchCollection(
      "caisse_movements",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onCaisseMovements(list.sort((a, b) => String(b.date).localeCompare(String(a.date))))
    );

    watchCollection(
      "daily_caisse_counts",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onDailyCaisseCounts(list.sort((a, b) => String(b.date).localeCompare(String(a.date))))
    );

    watchCollection(
      "staff_users",
      (id, d) => ({
        ...d,
        id,
        canViewPrixAchat: d.canViewPrixAchat ?? (d.role === "admin" || d.role === "gerant"),
        canModifyPrices: d.canModifyPrices ?? (d.role === "admin" || d.role === "gerant"),
      }),
      (list) => handlers.onStaffUsers(list.sort((a, b) => String(a.name).localeCompare(String(b.name))))
    );

    watchCollection(
      "role_policies",
      (id, d) => ({ ...d, roleId: d.roleId || id }),
      (list) => {
        const order = { admin: 0, gerant: 1, vendeur: 2 };
        handlers.onRolePolicies(list.sort((a, b) => (order[a.roleId] ?? 9) - (order[b.roleId] ?? 9)));
      }
    );

    watchCollection(
      "notifications",
      (id, d) => ({ ...d, id }),
      (list) => handlers.onNotifications(list.sort((a, b) => String(b.date).localeCompare(String(a.date))))
    );

    watchDoc("store_info", "default", handlers.onStoreInfo);
    watchDoc("app_settings", "default", handlers.onAppSettings);
  }

  stopListeners() {
    for (const unsub of this.unsubscribers) {
      try {
        unsub();
      } catch {
        // ignore
      }
    }
    this.unsubscribers = [];
  }

  async awaitPendingSync(timeoutMs = 15000) {
    if (!this.db) return true;
    try {
      await Promise.race([
        waitForPendingWrites(this.db),
        new Promise((_, rej) => setTimeout(() => rej(new Error("timeout")), timeoutMs)),
      ]);
      return true;
    } catch {
      return false;
    }
  }

  // --- Writes (all compliant with firestore.rules) ---

  async upsertProduct(product) {
    const ref = this.itemRef("products", product.id);
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildProductPayload(product, serverTimestamp(), createdAt));
  }

  async adjustProductStockDelta(productId, delta) {
    if (!delta) return;
    const ref = this.itemRef("products", productId);
    await updateDoc(ref, {
      stock: increment(Math.trunc(delta)),
      updatedAt: serverTimestamp(),
    });
  }

  async setProductStock(productId, newStock) {
    const ref = this.itemRef("products", productId);
    await updateDoc(ref, {
      stock: Math.trunc(Number(newStock) || 0),
      updatedAt: serverTimestamp(),
    });
  }

  async commitSaleBatch(plan) {
    const ts = serverTimestamp();
    const invRef = this.itemRef("invoices", plan.invoice.id);
    const invCreated = plan.isEdit ? await this.resolveCreatedAt(invRef) : null;
    const custRef = plan.updatedCustomer ? this.itemRef("customers", plan.updatedCustomer.id) : null;
    const custCreated = custRef ? await this.resolveCreatedAt(custRef) : null;

    const batch = writeBatch(this.db);
    batch.set(invRef, buildInvoicePayload(plan.invoice, ts, invCreated));

    for (const [prodId, delta] of Object.entries(plan.stockDeltas || {})) {
      if (delta !== 0) {
        batch.update(this.itemRef("products", prodId), {
          stock: increment(Math.trunc(delta)),
          updatedAt: ts,
        });
      }
    }

    if (plan.caisseMovement) {
      batch.set(
        this.itemRef("caisse_movements", plan.caisseMovement.id),
        buildCaisseMovementPayload(plan.caisseMovement, ts)
      );
    }

    if (custRef && plan.updatedCustomer) {
      batch.set(custRef, buildCustomerPayload(plan.updatedCustomer, ts, custCreated));
    }

    for (const n of plan.notifications || []) {
      batch.set(this.itemRef("notifications", n.id), buildDepotNotificationPayload(n, ts));
    }

    await batch.commit();
  }

  async commitDeleteInvoiceBatch(invoiceId, plan) {
    const ts = serverTimestamp();
    const invRef = this.itemRef("invoices", invoiceId);
    const custRef = plan.updatedCustomer ? this.itemRef("customers", plan.updatedCustomer.id) : null;
    const custCreated = custRef ? await this.resolveCreatedAt(custRef) : null;

    const batch = writeBatch(this.db);
    batch.delete(invRef);

    for (const [prodId, delta] of Object.entries(plan.restoredStockDeltas || {})) {
      if (delta !== 0) {
        batch.update(this.itemRef("products", prodId), {
          stock: increment(Math.trunc(delta)),
          updatedAt: ts,
        });
      }
    }

    if (custRef && plan.updatedCustomer) {
      batch.set(custRef, buildCustomerPayload(plan.updatedCustomer, ts, custCreated));
    }

    if (plan.notification) {
      batch.set(
        this.itemRef("notifications", plan.notification.id),
        buildDepotNotificationPayload(plan.notification, ts)
      );
    }

    await batch.commit();
    this.createdAtCache.delete(invRef.path);
  }

  async updateInvoiceHidePrices(invoiceId, hide) {
    await updateDoc(this.itemRef("invoices", invoiceId), {
      hidePrices: Boolean(hide),
      updatedAt: serverTimestamp(),
    });
  }

  async markInvoiceWhatsappSent(invoiceId) {
    await updateDoc(this.itemRef("invoices", invoiceId), {
      whatsappSentBeforePrint: true,
      updatedAt: serverTimestamp(),
    });
  }

  async upsertCustomer(customer) {
    const ref = this.itemRef("customers", customer.id);
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildCustomerPayload(customer, serverTimestamp(), createdAt));
  }

  async payCustomerDebt(updatedCustomer, caisseMovement) {
    const ts = serverTimestamp();
    const custRef = this.itemRef("customers", updatedCustomer.id);
    const custCreated = await this.resolveCreatedAt(custRef);
    const batch = writeBatch(this.db);
    batch.set(custRef, buildCustomerPayload(updatedCustomer, ts, custCreated));
    batch.set(
      this.itemRef("caisse_movements", caisseMovement.id),
      buildCaisseMovementPayload(caisseMovement, ts)
    );
    await batch.commit();
  }

  async upsertSupplier(supplier) {
    const ref = this.itemRef("suppliers", supplier.id);
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildSupplierPayload(supplier, serverTimestamp(), createdAt));
  }

  async commitSupplierMovement(updatedSupplier, supplierMovement, caisseMovement = null) {
    const ts = serverTimestamp();
    const supRef = this.itemRef("suppliers", updatedSupplier.id);
    const supCreated = await this.resolveCreatedAt(supRef);
    const batch = writeBatch(this.db);
    batch.set(supRef, buildSupplierPayload(updatedSupplier, ts, supCreated));
    batch.set(
      this.itemRef("supplier_movements", supplierMovement.id),
      buildSupplierMovementPayload(supplierMovement, ts)
    );
    if (caisseMovement) {
      batch.set(
        this.itemRef("caisse_movements", caisseMovement.id),
        buildCaisseMovementPayload(caisseMovement, ts)
      );
    }
    await batch.commit();
  }

  async createCaisseMovement(mov) {
    await setDoc(
      this.itemRef("caisse_movements", mov.id),
      buildCaisseMovementPayload(mov, serverTimestamp())
    );
  }

  async createDailyCaisseCount(cnt) {
    await setDoc(
      this.itemRef("daily_caisse_counts", cnt.id),
      buildDailyCaisseCountPayload(cnt, serverTimestamp())
    );
  }

  async upsertStaffUser(user) {
    const ref = this.itemRef("staff_users", user.id);
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildStaffUserPayload(user, serverTimestamp(), createdAt));
  }

  async deleteStaffUser(staffId) {
    const ref = this.itemRef("staff_users", staffId);
    await deleteDoc(ref);
    this.createdAtCache.delete(ref.path);
  }

  async upsertRolePolicy(policy) {
    const ref = this.itemRef("role_policies", policy.roleId);
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildRolePolicyPayload(policy, serverTimestamp(), createdAt));
  }

  async createNotification(notif) {
    await setDoc(
      this.itemRef("notifications", notif.id),
      buildDepotNotificationPayload(notif, serverTimestamp())
    );
  }

  async markAllNotificationsRead(unreadIds) {
    if (!unreadIds || unreadIds.length === 0) return;
    const ts = serverTimestamp();
    for (let i = 0; i < unreadIds.length; i += 250) {
      const chunk = unreadIds.slice(i, i + 250);
      const batch = writeBatch(this.db);
      for (const id of chunk) {
        batch.update(this.itemRef("notifications", id), { read: true, updatedAt: ts });
      }
      await batch.commit();
    }
  }

  async saveStoreInfo(info) {
    const ref = this.itemRef("store_info", "default");
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildStoreInfoPayload(info, serverTimestamp(), createdAt));
  }

  async saveAppSettings(settings) {
    const ref = this.itemRef("app_settings", "default");
    const createdAt = await this.resolveCreatedAt(ref);
    await setDoc(ref, buildAppSettingsPayload(settings, serverTimestamp(), createdAt));
  }
}

export function translateFirebaseError(err) {
  const code = err?.code || "";
  const msg = err?.message || String(err || "");
  if (code === "auth/unauthorized-domain") {
    return "Domaine non autorisé dans Firebase Auth. Lancez via http://localhost:8173 (Start_CERAMICA_PC.bat) ou ajoutez ce domaine dans Firebase Console > Authentication > Settings.";
  }
  if (code === "auth/popup-closed-by-user") {
    return "Fenêtre Google fermée avant la fin de la connexion.";
  }
  if (code === "permission-denied") {
    return "Accès refusé par les règles Firestore : vérifiez que vous utilisez le même compte Google que sur Android.";
  }
  if (code === "unavailable") {
    return "Serveur Firebase temporairement inaccessible — modifications gardées en file locale et synchronisées dès reconnexion.";
  }
  return msg;
}
