const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function validTimestamp() {
  return new Date(Date.now() - 1000);
}

function validProfilePayload(uid) {
  const ts = validTimestamp();
  return {
    userId: uid,
    displayName: "Abdullah Rahman",
    district: "Dhaka",
    calculationMethod: "ISLAMIC_FOUNDATION_BD",
    madhhab: "HANAFI",
    language: "bn",
    themeMode: "DARK",
    tasbihAllTimeTotal: 100,
    qazaFajr: 0,
    qazaDhuhr: 0,
    qazaAsr: 0,
    qazaMaghrib: 0,
    qazaIsha: 0,
    qazaWitr: 0,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validSalatLogPayload(uid) {
  const ts = validTimestamp();
  return {
    userId: uid,
    dateKey: "2026-11-10",
    fajr: true,
    dhuhr: true,
    asr: false,
    maghrib: false,
    isha: false,
    witr: false,
    duha: true,
    awwabin: false,
    tahajjud: false,
    fasting: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validBookmarkPayload(uid) {
  const ts = validTimestamp();
  return {
    userId: uid,
    surahNumber: 1,
    ayahNumber: 1,
    surahNameBn: "আল-ফাতিহা",
    surahNameEn: "Al-Fatihah",
    arabicPreview: "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
    translationPreview: "পরম করুণাময় অতি দয়ালু আল্লাহর নামে।",
    note: "Daily recitation",
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated user: cannot read or write user profiles, salat logs, or bookmarks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salat_logs")
      .doc("2026-11-10")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarks")
      .doc("bm_1")
      .get()
  );
});

test("Authenticated user: can create, read, and update their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payload = validProfilePayload(ALICE_UID);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(payload));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Abdullah Al-Mamun",
      tasbihAllTimeTotal: 133,
      updatedAt: validTimestamp(),
    })
  );
});

test("Authenticated user: cannot read or modify another user's profile or subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("salat_logs")
      .doc("2026-11-10")
      .set(validSalatLogPayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("bookmarks")
      .doc("bm_1")
      .set(validBookmarkPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("salat_logs")
      .doc("2026-11-10")
      .get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("bookmarks")
      .doc("bm_1")
      .get()
  );
});

test("Authenticated user: shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payload = validProfilePayload(ALICE_UID);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(payload));
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: validTimestamp(),
    })
  );
});

test("Authenticated user: can create, query, and update their own salat logs and bookmarks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salat_logs")
      .doc("2026-11-10")
      .set(validSalatLogPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salat_logs")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarks")
      .doc("bm_1")
      .set(validBookmarkPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarks")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});
