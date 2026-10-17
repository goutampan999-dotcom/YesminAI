package com.yesmin.ai

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesmin.ai.ai.GeminiService
import com.yesmin.ai.commands.CommandResult
import com.yesmin.ai.commands.CommandRouter
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var commandRouter: CommandRouter
    private val geminiService = GeminiService(apiKey = "YOUR_GEMINI_API_KEY")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        commandRouter = CommandRouter(this)

        setContent {
            YesminMainScreen(
                onSendMessage = { query, onComplete ->
                    handleUserQuery(query, onComplete)
                }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    private fun handleUserQuery(query: String, onComplete: (String) -> Unit) {
        when (val result = commandRouter.processCommand(query)) {
            is CommandResult.Handled -> {
                speak(result.message)
                onComplete(result.message)
            }
            is CommandResult.ForwardToAI -> {
                kotlinx.coroutines.GlobalScope.launch {
                    val aiResponse = geminiService.generateResponse(result.prompt)
                    speak(aiResponse)
                    onComplete(aiResponse)
                }
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun YesminMainScreen(onSendMessage: (String, (String) -> Unit) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("Namaste! Main YESMIN AI hoon. Main aapki kya madad kar sakti hoon?") }
    var isProcessing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF090D16)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "YESMIN AI",
                color = Color(0xFF00E5FF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Futuristic Glowing AI Core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .scale(if (isProcessing) pulseScale else 1f)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFF9D00FF), Color(0xFF00E5FF))),
                        shape = CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF131A2A))),
                            shape = CircleShape
                        )
                )
            }

            // AI Response Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 220.dp)
            ) {
                Text(
                    text = responseText,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 15.sp
                )
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Kahiye ya type karein...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF171F33),
                        unfocusedContainerColor = Color(0xFF171F33),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val msg = inputText
                            inputText = ""
                            isProcessing = true
                            onSendMessage(msg) { reply ->
                                responseText = reply
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF00E5FF), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}
