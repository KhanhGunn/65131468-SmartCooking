package gun.edu.smartcooking.network;

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
 * Tích hợp retry, exponential backoff, timeout và xử lý lỗi nâng cao.
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
    private String customApiKey = null;

    public GeminiApiService() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        conversationHistory = new ArrayList<>();
        initSystemMessage();
    }

    public void setCustomApiKey(String apiKey) {
        this.customApiKey = apiKey;
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

    // PHƯƠNG THỨC GỬI TIN NHẮN ĐI VÀ LẤY PHẢN HỒI TỪ AI (DÙNG ĐỂ VẤN ĐÁP CODE)
    public void sendMessage(String userMessage, GeminiCallback callback) {
        // [QUAN TRỌNG] Đẩy luồng gửi request sang Background Thread bằng ExecutorService để tránh đơ UI luồng chính
        executor.execute(() -> {
            try {
                // Tạo cấu trúc JSON lưu tin nhắn của User
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                conversationHistory.add(userMsg); // Thêm vào lịch sử trò chuyện
            } catch (Exception e) {
                // Chuyển kết quả lỗi về UI Thread (Luồng chính) thông qua Handler
                mainHandler.post(() -> callback.onError("Lỗi khởi tạo tin nhắn: " + e.getMessage()));
                return;
            }

            int maxRetries = 3; // Số lần thử lại tối đa khi lỗi mạng
            int retryDelayMs = 1500; // Thời gian delay khởi điểm (1.5 giây)
            boolean success = false;
            String resultStr = null;
            String errorMsg = "Lỗi kết nối chưa xác định";

            // Cơ chế tự động kết nối lại (Retry Loop) nếu gặp sự cố mạng hoặc quá tải API
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                HttpURLConnection connection = null;
                try {
                    // Dựng Request Body theo đặc tả OpenAI/Groq API
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("model", MODEL);
                    requestBody.put("temperature", 0.7); // Độ sáng tạo của AI
                    requestBody.put("max_tokens", 1024); // Giới hạn token trả về

                    // Nạp lịch sử cuộc trò chuyện vào request để AI hiểu ngữ cảnh chat
                    JSONArray messages = new JSONArray();
                    for (JSONObject msg : conversationHistory) {
                        messages.put(msg);
                    }
                    requestBody.put("messages", messages);

                    // Chọn API Key hoạt động (Key do người dùng nhập hoặc Key mặc định của app)
                    String activeKey = (customApiKey != null && !customApiKey.isEmpty()) ? customApiKey : API_KEY;
// Đoạn này quan trọng  dùng để tạo luồng phụ chạy ngầm kết nối API
                    // Mở kết nối mạng HTTPURLConnection
                    URL url = new URL(API_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    connection.setRequestProperty("Authorization", "Bearer " + activeKey); // Gắn Token Auth
                    connection.setConnectTimeout(15000); // 15 giây timeout kết nối mạng
                    connection.setReadTimeout(20000);    // 20 giây timeout đọc dữ liệu về
                    connection.setDoOutput(true);

                    // Ghi luồng dữ liệu JSON vào Body của Request mạng
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    // Đọc phản hồi HTTP Response Code từ Máy chủ (Groq)
                    int responseCode = connection.getResponseCode();
                    
                    // Nếu responseCode từ 200->299 là thành công, ngược lại là lỗi
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

                    if (responseCode == 200) { // HTTP 200 OK: Thành công
                        JSONObject json = new JSONObject(responseStr);
                        // Bóc tách JSON để lấy nội dung text mà AI trả về
                        resultStr = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        success = true;
                        break; // Thoát khỏi vòng lặp retry vì đã thành công
                    } else if (responseCode == 429) { // HTTP 429 Too Many Requests (Quá tải)
                        errorMsg = "AI đang bận vì quá lượt gọi (429). Đang kết nối lại...";
                        Log.w(TAG, "Rate limited (429). Attempt " + attempt + " of " + maxRetries);
                    } else {
                        errorMsg = "Lỗi máy chủ AI (" + responseCode + ")";
                        Log.e(TAG, "Server error (" + responseCode + "). Attempt " + attempt);
                    }
                } catch (java.net.SocketTimeoutException e) {
                    errorMsg = "Hết hạn kết nối AI. Đang thử lại...";
                    Log.w(TAG, "Timeout. Attempt " + attempt + " of " + maxRetries, e);
                } catch (Exception e) {
                    errorMsg = "Lỗi kết nối mạng: " + e.getMessage();
                    Log.e(TAG, "Connection failure. Attempt " + attempt + " of " + maxRetries, e);
                } finally {
                    if (connection != null) connection.disconnect(); // Đóng kết nối để giải phóng tài nguyên
                }

                // Nếu lỗi và chưa thử hết số lần, ngủ một lúc rồi thử lại (Exponential Backoff)
                if (!success && attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Gấp đôi thời gian chờ lần sau
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            // XỬ LÝ TRẢ KẾT QUẢ VỀ CHO GIAO DIỆN (UI)
            if (success && resultStr != null) {
                try {
                    // Lưu tin nhắn AI vào lịch sử trò chuyện cục bộ
                    JSONObject assistantMsg = new JSONObject();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", resultStr);
                    conversationHistory.add(assistantMsg);

                    final String aiResponse = resultStr;
                    // Handler đẩy phản hồi AI về luồng chính UI để hiển thị lên màn hình chat
                    mainHandler.post(() -> callback.onSuccess(aiResponse));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Lỗi lưu trữ lịch sử: " + e.getMessage()));
                }
            } else {
                // Xóa tin nhắn User vừa gửi khỏi lịch sử nếu cuộc gọi hoàn toàn thất bại
                if (conversationHistory.size() > 1) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                final String finalError = errorMsg.contains("429") || errorMsg.contains("Timeout") || errorMsg.contains("mạng") || errorMsg.contains("connect")
                        ? "Hiện tại trợ lý AI đang quá tải hoặc lỗi kết nối. Vui lòng thử lại sau giây lát."
                        : errorMsg;
                // Đẩy thông tin báo lỗi về luồng chính UI để hiển thị Toast hoặc thông báo
                mainHandler.post(() -> callback.onError(finalError));
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
