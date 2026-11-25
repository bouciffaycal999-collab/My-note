const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_depot_1";
const BOB_UID = "bob_depot_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validDeliveryPayload(uid, createdAt) {
  return {
    userId: uid,
    clientName: "Karim",
    clientPhone: "0550123456",
    clientAddress: "Oran Est",
    items: [
      { lineId: "l1", text: "60/60 king mat 18 cart", delivered: false, deliveredAtMs: 0 },
      { lineId: "l2", text: "Motif limouzine bleu 12 ps", delivered: false, deliveredAtMs: 0 },
    ],
    completed: false,
    createdAt,
    updatedAt: createdAt,
  };
}

function validRestockPayload(uid, createdAt) {
  return {
    userId: uid,
    articleName: "60/60 king mat",
    quantityNote: "2 palettes",
    urgent: true,
    purchased: false,
    createdAt,
    updatedAt: createdAt,
  };
}

function validJournalPayload(uid, createdAt, entryType = "DELIVERY") {
  return {
    userId: uid,
    entryType,
    articleText: "60/60 king mat 18 cart",
    quantityNote: "",
    clientName: "Karim",
    clientPhone: "0550123456",
    clientAddress: "Oran Est",
    sourceNoteId: "note_1",
    sourceLineId: "l1",
    dayKey: "2026-11-25",
    createdAt,
  };
}

test("Unauthenticated user cannot read or write any DepotNote collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("deliveryNotes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("deliveryNotes").doc("note_1").set(validDeliveryPayload(ALICE_UID, now))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("restockNotes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("restockNotes").doc("restock_1").set(validRestockPayload(ALICE_UID, now))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("journalEntries").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("journalEntries").doc("journal_1").set(validJournalPayload(ALICE_UID, now))
  );
});

test("DeliveryNotes: owner create (with optional blank client fields), partial check-off update, edit, list, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("deliveryNotes");

  // Create with optional client fields empty (only paid articles required)
  await assertSucceeds(
    col.doc("note_no_client").set({
      userId: ALICE_UID,
      clientName: "",
      clientPhone: "",
      clientAddress: "",
      items: [{ lineId: "l1", text: "60/60 king mat 18 cart", delivered: false, deliveredAtMs: 0 }],
      completed: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Check off article + mark note completed (mirrors Kotlin toggleDeliveryLine)
  await assertSucceeds(
    col.doc("note_no_client").update({
      items: [{ lineId: "l1", text: "60/60 king mat 18 cart", delivered: true, deliveredAtMs: Date.now() }],
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Edit client metadata & items
  await assertSucceeds(
    col.doc("note_no_client").update({
      clientName: "Karim",
      clientPhone: "0661001122",
      clientAddress: "Dépôt Nord",
      items: [{ lineId: "l1", text: "60/60 king mat 18 cart", delivered: false, deliveredAtMs: 0 }],
      completed: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // List query mirrors Kotlin repository observeDeliveryNotes
  await assertSucceeds(col.get());

  // Delete succeeds
  await assertSucceeds(col.doc("note_no_client").delete());
});

test("DeliveryNotes: rejects ghost fields, id field, empty items, future timestamps, createdAt tampering, and cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const aliceCol = aliceDb.collection("users").doc(ALICE_UID).collection("deliveryNotes");

  // Reject @DocumentId 'id' inside fields
  await assertFails(
    aliceCol.doc("note_bad_id").set({ ...validDeliveryPayload(ALICE_UID, created), id: "note_bad_id" })
  );

  // Reject ghost field (Shadow Update test)
  await assertFails(
    aliceCol.doc("note_ghost").set({ ...validDeliveryPayload(ALICE_UID, created), isVerified: true })
  );

  // Reject empty items array
  await assertFails(
    aliceCol.doc("note_empty").set({ ...validDeliveryPayload(ALICE_UID, created), items: [] })
  );

  // Reject tampered future timestamp
  await assertFails(
    aliceCol.doc("note_future").set(validDeliveryPayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  // Seed valid note then verify immutable createdAt & cross-user denial
  await assertSucceeds(aliceCol.doc("note_1").set(validDeliveryPayload(ALICE_UID, created)));
  await assertFails(
    aliceCol.doc("note_1").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  const bobRef = bobDb.collection("users").doc(ALICE_UID).collection("deliveryNotes").doc("note_1");
  await assertFails(bobRef.get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("deliveryNotes").get());
  await assertFails(bobRef.update({ completed: true, updatedAt: new Date(Date.now() - 500) }));
  await assertFails(bobRef.delete());
});

test("RestockNotes: owner CRUD succeeds; rejects blank articleName, ghost fields, and cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("restockNotes");

  await assertSucceeds(col.doc("r1").set(validRestockPayload(ALICE_UID, created)));
  await assertSucceeds(col.doc("r1").update({ purchased: true, updatedAt: new Date(Date.now() - 1000) }));
  await assertSucceeds(col.doc("r1").update({ articleName: "60/60 king mat beige", quantityNote: "3 palettes", urgent: false, updatedAt: new Date(Date.now() - 500) }));
  await assertSucceeds(col.get());

  // Reject blank articleName
  await assertFails(col.doc("r_blank").set({ ...validRestockPayload(ALICE_UID, created), articleName: "" }));
  // Reject ghost field
  await assertFails(col.doc("r_ghost").set({ ...validRestockPayload(ALICE_UID, created), role: "admin" }));
  // Cross-user denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("restockNotes").doc("r1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("restockNotes").get());

  await assertSucceeds(col.doc("r1").delete());
});

test("JournalEntries: owner create, list, delete succeed; updates are forbidden; rejects invalid entryType/dayKey and cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("journalEntries");

  await assertSucceeds(col.doc("j1").set(validJournalPayload(ALICE_UID, created, "DELIVERY")));
  await assertSucceeds(col.doc("j2").set(validJournalPayload(ALICE_UID, created, "PURCHASE")));
  await assertSucceeds(col.get());

  // Journal entries are immutable (update denied)
  await assertFails(col.doc("j1").update({ articleText: "Modified" }));
  // Invalid entryType denied
  await assertFails(col.doc("j_bad_type").set(validJournalPayload(ALICE_UID, created, "OTHER")));
  // Invalid dayKey format denied
  await assertFails(col.doc("j_bad_day").set({ ...validJournalPayload(ALICE_UID, created), dayKey: "25/11/2026" }));
  // Cross-user denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("journalEntries").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("journalEntries").doc("j1").delete());

  // Owner delete (used for Undo & manual cleanup) succeeds
  await assertSucceeds(col.doc("j1").delete());
});
