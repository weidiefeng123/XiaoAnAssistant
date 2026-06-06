package com.xiaoan.assistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "XiaoAnAssistant";
    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static final String WAKE_WORD = "小安小安";
    private static final String WAKE_RESPONSE = "你好，我在";

    // UI组件
    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private TextInputEditText etInput;
    private ImageButton btnSend;
    private ImageButton btnMic;
    private LinearLayout voiceOverlay;
    private TextView tvPartialResult;
    private TextView tvStatus;

    // 语音相关
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private boolean isListening = false;
    private boolean isWakeupMode = true; // 启动后先进入唤醒监听模式

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String username = getIntent().getStringExtra("username");
        Toast.makeText(this, getString(R.string.welcome_message, username), Toast.LENGTH_SHORT).show();

        initViews();
        initChat();
        checkAudioPermission();
        initTTS();

        // 添加欢迎消息
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_ASSISTANT,
                "你好！" + username + "，我是小安助手。说出「小安小安」可以唤醒我，也可以点击麦克风按钮直接对话。"));
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        voiceOverlay = findViewById(R.id.voiceOverlay);
        tvPartialResult = findViewById(R.id.tvPartialResult);
        tvStatus = findViewById(R.id.tvStatus);

        btnSend.setOnClickListener(v -> sendTextMessage());
        btnMic.setOnClickListener(v -> toggleListening());

        findViewById(R.id.btnStopListening).setOnClickListener(v -> stopListening());
    }

    private void initChat() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    // ========== 权限处理 ==========

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        } else {
            initSpeechRecognizer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSpeechRecognizer();
            } else {
                Toast.makeText(this, "需要麦克风权限才能使用语音功能", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ========== TTS 初始化 ==========

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Chinese language not supported, trying default");
                textToSpeech.setLanguage(Locale.getDefault());
            }
            isTtsReady = true;

            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {}

                @Override
                public void onDone(String utteranceId) {
                    // TTS播放完毕后，如果是唤醒响应，继续监听
                    if ("wakeup_response".equals(utteranceId)) {
                        handler.post(() -> {
                            tvStatus.setText("聆听中...");
                            startListeningForCommand();
                        });
                    }
                }

                @Override
                public void onError(String utteranceId) {}
            });
        } else {
            Log.e(TAG, "TTS initialization failed");
        }
    }

    private void speak(String text, String utteranceId) {
        if (isTtsReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    // ========== 语音识别 (ASR) 初始化 ==========

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new XiaoAnRecognitionListener());
        } else {
            Toast.makeText(this, "此设备不支持语音识别", Toast.LENGTH_LONG).show();
        }
    }

    private void startListening() {
        if (speechRecognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
            voiceOverlay.setVisibility(View.VISIBLE);
            tvPartialResult.setText("");
            tvStatus.setText("聆听中...");
        } catch (Exception e) {
            Log.e(TAG, "startListening error: " + e.getMessage());
            Toast.makeText(this, "语音识别启动失败，请检查是否安装了语音输入法", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 唤醒后进入指令监听模式
     */
    private void startListeningForCommand() {
        if (speechRecognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
        } catch (Exception e) {
            Log.e(TAG, "startListeningForCommand error: " + e.getMessage());
        }
    }

    private void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
        voiceOverlay.setVisibility(View.GONE);
        tvStatus.setText("待命中");
    }

    private void toggleListening() {
        if (isListening) {
            stopListening();
        } else {
            isWakeupMode = false; // 手动点击时为对话模式
            startListening();
        }
    }

    // ========== 语音识别回调 ==========

    private class XiaoAnRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            tvPartialResult.setText("正在聆听...");
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            isListening = false;
            voiceOverlay.setVisibility(View.GONE);
        }

        @Override
        public void onError(int error) {
            isListening = false;
            voiceOverlay.setVisibility(View.GONE);

            String errorMsg;
            switch (error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = "未能识别";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = "语音超时";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = "网络错误";
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = "音频错误";
                    break;
                default:
                    errorMsg = "识别错误(" + error + ")";
            }
            Log.w(TAG, "ASR Error: " + errorMsg);
            tvStatus.setText("待命中");

            // 如果是唤醒模式且出现超时或无匹配，继续监听
            if (isWakeupMode && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                handler.postDelayed(() -> {
                    if (isWakeupMode) {
                        startListening();
                    }
                }, 500);
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String recognizedText = matches.get(0);
                processRecognizedText(recognizedText);
            }
            tvStatus.setText("待命中");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (partial != null && !partial.isEmpty()) {
                String partialText = partial.get(0);
                tvPartialResult.setText(partialText);

                // 实时检测唤醒词
                if (isWakeupMode && partialText.contains(WAKE_WORD)) {
                    handleWakeup();
                }
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}
    }

    // ========== 唤醒词处理 ==========

    private void handleWakeup() {
        Log.d(TAG, "Wakeup word detected!");
        isWakeupMode = false;
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
        voiceOverlay.setVisibility(View.GONE);

        // 显示助手回复
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_ASSISTANT, WAKE_RESPONSE));
        scrollToBottom();

        // TTS 回答
        tvStatus.setText("回答中...");
        speak(WAKE_RESPONSE, "wakeup_response");
    }

    // ========== 识别结果处理 ==========

    private void processRecognizedText(String text) {
        if (text == null || text.trim().isEmpty()) return;

        // 检查唤醒词
        if (text.contains(WAKE_WORD)) {
            handleWakeup();
            return;
        }

        // 显示用户消息
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_USER, text));
        scrollToBottom();

        // 如果是唤醒后的对话模式，生成助手回复
        if (!isWakeupMode) {
            String response = generateResponse(text);
            chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_ASSISTANT, response));
            scrollToBottom();
            speak(response, "response_" + System.currentTimeMillis());

            // 对话结束后，重新进入唤醒监听模式
            isWakeupMode = true;
            handler.postDelayed(() -> {
                if (isWakeupMode) {
                    tvStatus.setText("唤醒监听中...");
                    startListening();
                }
            }, 3000);
        } else {
            // 唤醒模式但没检测到唤醒词，继续监听
            handler.postDelayed(() -> {
                if (isWakeupMode) {
                    startListening();
                }
            }, 500);
        }
    }

    /**
     * 简单的回复生成（可扩展为接入大模型API）
     */
    private String generateResponse(String userText) {
        String lower = userText.toLowerCase();

        if (lower.contains("你好") || lower.contains("您好") || lower.contains("hi") || lower.contains("hello")) {
            return "你好！我是小安助手，有什么可以帮你的吗？";
        }
        if (lower.contains("时间") || lower.contains("几点")) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("现在是yyyy年MM月dd日 HH点mm分", Locale.CHINESE);
            return sdf.format(new java.util.Date());
        }
        if (lower.contains("天气")) {
            return "抱歉，我暂时还无法获取天气信息，后续会为您接入天气服务。";
        }
        if (lower.contains("你是谁") || lower.contains("你叫什么")) {
            return "我是小安助手，您的智能语音助手，随时为您服务！";
        }
        if (lower.contains("再见") || lower.contains("拜拜") || lower.contains("bye")) {
            return "再见！随时呼唤「小安小安」唤醒我。";
        }
        if (lower.contains("谢谢") || lower.contains("感谢")) {
            return "不客气，很高兴能帮到你！";
        }

        // 默认回复
        return "我听到了您说：「" + userText + "」。这是演示版本，后续将接入更智能的对话能力。";
    }

    // ========== 文本输入 ==========

    private void sendTextMessage() {
        String text = etInput.getText() != null ? etInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_USER, text));
        scrollToBottom();
        etInput.setText("");

        // 检查唤醒词
        if (text.contains(WAKE_WORD)) {
            chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_ASSISTANT, WAKE_RESPONSE));
            scrollToBottom();
            speak(WAKE_RESPONSE, "wakeup_response_text");
            return;
        }

        String response = generateResponse(text);
        chatAdapter.addMessage(new ChatMessage(ChatMessage.TYPE_ASSISTANT, response));
        scrollToBottom();
        speak(response, "response_" + System.currentTimeMillis());
    }

    private void scrollToBottom() {
        rvChat.post(() -> rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    // ========== 生命周期 ==========

    @Override
    protected void onResume() {
        super.onResume();
        // 进入唤醒监听模式
        if (speechRecognizer != null && !isListening) {
            isWakeupMode = true;
            handler.postDelayed(() -> {
                if (isWakeupMode) {
                    tvStatus.setText("唤醒监听中...");
                    startListening();
                }
            }, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isListening && speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
        voiceOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
