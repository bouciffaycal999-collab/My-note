import { test, before, after, beforeEach } from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import { createRequire } from "node:module";

import {
  FIREBASE_PROJECT_ID,
  FIRESTORE_DATABASE_ID,
  DEFAULT_ROLE_POLICIES,
  DEFAULT_USERS,
  DEFAULT_CUSTOMERS,
  DEFAULT_SUPPLIERS,
  DEFAULT_STORE_INFO,
  DEFAULT_SETTINGS,
  TileCalculatorMath,
  resolveEffectivePermissions,
  parseFirebaseConfigInput,
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
  computeSaleCommitPlan,
  computeDeleteInvoicePlan,
} from "./js/pos-domain.js";

const require = createRequire(import.meta.url);
const { initializeTestEnvironment, assertSucceeds, assertFails } = require("@firebase/rules-unit-testing");
const rulesPkgPath = require.resolve("@firebase/rules-unit-testing");
const firebase = require(require.resolve("firebase/compat/app", { paths: [rulesPkgPath] }));
require(require.resolve("firebase/compat/firestore", { paths: [rulesPkgPath] }));

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);
const UID = "pc_owner_uid_1";
const OTHER_UID = "other_uid_2";

const serverTs = () => firebase.firestore.FieldValue.serverTimestamp();
const inc = (n) => firebase.firestore.FieldValue.increment(n);

let testEnv;

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: { rules, host: emulatorHost, port: emulatorPort },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Named database and project constants match Android configuration", () => {
  assert.equal(FIREBASE_PROJECT_ID, "ace-project-498620");
  assert.equal(FIRESTORE_DATABASE_ID, "ai-studio-android-82d1216f-37fc-468a-a33f-aa7c2903aaec");
});

test("parseFirebaseConfigInput parses both JS snippet and JSON cleanly", () => {
  const snippet = `
    // Your web app's Firebase configuration
    const firebaseConfig = {
      apiKey: "AIzaDummyKeyForParserTestOnly123",
      authDomain: "ace-project-498620.firebaseapp.com",
      projectId: "ace-project-498620",
      storageBucket: "ace-project-498620.firebasestorage.app",
      messagingSenderId: "306069028107",
      appId: "1:306069028107:web:abc123",
    };
  `;
  const parsed = parseFirebaseConfigInput(snippet);
  assert.equal(parsed.ok, true);
  assert.equal(parsed.projectMismatch, false);
  assert.equal(parsed.config.projectId, "ace-project-498620");

  const bad = parseFirebaseConfigInput("{ not valid");
  assert.equal(bad.ok, false);
});

test("TileCalculatorMath and RBAC permissions match Android calculations", () => {
  const surf = TileCalculatorMath.calculateFromSurface(20, 10, 1.44, 4, 1500);
  assert.equal(surf.cartons, 16);
  assert.equal(surf.deliveredM2, 23.04);
  assert.equal(surf.totalDa, 34560);

  const kader = DEFAULT_USERS.find((u) => u.id === "u_kader");
  const zohir = DEFAULT_USERS.find((u) => u.id === "u_zohir");
  const adminPerms = resolveEffectivePermissions(kader, DEFAULT_ROLE_POLICIES);
  const sellerPerms = resolveEffectivePermissions(zohir, DEFAULT_ROLE_POLICIES);

  assert.equal(adminPerms.canViewPrixAchat, true);
  assert.equal(adminPerms.canModifyStock, true);
  assert.equal(sellerPerms.canViewPrixAchat, false);
  assert.equal(sellerPerms.canModifyStock, false);
  assert.equal(sellerPerms.canModifyPrices, false);
});

