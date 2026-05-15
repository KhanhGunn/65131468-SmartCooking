package gun.edu.smartcooking;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment cho trang Chef Assistant - Chat AI với Gemini
 */
public class ChefAssistantFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend;
    private View btnClearChat;
    private HorizontalScrollView svSuggestions;
    private TextView chipSuggestion1, chipSuggestion2, chipSuggestion3;

    private List<ChatMessage> messageList;
    private ChatAdapter chatAdapter;
    private GeminiApiService geminiService;

    private boolean isWaitingResponse = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chef_assistant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnClearChat = view.findViewById(R.id.btnClearChat);
        svSuggestions = view.findViewById(R.id.svSuggestions);
        chipSuggestion1 = view.findViewById(R.id.chipSuggestion1);
        chipSuggestion2 = view.findViewById(R.id.chipSuggestion2);
        chipSuggestion3 = view.findViewById(R.id.chipSuggestion3);

        // Khởi tạo service và data
        geminiService = new GeminiApiService();
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(chatAdapter);

        // Tin nhắn chào mặc định từ AI
        addAiMessage(getString(R.string.chat_welcome));

        // Sự kiện gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMessage());

        // Sự kiện Clear chat
        btnClearChat.setOnClickListener(v -> clearChat());

        // Sự kiện suggestion chips
        chipSuggestion1.setOnClickListener(v -> sendSuggestion(chipSuggestion1.getText().toString()));
        chipSuggestion2.setOnClickListener(v -> sendSuggestion(chipSuggestion2.getText().toString()));
        chipSuggestion3.setOnClickListener(v -> sendSuggestion(chipSuggestion3.getText().toString()));

        // Mic (placeholder)
        View btnMic = view.findViewById(R.id.btnMic);
        btnMic.setOnClickListener(v ->
                Toast.makeText(getContext(), "Nhập giọng nói", Toast.LENGTH_SHORT).show());
    }

    /**
     * Gửi tin nhắn từ ô input
     */
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text) || isWaitingResponse) return;

        // Ẩn suggestions sau khi gửi tin nhắn đầu tiên
        svSuggestions.setVisibility(View.GONE);

        // Thêm tin nhắn user
        addUserMessage(text);
        etMessage.setText("");

        // Ẩn bàn phím
        hideKeyboard();

        // Gọi Gemini API
        callGeminiApi(text);
    }

    /**
     * Gửi suggestion chip
     */
    private void sendSuggestion(String suggestion) {
        if (isWaitingResponse) return;

        svSuggestions.setVisibility(View.GONE);
        addUserMessage(suggestion);
        callGeminiApi(suggestion);
    }

    /**
     * Gọi Gemini API và hiển thị phản hồi
     */
    private void callGeminiApi(String userMessage) {
        isWaitingResponse = true;
        btnSend.setAlpha(0.5f);

        // Thêm typing indicator
        addAiMessage(getString(R.string.chat_typing));
        int typingPosition = messageList.size() - 1;

        geminiService.sendMessage(userMessage, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()) return;
                isWaitingResponse = false;

                // Thay thế typing indicator bằng phản hồi thực
                if (typingPosition < messageList.size()) {
                    messageList.get(typingPosition).setMessage(response);
                    chatAdapter.notifyItemChanged(typingPosition);
                    scrollToBottom();
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                isWaitingResponse = false;

                // Thay thế typing indicator bằng thông báo lỗi chi tiết
                if (typingPosition < messageList.size()) {
                    String errorMsg = getString(R.string.chat_error) + "\n(" + error + ")";
                    messageList.get(typingPosition).setMessage(errorMsg);
                    chatAdapter.notifyItemChanged(typingPosition);
                    scrollToBottom();
                }
            }
        });
    }

    /**
     * Thêm tin nhắn AI vào danh sách
     */
    private void addAiMessage(String message) {
        messageList.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    /**
     * Thêm tin nhắn User vào danh sách
     */
    private void addUserMessage(String message) {
        messageList.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    /**
     * Cuộn xuống tin nhắn mới nhất
     */
    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.post(() ->
                    rvMessages.smoothScrollToPosition(messageList.size() - 1));
        }
    }

    /**
     * Ẩn bàn phím
     */
    private void hideKeyboard() {
        if (getActivity() != null && etMessage != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

    /**
     * Xóa toàn bộ cuộc trò chuyện
     */
    private void clearChat() {
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
        geminiService.clearHistory();
        isWaitingResponse = false;

        // Reset lại tin nhắn chào
        addAiMessage(getString(R.string.chat_welcome));
        svSuggestions.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), getString(R.string.chat_cleared), Toast.LENGTH_SHORT).show();
    }
}
