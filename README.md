# 📞 Escape Call — One-Tap Fake Incoming Call

A lightweight system that triggers a **real incoming phone call** to your Android device with a single tap. Perfect for politely exiting conversations.

```
Android App (Button / Widget)
  → HTTPS POST
  → Vercel Serverless Backend
  → Twilio Voice API
  → Real incoming call to your phone
```

---

## Project Structure

```
escape-call/
├── backend/                    # Vercel serverless functions
│   ├── api/
│   │   ├── trigger-call.js     # POST endpoint — initiates the Twilio call
│   │   └── twiml-silent.js     # GET endpoint — returns silent TwiML
│   ├── package.json
│   ├── vercel.json
│   └── .env.example
│
├── android/                    # Android app (Kotlin)
│   ├── app/src/main/
│   │   ├── java/com/escapecall/
│   │   │   ├── Config.kt              # ← EDIT THIS: your backend URL + token
│   │   │   ├── MainActivity.kt        # In-app trigger button
│   │   │   ├── network/
│   │   │   │   ├── ApiClient.kt       # HTTP client
│   │   │   │   └── TriggerCallService.kt  # Foreground service for widget
│   │   │   └── widget/
│   │   │       └── EscapeCallWidget.kt    # Homescreen widget
│   │   └── res/                        # Layouts, drawables, XML configs
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
└── README.md
```

---

## Setup Guide

### Prerequisites

