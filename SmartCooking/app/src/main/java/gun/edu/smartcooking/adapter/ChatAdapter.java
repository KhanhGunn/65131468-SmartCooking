package gun.edu.smartcooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.model.ChatMessage;

/**
 * Adapter cho RecyclerView hiển thị tin nhắn chat
 * Hỗ trợ 2 loại ViewHolder: AI message và User message
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_AI = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    // GHI ĐÈ PHƯƠNG THỨC PHÂN LOẠI VIEW TYPE: Dùng để xác định tin nhắn này thuộc về ai (để nạp layout tương ứng)
    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isUser()) {
            return VIEW_TYPE_USER; // Tin nhắn từ phía người dùng
        } else if (msg.getMessage() != null && (msg.getMessage().contains("đang suy nghĩ") || msg.getMessage().contains("đang soạn"))) {
            return VIEW_TYPE_LOADING; // Tin nhắn AI đang xử lý (hiển thị chấm xoay tròn/hiệu ứng loading)
        } else {
            return VIEW_TYPE_AI; // Tin nhắn trả lời từ AI Chef
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // [XỬ LÝ ĐA DẠNG LAYOUT] Dựa vào View Type đã phân loại ở trên để nạp (inflate) layout xml tương ứng
        if (viewType == VIEW_TYPE_USER) {
            // Nạp layout bong bóng chat nằm bên phải (màu cam) của người dùng
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            // Nạp layout hiển thị bong bóng loading đang chờ AI trả lời
            View view = inflater.inflate(R.layout.item_message_loading, parent, false);
            return new LoadingMessageViewHolder(view);
        } else {
            // Nạp layout bong bóng chat nằm bên trái (màu xám nhạt) của AI
            View view = inflater.inflate(R.layout.item_message_ai, parent, false);
            return new AiMessageViewHolder(view);
        }
    }

    // GẮN DỮ LIỆU VÀO VIEW: Đổ nội dung tin nhắn dạng text lên TextView tương ứng của mỗi bong bóng chat
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AiMessageViewHolder) {
            ((AiMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size(); // Trả về tổng số lượng tin nhắn trong cuộc hội thoại
    }

    /**
     * ViewHolder cho tin nhắn của AI Chef
     */
    static class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAiMessage;

        AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
        }

        void bind(ChatMessage message) {
            tvAiMessage.setText(message.getMessage());
        }
    }

    /**
     * ViewHolder cho tin nhắn của người dùng
     */
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
        }

        void bind(ChatMessage message) {
            tvUserMessage.setText(message.getMessage());
        }
    }

    /**
     * ViewHolder hiển thị bong bóng AI đang soạn câu trả lời
     */
    static class LoadingMessageViewHolder extends RecyclerView.ViewHolder {
        LoadingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
