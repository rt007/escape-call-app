// /api/trigger-call.js
// Vercel Serverless Function — triggers a Twilio call to the user's phone

const twilio = require("twilio");

module.exports = async (req, res) => {
  // ── Only allow POST ──
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  // ── Validate secret token ──
  const token = req.headers["x-escape-token"];
  if (!token || token !== process.env.ESCAPE_SECRET_TOKEN) {
    return res.status(401).json({ error: "Unauthorized" });
  }

  // ── Environment variables ──
  const accountSid = process.env.TWILIO_ACCOUNT_SID;
  const authToken = process.env.TWILIO_AUTH_TOKEN;
  const twilioNumber = process.env.TWILIO_PHONE_NUMBER; // Your Twilio number (caller ID)
  const userNumber = process.env.USER_PHONE_NUMBER; // Your real phone number (recipient)

  if (!accountSid || !authToken || !twilioNumber || !userNumber) {
    console.error("Missing required environment variables");
    return res.status(500).json({ error: "Server misconfigured" });
  }

  // ── Build the TwiML URL (same Vercel deployment) ──
  const host = `https://${req.headers.host}`;
  const twimlUrl = `${host}/api/twiml-silent`;

  // ── Place the call via Twilio ──
  const client = twilio(accountSid, authToken);

  try {
    const call = await client.calls.create({
      to: userNumber,
      from: twilioNumber,
      url: twimlUrl,
      method: "GET",
    });

    console.log(`Call initiated: ${call.sid}`);
    return res.status(200).json({
      success: true,
      callSid: call.sid,
    });
  } catch (err) {
    console.error("Twilio error:", err.message);
    return res.status(500).json({
      error: "Failed to place call",
      detail: err.message,
    });
  }
};