test("All pc-web payloads pass firestore.rules and sync atomically in emulator", async () => {
  const db = testEnv.authenticatedContext(UID, { email: "kader@ceramica.dz" }).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const uRef = db.collection("users").doc(UID);

  // 1. User account bootstrap & update
  await assertSucceeds(uRef.set(buildUserAccountPayload(UID, "kader@ceramica.dz", "Kader", serverTs())));
  await assertSucceeds(uRef.update({ email: "kader@ceramica.dz", displayName: "Kader PC", updatedAt: serverTs() }));

  // 2. Role policies, staff, store_info, app_settings
  for (const rp of DEFAULT_ROLE_POLICIES) {
    await assertSucceeds(uRef.collection("role_policies").doc(rp.roleId).set(buildRolePolicyPayload(rp, serverTs())));
  }
  for (const st of DEFAULT_USERS.slice(0, 2)) {
    await assertSucceeds(uRef.collection("staff_users").doc(st.id).set(buildStaffUserPayload(st, serverTs())));
  }
  await assertSucceeds(uRef.collection("store_info").doc("default").set(buildStoreInfoPayload(DEFAULT_STORE_INFO, serverTs())));
  await assertSucceeds(uRef.collection("app_settings").doc("default").set(buildAppSettingsPayload(DEFAULT_SETTINGS, serverTs())));

  // 3. Products & Customers & Suppliers + SupplierMovement
  const prod = {
    id: "p_60_60_bati_luxor_rect",
    name: "60/60 BATI LUXOR RECT",
    usine: "BATI",
    dimension: "60×60",
    m2Carton: 1.44,
    pcs: 4,
    prix: 1500,
    prixAchat: 950,
    stock: 10,
    color: "#94a3b8",
    colorName: "Gris Minéral",
    image: "",
    finition: "rectifie",
    choix: "1er Choix",
    categorie: "sol",
    description: "Carrelage test",
    pricingUnit: "carton",
    prixPiece: 0,
  };
  await assertSucceeds(uRef.collection("products").doc(prod.id).set(buildProductPayload(prod, serverTs())));

  const cust = DEFAULT_CUSTOMERS[0];
  await assertSucceeds(uRef.collection("customers").doc(cust.id).set(buildCustomerPayload(cust, serverTs())));
  const custSnap = await uRef.collection("customers").doc(cust.id).get();
  const custCreatedAt = custSnap.data().createdAt;

  const sup = DEFAULT_SUPPLIERS[0];
  await assertSucceeds(uRef.collection("suppliers").doc(sup.id).set(buildSupplierPayload(sup, serverTs())));
  await assertSucceeds(
    uRef
      .collection("supplier_movements")
      .doc("sup_mov_1")
      .set(
        buildSupplierMovementPayload(
          {
            supplierId: sup.id,
            date: new Date().toISOString(),
            type: "versement",
            montant: 25000,
            motif: "Versement PC",
            pieceJointeRef: "BL-123",
            auteur: "Kader",
          },
          serverTs()
        )
      )
  );

  // 4. Full Sale Batch (invoice + stock decrement + caisse + customer update + low-stock & sale notifications)
  const plan = computeSaleCommitPlan({
    cart: [
      {
        id: "ci_1",
        productId: prod.id,
        name: prod.name,
        usine: prod.usine,
        dimension: prod.dimension,
        m2Carton: prod.m2Carton,
        pcs: prod.pcs,
        prixUnitaire: prod.prix,
        prixAchat: prod.prixAchat,
        cartons: 7,
        pieces: 0,
        m2Total: 10.08,
        sousTotal: 15120,
        bain: "B1",
        calibre: "C2",
        color: prod.color,
        colorName: prod.colorName,
        image: "",
        prixPiece: 0,
        pricingUnit: "carton",
      },
    ],
    freeLines: [
      {
        id: "fl_1",
        designation: "Colle C1",
        quantite: 2,
        unite: "sac",
        prixUnitaire: 750,
        sousTotal: 1500,
      },
    ],
    client: { id: cust.id, nom: cust.nom, tel: cust.tel, adresse: cust.adresse },
    remise: 620,
    montantPaye: 10000,
    modePaiement: "Partiel",
    notes: "Livraison Remchi",
    sellerName: "Kader",
    magasin: "Depot Remchi",
    canModifyPrices: true,
    products: [prod],
    customers: [cust],
  });

  // Assert low stock alert was generated (10 - 7 = 3 <= 5)
  assert.equal(plan.notifications.length, 2);

  const batch = db.batch();
  batch.set(uRef.collection("invoices").doc(plan.invoice.id), buildInvoicePayload(plan.invoice, serverTs()));
  batch.update(uRef.collection("products").doc(prod.id), { stock: inc(plan.stockDeltas[prod.id]), updatedAt: serverTs() });
  batch.set(
    uRef.collection("caisse_movements").doc(plan.caisseMovement.id),
    buildCaisseMovementPayload(plan.caisseMovement, serverTs())
  );
  batch.set(
    uRef.collection("customers").doc(cust.id),
    buildCustomerPayload(plan.updatedCustomer, serverTs(), custCreatedAt)
  );
  for (const n of plan.notifications) {
    batch.set(uRef.collection("notifications").doc(n.id), buildDepotNotificationPayload(n, serverTs()));
  }
  await assertSucceeds(batch.commit());

  const updatedProd = (await uRef.collection("products").doc(prod.id).get()).data();
  assert.equal(updatedProd.stock, 3);

  // 5. Daily caisse count + invoice flags + mark notifications read
  await assertSucceeds(
    uRef
      .collection("daily_caisse_counts")
      .doc("cnt_1")
      .set(
        buildDailyCaisseCountPayload(
          {
            date: new Date().toISOString(),
            magasin: "Depot Remchi",
            auteur: "Kader",
            fondOuverture: 50000,
            ventesEspecesJour: 10000,
            entreesDiversJour: 0,
            sortiesJour: 0,
            theorique: 60000,
            compteReel: 60000,
            ecart: 0,
            notes: "RAS",
          },
          serverTs()
        )
      )
  );

  await assertSucceeds(
    uRef.collection("invoices").doc(plan.invoice.id).update({ hidePrices: true, updatedAt: serverTs() })
  );
  await assertSucceeds(
    uRef.collection("notifications").doc(plan.notifications[0].id).update({ read: true, updatedAt: serverTs() })
  );

  // 6. Delete invoice batch restores stock
  const delPlan = computeDeleteInvoicePlan({
    invoice: plan.invoice,
    actorName: "Kader",
    customers: [plan.updatedCustomer],
  });
  const delBatch = db.batch();
  delBatch.delete(uRef.collection("invoices").doc(plan.invoice.id));
  delBatch.update(uRef.collection("products").doc(prod.id), {
    stock: inc(delPlan.restoredStockDeltas[prod.id]),
    updatedAt: serverTs(),
  });
  delBatch.set(
    uRef.collection("notifications").doc(delPlan.notification.id),
    buildDepotNotificationPayload(delPlan.notification, serverTs())
  );
  await assertSucceeds(delBatch.commit());
  assert.equal((await uRef.collection("products").doc(prod.id).get()).data().stock, 10);

  // 7. Cross-user isolation remains enforced
  await assertFails(otherDb.collection("users").doc(UID).collection("invoices").get());
});
