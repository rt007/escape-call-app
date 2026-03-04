package com.escapecall

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.escapecall.network.ApiClient
import com.escapecall.network.TriggerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var triggerButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerButton = findViewById(R.id.btn_trigger)
        statusText = findViewById(R.id.tv_status)

        triggerButton.setOnClickListener {
            triggerEscapeCall()
        }
    }

    private fun triggerEscapeCall() {
        // Immediately update UI
        triggerButton.isEnabled = false
        triggerButton.text = "Calling…"
        statusText.text = "Requesting call…"

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = ApiClient.triggerCall()) {
                is TriggerResult.Success -> {
                    statusText.text = "Call incoming! ✓"
                }
                is TriggerResult.Error -> {
                    statusText.text = "Error: ${result.message}"
                }
            }

            // Re-enable after a brief cooldown
            triggerButton.isEnabled = true
            triggerButton.text = "Trigger Escape Call"
        }
    }
}
