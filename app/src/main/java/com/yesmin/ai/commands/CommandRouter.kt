package com.yesmin.ai.commands

import android.content.Context
import android.content.Intent
import android.net.Uri

sealed class CommandResult {
    data class Handled(val message: String) : CommandResult()
    data class ForwardToAI(val prompt: String) : CommandResult()
}

class CommandRouter(private val context: Context) {

    fun processCommand(input: String): CommandResult {
        val lower = input.lowercase().trim()

        return when {
            lower.contains("youtube kholo") || lower.contains("open youtube") -> {
                launchApp("com.google.android.youtube", "https://www.youtube.com")
                CommandResult.Handled("YouTube open kar rahi hoon...")
            }
            lower.contains("chrome kholo") || lower.contains("open chrome") -> {
                launchApp("com.android.chrome", "https://www.google.com")
                CommandResult.Handled("Google Chrome open kar rahi hoon...")
            }
            lower.contains("gana chalao") || lower.contains("play music") -> {
                val query = lower.replace("gana chalao", "").replace("play", "").trim()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult.Handled("YouTube par '$query' play kar rahi hoon...")
            }
            else -> CommandResult.ForwardToAI(input)
        }
    }

    private fun launchApp(packageName: String, fallbackUrl: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
