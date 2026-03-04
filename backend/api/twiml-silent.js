// /api/twiml-silent.js
// Returns TwiML that keeps the call alive with silence for 30 seconds.
// Phase 2: Replace <Pause> with <Play> or <Say> for voice playback.

module.exports = async (req, res) => {
  res.setHeader("Content-Type", "application/xml");
  res.status(200).send(`<?xml version="1.0" encoding="UTF-8"?>
<Response>
  <Pause length="30"/>
</Response>`);
};
