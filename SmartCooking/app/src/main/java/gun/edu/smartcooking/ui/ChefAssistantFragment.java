package gun.edu.smartcooking.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.adapter.ChatAdapter;
import gun.edu.smartcooking.databinding.FragmentChefAssistantBinding;
import gun.edu.smartcooking.model.ChatMessage;
import gun.edu.smartcooking.network.GeminiApiService;
import gun.edu.smartcooking.viewmodel.ChefAssistantViewModel;

/**
 * Fragment cho trang Chef Assistant - Chat AI với Gemini
 */
public class ChefAssistantFragment extends Fragment {

    private FragmentChefAssistantBinding binding;
    private ChefAssistantViewModel viewModel;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChefAssistantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ChefAssistantViewModel.class);

        messageList = viewModel.getMessages().getValue();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(chatAdapter);

        viewModel.getMessages().observe(getViewLifecycleOwner(), chatMessages -> {
            chatAdapter.notifyDataSetChanged();
            scrollToBottom();
            
            if (chatMessages != null && chatMessages.size() > 1) {
                binding.svSuggestions.setVisibility(View.GONE);
            } else {
                binding.svSuggestions.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsWaitingResponse().observe(getViewLifecycleOwner(), isWaiting -> {
            binding.btnSend.setAlpha(isWaiting ? 0.5f : 1.0f);
        });

        if (messageList == null || messageList.isEmpty()) {
            addAiMessage(getString(R.string.chat_welcome));
        }

        // Tải API Key từ SharedPreferences nếu có
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("savor_smart_prefs", Context.MODE_PRIVATE);
        String savedKey = prefs.getString("groq_api_key", "");
        if (!savedKey.isEmpty()) {
            viewModel.getGeminiService().setCustomApiKey(savedKey);
        }

        // Cài đặt nút Cấu hình API Key
        binding.btnSettings.setOnClickListener(v -> {
            android.widget.EditText etKeyInput = new android.widget.EditText(requireContext());
            etKeyInput.setHint("Nhập Groq API Key (gsk_...)");
            etKeyInput.setText(prefs.getString("groq_api_key", ""));
            etKeyInput.setPadding(60, 40, 60, 40);

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("🔑 Cấu hình Groq API Key")
                    .setMessage("Nhập mã API Key cá nhân từ Groq Console (https://console.groq.com) của bạn để tiếp tục trò chuyện ổn định:")
                    .setView(etKeyInput)
                    .setPositiveButton("Lưu cấu hình", (dialog, which) -> {
                        String newKey = etKeyInput.getText().toString().trim();
                        prefs.edit().putString("groq_api_key", newKey).apply();
                        viewModel.getGeminiService().setCustomApiKey(newKey);
                        Toast.makeText(getContext(), "Đã lưu API Key mới thành công! 🎉", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnClearChat.setOnClickListener(v -> clearChat());

        binding.chipSuggestion1.setOnClickListener(v -> sendSuggestion(binding.chipSuggestion1.getText().toString()));
        binding.chipSuggestion2.setOnClickListener(v -> sendSuggestion(binding.chipSuggestion2.getText().toString()));
        binding.chipSuggestion3.setOnClickListener(v -> sendSuggestion(binding.chipSuggestion3.getText().toString()));

        binding.btnMic.setOnClickListener(v ->
                Toast.makeText(getContext(), "Nhập giọng nói", Toast.LENGTH_SHORT).show());

        Bundle args = getArguments();
        if (args != null && args.containsKey("auto_query")) {
            String autoQuery = args.getString("auto_query");
            if (autoQuery != null && !autoQuery.isEmpty()) {
                view.postDelayed(() -> sendSuggestion(autoQuery), 350);
                args.remove("auto_query");
            }
        }
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        Boolean isWaiting = viewModel.getIsWaitingResponse().getValue();
        if (TextUtils.isEmpty(text) || (isWaiting != null && isWaiting)) return;

        binding.svSuggestions.setVisibility(View.GONE);

        addUserMessage(text);
        binding.etMessage.setText("");

        hideKeyboard();
        callGeminiApi(text);
    }

    private void sendSuggestion(String suggestion) {
        if (binding == null) return;
        Boolean isWaiting = viewModel.getIsWaitingResponse().getValue();
        if (isWaiting != null && isWaiting) return;

        binding.svSuggestions.setVisibility(View.GONE);
        addUserMessage(suggestion);
        callGeminiApi(suggestion);
    }

    private void callGeminiApi(String userMessage) {
        viewModel.setWaitingResponse(true);

        addAiMessage(getString(R.string.chat_typing));
        int typingPosition = messageList.size() - 1;

        viewModel.getGeminiService().sendMessage(userMessage, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded() || binding == null) return;
                viewModel.setWaitingResponse(false);

                if (typingPosition < messageList.size()) {
                    messageList.get(typingPosition).setMessage(response);
                    chatAdapter.notifyItemChanged(typingPosition);
                    scrollToBottom();
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded() || binding == null) return;
                viewModel.setWaitingResponse(false);

                if (typingPosition < messageList.size()) {
                    String errorMsg = getString(R.string.chat_error) + "\n(" + error + ")";
                    messageList.get(typingPosition).setMessage(errorMsg);
                    chatAdapter.notifyItemChanged(typingPosition);
                    scrollToBottom();
                }
            }
        });
    }

    private void addAiMessage(String message) {
        viewModel.addMessage(new ChatMessage(message, false));
    }

    private void addUserMessage(String message) {
        viewModel.addMessage(new ChatMessage(message, true));
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            binding.rvMessages.post(() ->
                    binding.rvMessages.smoothScrollToPosition(messageList.size() - 1));
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && binding.etMessage != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.etMessage.getWindowToken(), 0);
        }
    }

    private void clearChat() {
        viewModel.clearChat();
        addAiMessage(getString(R.string.chat_welcome));
        binding.svSuggestions.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), getString(R.string.chat_cleared), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