- [Node.js](https://nodejs.org/) (v18+)
- [Vercel CLI](https://vercel.com/docs/cli) (`npm i -g vercel`)
- [Android Studio](https://developer.android.com/studio) (Hedgehog or newer)
- A [Twilio account](https://www.twilio.com/try-twilio) (free trial works)
- A Twilio phone number (you get one free on trial)

---

### Step 1: Twilio Setup

1. Sign up at [twilio.com](https://www.twilio.com/try-twilio)
2. From the Console dashboard, note your:
   - **Account SID** (starts with `AC`)
   - **Auth Token**
3. Get a phone number: **Phone Numbers → Manage → Buy a Number**
4. Note your Twilio phone number (e.g., `+12025551234`)

> **Pro tip:** Save the Twilio number as a contact on your phone (e.g., "Mom" or "Boss") so incoming calls look completely natural.

---

### Step 2: Deploy the Backend to Vercel

This step takes the `backend/` folder from the project and deploys it as a live serverless API on Vercel's free tier. By the end, you'll have two working URLs that the Android app (and Twilio) will call.

---

#### 2.1 — Install Node.js (if you don't have it)

Vercel CLI requires Node.js 18 or higher.

**Check if you already have it:**

```bash
node --version
# Should show v18.x.x or higher
```

**If not installed**, download from [nodejs.org](https://nodejs.org/) (LTS recommended) or use a version manager:

```bash
# macOS (Homebrew)
brew install node

# Windows (winget)
winget install OpenJS.NodeJS.LTS

# Linux (Ubuntu/Debian)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs
```

Verify both `node` and `npm` are available:

```bash
node --version    # v18+ required
npm --version     # comes with Node
```

---

#### 2.2 — Install the Vercel CLI

```bash
npm install -g vercel
```

Verify installation:

```bash
vercel --version
# Should show something like: Vercel CLI 37.x.x
```

> **Troubleshooting:** If you get a permission error on macOS/Linux, either prefix with `sudo` or [fix your npm permissions](https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally).

---

#### 2.3 — Create a Vercel Account & Log In

1. Go to [vercel.com](https://vercel.com/) and sign up (free "Hobby" plan is all you need)
   - You can sign up with GitHub, GitLab, Bitbucket, or email
2. Back in your terminal, log in:

```bash
vercel login
```

3. Choose your login method (e.g., **Continue with Email**). Vercel will send a verification email — click the link to confirm. You'll see:

```
✔ Email confirmed
> Congratulations! You are now logged in.
```

---

#### 2.4 — Install Backend Dependencies

Navigate to the `backend/` folder and install the Twilio SDK:

```bash
cd backend
npm install
```

This creates a `node_modules/` folder and a `package-lock.json`. Both are normal — only `package.json` and the `api/` folder get deployed.

---

#### 2.5 — Generate Your Secret Token

Before deploying, generate a random secret string that both your backend and Android app will share. Run this in your terminal:

```bash
# macOS / Linux
openssl rand -hex 32

# Windows (PowerShell)
-join ((1..32) | ForEach-Object { '{0:x2}' -f (Get-Random -Max 256) })
```

This outputs something like: `a3f8b2c1d4e5f67890abcdef12345678abcdef1234567890abcdef1234567890`

**Save this string** — you'll need it in the next step and again when configuring the Android app.

---

#### 2.6 — Deploy to Vercel (First Deploy)

From inside the `backend/` folder:

```bash
vercel
```

The CLI will ask you a series of questions. Here's what to answer:

```
? Set up and deploy "~/escape-call/backend"? [Y/n]
→ Y

? Which scope do you want to deploy to?
→ Select your account (there's usually only one)

? Link to existing project? [y/N]
→ N

? What's your project's name?
→ escape-call-backend  (or any name you like)

? In which directory is your code located?
→ ./  (just press Enter — you're already in backend/)

? Want to modify these settings? [y/N]
→ N
```

Vercel will build and deploy. You'll see output like:

```
🔗 Linked to your-username/escape-call-backend
🔍 Inspect: https://vercel.com/your-username/escape-call-backend/...
✅ Preview: https://escape-call-backend-abc123xyz.vercel.app
```

The **Preview URL** is a temporary deployment. We'll create the production deployment after setting environment variables.

> **Note:** This first deploy won't work yet because the environment variables aren't set. That's expected.

---

#### 2.7 — Set Environment Variables

You need to add 5 environment variables to your Vercel project. Run each command below — Vercel will prompt you to enter the value interactively (your input stays hidden):

```bash
vercel env add TWILIO_ACCOUNT_SID
```
```
? What's the value of TWILIO_ACCOUNT_SID?
→ ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  (paste your Account SID from Twilio Console)

? Add TWILIO_ACCOUNT_SID to which Environments?
→ Select all: Production, Preview, Development (press 'a' then Enter)
```

Repeat for each remaining variable:

```bash
vercel env add TWILIO_AUTH_TOKEN
# → Paste your Twilio Auth Token

vercel env add TWILIO_PHONE_NUMBER
# → Your Twilio number in E.164 format, e.g., +12025551234

vercel env add USER_PHONE_NUMBER
# → YOUR personal phone number in E.164 format, e.g., +353851234567

vercel env add ESCAPE_SECRET_TOKEN
# → The random string you generated in step 2.5
```

**Verify all 5 are set:**

```bash
vercel env ls
```

You should see:

```
> Environment Variables found in Project escape-call-backend:

  TWILIO_ACCOUNT_SID     Production, Preview, Development
  TWILIO_AUTH_TOKEN       Production, Preview, Development
  TWILIO_PHONE_NUMBER    Production, Preview, Development
  USER_PHONE_NUMBER      Production, Preview, Development
  ESCAPE_SECRET_TOKEN    Production, Preview, Development
```

---

#### 2.8 — Deploy to Production

Environment variables aren't applied retroactively — you need to redeploy:

```bash
vercel --prod
```

This creates your **production URL**:

```
✅ Production: https://escape-call-backend.vercel.app
```

**This is your live backend URL.** Copy it — you'll need it for the Android app's `Config.kt`.

> **Tip:** Your production URL format is `https://<project-name>.vercel.app`. You can also find it anytime at [vercel.com/dashboard](https://vercel.com/dashboard).

---

#### 2.9 — Test the Endpoints

**Test 1: Verify the TwiML endpoint returns XML**

```bash
curl https://YOUR-PROJECT.vercel.app/api/twiml-silent
```

Expected output:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Response>
  <Pause length="30"/>
</Response>
```

**Test 2: Trigger an actual call to your phone**

```bash
curl -X POST https://YOUR-PROJECT.vercel.app/api/trigger-call \
  -H "Content-Type: application/json" \
  -H "X-Escape-Token: YOUR_SECRET_TOKEN_HERE" \
  -d '{}'
```

Replace `YOUR-PROJECT` with your actual Vercel project name and `YOUR_SECRET_TOKEN_HERE` with the token from step 2.5.

**Expected response:**

```json
{"success":true,"callSid":"CA..."}
```

**Your phone should ring within 3–8 seconds!** When you answer, it will be silent for 30 seconds, then hang up.

**Test 3: Verify security (should fail without token)**

```bash
curl -X POST https://YOUR-PROJECT.vercel.app/api/trigger-call \
  -H "Content-Type: application/json" \
  -d '{}'
```

Expected:

```json
{"error":"Unauthorized"}
```

---

#### 2.10 — Troubleshooting Deployment Issues

| Problem | Cause | Fix |
|---------|-------|-----|
| `vercel: command not found` | CLI not installed or not in PATH | Run `npm install -g vercel` again, or use `npx vercel` instead |
| Deploy succeeds but API returns 500 | Missing environment variables | Run `vercel env ls` to check, then `vercel --prod` to redeploy |
| `"Server misconfigured"` response | One or more env vars are empty | Double-check all 5 vars with `vercel env ls` |
| Twilio error: "unverified number" | Twilio trial account restriction | Go to Twilio Console → **Phone Numbers → Verified Caller IDs** → add your personal number |
| Twilio error: "authenticate" | Wrong Account SID or Auth Token | Verify credentials at [twilio.com/console](https://www.twilio.com/console) |
| Call arrives but shows "Unknown" | Twilio number not saved as contact | Save the Twilio number in your phone's contacts |
| `npm install` fails | Node.js version too old | Update to Node.js 18+ |

> **Vercel Logs:** If something isn't working, check your function logs in the Vercel dashboard: **Project → Deployments → (latest) → Functions tab → View Logs**. This shows `console.log` and `console.error` output from your serverless functions.

---

#### Quick Reference: What You Should Have After Step 2

| Item | Example Value |
|------|---------------|
| Production URL | `https://escape-call-backend.vercel.app` |
| Trigger endpoint | `https://escape-call-backend.vercel.app/api/trigger-call` |
| TwiML endpoint | `https://escape-call-backend.vercel.app/api/twiml-silent` |
| Secret token | `a3f8b2c1d4e5f6...` (your generated string) |

You'll need the **Production URL** and **Secret Token** for Step 3 (Android app configuration).

---

### Step 3: Build & Install the Android App

This step walks you through installing Android Studio, opening the project, configuring it with your backend details, building the APK, and installing it on your phone.

---

#### 3.1 — Download & Install Android Studio

Android Studio is the official IDE for building Android apps. It's free.

1. Go to [developer.android.com/studio](https://developer.android.com/studio)
2. Click **Download Android Studio** (the latest stable version is fine)
3. Install for your operating system:

**macOS:**
- Open the downloaded `.dmg` file
- Drag **Android Studio** into your **Applications** folder
- Launch it from Applications
- If macOS shows a security warning: go to **System Settings → Privacy & Security** and click **Open Anyway**

**Windows:**
- Run the downloaded `.exe` installer
- Accept all defaults (install Android Studio + Android Virtual Device)
- Leave the install location as default (`C:\Program Files\Android\Android Studio`)
- Click **Install**, then **Finish**
- Launch Android Studio from the Start Menu

**Linux (Ubuntu/Debian):**
```bash
# Extract the downloaded tar.gz
sudo tar -xzf android-studio-*.tar.gz -C /opt/

# Launch
/opt/android-studio/bin/studio.sh
```

> **Disk space:** Android Studio + SDK requires ~5–8 GB of free space.

---

#### 3.2 — First-Time Android Studio Setup

When you launch Android Studio for the first time, it runs a setup wizard:

1. **Welcome screen** → Choose **Standard** setup (not Custom)
2. **SDK Components Setup** → It will download:
   - Android SDK (latest version)
   - Android SDK Platform-Tools
   - Android SDK Build-Tools
   - Intel HAXM or equivalent (for emulator — optional for this project)
3. Click **Next** → **Finish** and let it download everything

This initial download takes 5–15 minutes depending on your internet speed.

When complete, you'll see the **Welcome to Android Studio** home screen.

---

#### 3.3 — Open the Escape Call Project

1. On the Welcome screen, click **Open**
2. Navigate to the `escape-call/android` folder (not `escape-call/` — specifically the `android/` subfolder)
3. Click **OK** / **Open**

Android Studio will start loading the project. You'll see:

```
Gradle sync started...
```

**First sync will take a few minutes** as it downloads:
- Kotlin compiler
- AndroidX libraries
- OkHttp networking library
- Material Design components
- Gradle build tools

Wait until you see **"BUILD SUCCESSFUL"** or the green checkmark in the bottom status bar.

> **If Gradle sync fails**, see the troubleshooting section at the bottom of this step.

---

#### 3.4 — Install the Correct Android SDK Version

The project targets SDK 34 (Android 14). If Android Studio warns about a missing SDK:

1. Go to **File → Settings** (Windows/Linux) or **Android Studio → Settings** (macOS)
2. Navigate to **Languages & Frameworks → Android SDK**
3. In the **SDK Platforms** tab:
   - Check the box next to **Android 14.0 ("UpsideDownCake") — API Level 34**
   - Click **Apply** → **OK** to download it
4. In the **SDK Tools** tab, make sure these are checked:
   - **Android SDK Build-Tools 34**
   - **Android SDK Platform-Tools**
5. Click **Apply** if you made changes, then wait for the download

After this, re-sync the project: **File → Sync Project with Gradle Files** (or click the elephant icon with the blue arrow in the toolbar).

---

#### 3.5 — Configure Your Backend URL and Secret Token

This is the critical step — connecting your Android app to your Vercel backend.

1. In the left sidebar (Project view), navigate to:
   ```
   app → src → main → java → com → escapecall → Config.kt
   ```
2. Double-click **Config.kt** to open it
3. Replace the placeholder values with your real ones:

```kotlin
object Config {
    const val BACKEND_URL = "https://escape-call-backend.vercel.app"  // ← Your Vercel production URL from Step 2.8
    const val SECRET_TOKEN = "a3f8b2c1d4e5f6..."                     // ← Same token from Step 2.5
}
```

**Important details:**
- `BACKEND_URL` must have **no trailing slash** (use `https://example.vercel.app` not `https://example.vercel.app/`)
- `SECRET_TOKEN` must be **exactly the same** as the `ESCAPE_SECRET_TOKEN` you set in Vercel env vars
- Both values must be inside the double quotes

Save the file (**Ctrl+S** / **Cmd+S**).

---

#### 3.6 — Enable Developer Mode on Your Android Phone

To install the app directly from Android Studio, you need USB Debugging enabled on your phone.

1. On your Android phone, go to **Settings → About Phone**
2. Find **Build Number** (on some phones it's under **Software Information**)
3. **Tap "Build Number" 7 times rapidly** — you'll see a toast message:
   ```
   "You are now a developer!"
   ```
4. Go back to **Settings → System → Developer Options** (the location varies by phone manufacturer — you may need to search for "Developer Options" in Settings)
5. Enable **USB Debugging** — confirm the security prompt

---

#### 3.7 — Connect Your Phone to Your Computer

1. Plug your Android phone into your computer via USB cable
2. On your phone, a prompt will appear:
   ```
   Allow USB debugging?
   The computer's RSA key fingerprint is: XX:XX:XX...
   ☐ Always allow from this computer
   ```
3. Check **"Always allow from this computer"** and tap **Allow**

**Verify the connection in Android Studio:**
- Look at the **device dropdown** in the top toolbar (near the green Play ▶ button)
- Your phone should appear, e.g.: `Samsung Galaxy S24 (API 34)`

If your phone doesn't appear:
- Try a different USB cable (some cables are charge-only, not data)
- On Windows: you may need to install your phone manufacturer's USB driver
- Try toggling USB Debugging off and on again
- On some phones: change the USB connection mode from "Charging" to "File Transfer / MTP"

> **Alternative — Wireless Debugging (Android 11+):**
> If you prefer wireless, go to **Developer Options → Wireless Debugging → Enable**. In Android Studio, go to **Device Manager → Pair device using Wi-Fi** and scan the QR code shown on your phone.

---

#### 3.8 — Build and Install the App

With your phone connected and selected in the device dropdown:

1. Click the **green Play ▶ button** in the top toolbar
   - Or use the menu: **Run → Run 'app'**
   - Or use the keyboard shortcut: **Shift+F10** (Windows/Linux) / **Ctrl+R** (macOS)

2. Android Studio will:
   - Compile the Kotlin code
   - Build the APK
   - Transfer it to your phone
   - Install and launch the app

First build takes 1–3 minutes. You'll see progress in the **Build** tab at the bottom.

When done, the app will automatically open on your phone showing:
- The **"Escape Call"** title
- A large blue circular **"Trigger Escape Call"** button
- A status text reading **"Ready"**

3. **Test it:** Tap the button. You should see "Calling…" briefly, then "Call incoming! ✓", and your phone should ring within a few seconds.

---

#### 3.9 — Add the Homescreen Widget

The widget gives you one-tap access without opening the app.

1. Go to your Android homescreen
2. **Long-press on an empty area** of the homescreen
3. Tap **Widgets** (usually appears at the bottom)
4. Scroll or search for **"Escape Call"**
5. **Long-press the Escape Call widget** and drag it onto your homescreen
6. Release it where you want it placed

The widget appears as a small **blue circle with a white phone icon** (1×1 grid size).

**Test it:** Tap the widget. A brief, near-invisible notification will flash ("Placing call…"), and your phone should ring within a few seconds.

> **Tip:** Place the widget on a convenient spot — e.g., your first homescreen page for quick thumb access, or even on your lock screen (if your launcher supports lock screen widgets).

---

#### 3.10 — Generate a Standalone APK (Optional)

If you want to install the app without Android Studio (e.g., share it to another device or keep a backup):

**Debug APK (quick, for personal use):**

1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for the build to complete
3. Click **"locate"** in the notification popup, or find the APK at:
   ```
   android/app/build/outputs/apk/debug/app-debug.apk
   ```
4. Transfer this `.apk` file to any Android phone and open it to install

> **Note:** You'll need to enable **"Install from unknown sources"** on the target phone: **Settings → Apps → Special app access → Install unknown apps** → allow your file manager or browser.

**Signed Release APK (optional, for a cleaner build):**

1. **Build → Generate Signed Bundle / APK**
2. Choose **APK**
3. Click **Create new…** to create a keystore:
   - Pick a location and password
   - Fill in at least one field (e.g., your name)
4. Select **release** build type
5. Click **Create**
6. The signed APK will be at:
   ```
   android/app/build/outputs/apk/release/app-release.apk
   ```

---

#### 3.11 — Troubleshooting Android Build Issues

| Problem | Fix |
|---------|-----|
| **Gradle sync fails: "Could not resolve com.android.tools.build"** | Go to **File → Settings → HTTP Proxy** and make sure **Auto-detect** is selected. Check your internet connection. |
| **"SDK location not found"** | Go to **File → Project Structure → SDK Location** and set the Android SDK path (usually `~/Android/Sdk` on Mac/Linux, `C:\Users\<you>\AppData\Local\Android\Sdk` on Windows) |
| **"Failed to find target with hash string 'android-34'"** | Install SDK 34 per step 3.4 |
| **Build error: "Java 17 required"** | Go to **File → Settings → Build → Gradle** → set Gradle JDK to **JetBrains Runtime 17** (bundled with Android Studio) |
| **App installs but crashes on launch** | Check **Logcat** tab at the bottom of Android Studio for the red error. Most likely cause: wrong URL in `Config.kt` |
| **Button press shows "Error: Unable to resolve host"** | Your phone needs internet access. Check WiFi/mobile data is on. Also verify the URL in `Config.kt` has no typos. |
| **Widget tap does nothing** | Grant notification permission: **Settings → Apps → Escape Call → Notifications → Enable**. The widget uses a foreground service that requires notification permission. |
| **Phone doesn't appear in device list** | See the USB troubleshooting tips in step 3.7 |

> **Logcat (your best debugging tool):** In Android Studio, click the **Logcat** tab at the bottom. Filter by `com.escapecall` to see only your app's logs. Any network errors or crashes will appear here in red.

---

#### Quick Reference: What You Should Have After Step 3

| Item | Status |
|------|--------|
| Android Studio installed | ✓ |
| App built and running on phone | ✓ |
| In-app trigger button works | ✓ Phone rings within seconds |
| Homescreen widget placed | ✓ One-tap trigger |
| Twilio number saved as a contact | ✓ Calls show a believable name |

🎉 **You're done!** The full escape call system is live and working.

---

## How It Works

### Flow

1. You tap the button (app) or widget (homescreen)
2. App sends `POST /api/trigger-call` with `X-Escape-Token` header
3. Backend validates the token
4. Backend calls `twilio.calls.create()` with your phone number
5. Twilio calls your phone — it rings like a normal call
6. When you answer, the call is silent (30-second pause, then hangs up)

### Security

- Twilio credentials are **only** on the Vercel backend (env vars)
- The Android app only knows the backend URL and a shared secret token
- The endpoint rejects requests without a valid token
- No phone numbers are stored in the app

---

## Costs

| Service | Free Tier | Expected Cost |
|---------|-----------|---------------|
| Vercel | 100GB bandwidth, serverless functions | $0 |
| Twilio | ~$1/mo for phone number + ~$0.014/min per call | ~$1-2/mo |

For occasional use (a few calls per month), total cost is **~$1/month** (just the Twilio number).

---

## Phase 2 Roadmap (Future)

The architecture is designed for easy extension:

- **Voice playback**: Replace `twiml-silent.js` with `<Say>` or `<Play>` TwiML
- **Multiple personas**: Add a query param to `trigger-call` to select different TwiML responses
- **Random delay**: Add `setTimeout` in the backend before calling Twilio
- **Cooldown**: Track last call time in a simple KV store
- **Scheduled calls**: Use Vercel Cron or similar

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Call doesn't arrive | Check Twilio Console → Call Logs for errors |
| 401 from backend | Ensure `X-Escape-Token` matches `ESCAPE_SECRET_TOKEN` |
| Widget doesn't work | Ensure the app has notification permission (for foreground service) |
| Call shows unknown number | Save the Twilio number as a contact on your phone |
| Twilio trial restrictions | Verify your personal number in Twilio Console → Verified Caller IDs |

> **Twilio Trial Note:** On a free trial, you can only call **verified phone numbers**. Go to Twilio Console → Phone Numbers → Verified Caller IDs and add your personal number.

---

## License

MIT — use freely.
