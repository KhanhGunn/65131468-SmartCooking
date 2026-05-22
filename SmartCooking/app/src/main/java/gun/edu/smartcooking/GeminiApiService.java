package gun.edu.smartcooking;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service gọi Groq API (OpenAI-compatible) để lấy phản hồi từ AI Chef
 */
public class GeminiApiService {

    private static final String TAG = "GeminiApiService";
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private static final String SYSTEM_PROMPT =
            "Bạn là AI Chef - trợ lý nấu ăn thông minh trong ứng dụng SavorSmart. " +
            "Bạn chuyên về:\n" +
            "- Gợi ý công thức nấu ăn dựa trên nguyên liệu có sẵn\n" +
            "- Hướng dẫn cách nấu từng bước chi tiết\n" +
            "- Tư vấn dinh dưỡng và chế độ ăn\n" +
            "Hãy trả lời thân thiện, ngắn gọn và sử dụng emoji. " +
            "Trả lời bằng ngôn ngữ người dùng sử dụng.";

    private final ExecutorService executor;
    private final Handler mainHandler;
    private final List<JSONObject> conversationHistory;

    public GeminiApiService() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        conversationHistory = new ArrayList<>();
        initSystemMessage();
    }

    private void initSystemMessage() {
        try {
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            conversationHistory.add(systemMsg);
        } catch (Exception e) {
            Log.e(TAG, "Error init system prompt", e);
        }
    }

    public void sendMessage(String userMessage, GeminiCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                conversationHistory.add(userMsg);

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 1024);

                JSONArray messages = new JSONArray();
                for (JSONObject msg : conversationHistory) {
                    messages.put(msg);
                }
                requestBody.put("messages", messages);

                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String responseStr = response.toString();

                if (responseCode == 200) {
                    JSONObject json = new JSONObject(responseStr);
                    String aiResponse = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    JSONObject assistantMsg = new JSONObject();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", aiResponse);
                    conversationHistory.add(assistantMsg);

                    mainHandler.post(() -> callback.onSuccess(aiResponse));
                } else {
                    if (conversationHistory.size() > 1) {
                        conversationHistory.remove(conversationHistory.size() - 1);
                    }
                    mainHandler.post(() -> callback.onError("Lỗi server (" + responseCode + ")"));
                }
            } catch (Exception e) {
                if (conversationHistory.size() > 1) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                mainHandler.post(() -> callback.onError("Lỗi kết nối: " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public void clearHistory() {
        if (conversationHistory.isEmpty()) {
            initSystemMessage();
        } else {
            JSONObject systemMsg = conversationHistory.get(0);
            conversationHistory.clear();
            conversationHistory.add(systemMsg);
        }
    }

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}

