package com.escapecall

/**
 * ┌─────────────────────────────────────────────────┐
 * │  CONFIGURE THESE VALUES BEFORE BUILDING THE APP │
 * └─────────────────────────────────────────────────┘
 *
 * BACKEND_URL  → Your deployed Vercel URL (no trailing slash)
 * SECRET_TOKEN → Must match ESCAPE_SECRET_TOKEN in your Vercel env vars
 */
object Config {
    const val BACKEND_URL = "https://your-project.vercel.app"
    const val SECRET_TOKEN = "your_random_secret_token_here"
}
