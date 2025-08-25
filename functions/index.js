// /functions/index.js
const admin = require("firebase-admin");
const crypto = require("crypto");
const nodemailer = require("nodemailer");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

admin.initializeApp();
const db = admin.firestore();

// ====== Secrets you already set with the CLI ======
const GMAIL_USER = defineSecret("GMAIL_USER");
const GMAIL_APP_PASSWORD = defineSecret("GMAIL_APP_PASSWORD");

// ====== Constants ======
const REGION = "asia-south1";            // Near India for lower latency
const OTP_TTL_MS = 10 * 60 * 1000;       // 10 minutes
const RESEND_COOLDOWN_MS = 60 * 1000;    // 60 seconds
const MAX_HOURLY_SENDS = 5;
const MAX_VERIFY_ATTEMPTS = 5;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// ====== Helpers ======
const genOtp = () => String(Math.floor(100000 + Math.random() * 900000));
const hashOtp = (otp, salt) =>
  crypto.createHmac("sha256", salt).update(otp).digest("hex");

const createTransporter = () =>
  nodemailer.createTransport({
    service: "gmail",
    auth: { user: GMAIL_USER.value(), pass: GMAIL_APP_PASSWORD.value() },
  });

// ====== requestOtp (generate + email the code) ======
exports.requestOtp = onCall(
  { region: REGION, secrets: [GMAIL_USER, GMAIL_APP_PASSWORD], enforceAppCheck: true },
  async (req) => {
    const email = String(req.data?.email || "").trim().toLowerCase();
    if (!EMAIL_RE.test(email)) throw new HttpsError("invalid-argument", "Invalid email.");

    const now = admin.firestore.Timestamp.now();
    const metaRef = db.collection("_otpMeta").doc(email);

    // Create OTP & rate-limit atomically
    const { otp, expiresAt } = await db.runTransaction(async (tx) => {
      const snap = await tx.get(metaRef);
      let lastRequestAt = null, hourlyCount = 0, windowStart = now;
      if (snap.exists) {
        const m = snap.data();
        lastRequestAt = m.lastRequestAt || null;
        hourlyCount  = m.hourlyCount  || 0;
        windowStart  = m.windowStart  || now;
      }
      if (lastRequestAt && (now.toMillis() - lastRequestAt.toMillis() < RESEND_COOLDOWN_MS)) {
        throw new HttpsError("resource-exhausted", "Wait 60 seconds before resending.");
      }
      // reset hourly window
      if (now.toMillis() - windowStart.toMillis() > 60 * 60 * 1000) {
        windowStart = now;
        hourlyCount = 0;
      }
      if (hourlyCount >= MAX_HOURLY_SENDS) {
        throw new HttpsError("resource-exhausted", "Too many OTP requests. Try later.");
      }

      const otp = genOtp();
      const salt = crypto.randomBytes(16).toString("hex");
      const otpHash = hashOtp(otp, salt);
      const expiresAt = admin.firestore.Timestamp.fromMillis(now.toMillis() + OTP_TTL_MS);

      const otpRef = db.collection("_emailOtps").doc();
      tx.set(otpRef, { email, otpHash, salt, used: false, attempts: 0, createdAt: now, expiresAt });

      tx.set(
        metaRef,
        {
          lastRequestAt: now,
          windowStart,
          hourlyCount: admin.firestore.FieldValue.increment(1),
        },
        { merge: true }
      );

      return { otp, expiresAt };
    });

    // Send email (outside transaction)
    try {
      const t = createTransporter();
      await t.sendMail({
        from: `ShopIt <${GMAIL_USER.value()}>`,
        to: email,
        subject: "Your ShopIt verification code",
        text: `Your ShopIt verification code is ${otp}. It expires in 10 minutes.`,
        html: `<p>Your ShopIt verification code is <b style="font-size:18px;">${otp}</b>.</p>
               <p>This code expires in 10 minutes.</p>`,
      });
    } catch (e) {
      logger.error("Email send failed", e);
      throw new HttpsError("unavailable", "Could not send OTP email.");
    }

    // Never return the OTP to the client
    return { ok: true, expiresAt: expiresAt.toMillis() };
  }
);

// ====== verifyOtp (check + sign-in with custom token) ======
exports.verifyOtp = onCall(
  { region: REGION, enforceAppCheck: true },
  async (req) => {
    const email = String(req.data?.email || "").trim().toLowerCase();
    const otp   = String(req.data?.otp || "").trim();

    if (!EMAIL_RE.test(email)) throw new HttpsError("invalid-argument", "Invalid email.");
    if (!/^\d{6}$/.test(otp))   throw new HttpsError("invalid-argument", "Invalid OTP format.");

    const now = admin.firestore.Timestamp.now();
    const q = await db.collection("_emailOtps")
      .where("email", "==", email)
      .where("used", "==", false)
      .where("expiresAt", ">=", now)
      .orderBy("expiresAt", "desc")
      .limit(1)
      .get();

    if (q.empty) throw new HttpsError("deadline-exceeded", "OTP expired or not found.");

    const doc = q.docs[0];
    const data = doc.data();
    const attempts = data.attempts || 0;

    if (attempts >= MAX_VERIFY_ATTEMPTS) {
      await doc.ref.update({ used: true, blockedAt: now });
      throw new HttpsError("resource-exhausted", "Too many incorrect attempts.");
    }

    const computed = hashOtp(otp, data.salt);
    if (computed !== data.otpHash) {
      await doc.ref.update({ attempts: attempts + 1 });
      throw new HttpsError("permission-denied", "Incorrect code.");
    }

    await doc.ref.update({ used: true, verifiedAt: now });

    // Create/get Firebase Auth user and return a custom token to sign in on Android
        let user;
    try {
      user = await admin.auth().getUserByEmail(email);
    } catch {
      user = await admin.auth().createUser({ email });
    }

    const customToken = await admin.auth().createCustomToken(user.uid);
    return { ok: true, token: customToken };
  }
);

