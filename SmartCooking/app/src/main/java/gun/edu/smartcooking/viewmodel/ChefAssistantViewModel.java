package gun.edu.smartcooking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import gun.edu.smartcooking.model.ChatMessage;
import gun.edu.smartcooking.network.GeminiApiService;

/**
 * ViewModel dành cho ChefAssistantFragment để lưu và duy trì dữ liệu chat
 * vượt qua vòng đời hủy của fragment (như chuyển tab BottomNavigation).
 */
public class ChefAssistantViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> messageListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isWaitingResponseLiveData = new MutableLiveData<>(false);
    private final GeminiApiService geminiService;

    public ChefAssistantViewModel() {
        geminiService = new GeminiApiService();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messageListLiveData;
    }

    public LiveData<Boolean> getIsWaitingResponse() {
        return isWaitingResponseLiveData;
    }

    public GeminiApiService getGeminiService() {
        return geminiService;
    }

    public void addMessage(ChatMessage message) {
        List<ChatMessage> currentList = messageListLiveData.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.add(message);
        messageListLiveData.setValue(currentList);
    }

    public void setWaitingResponse(boolean isWaiting) {
        isWaitingResponseLiveData.setValue(isWaiting);
    }

    public void clearChat() {
        List<ChatMessage> currentList = messageListLiveData.getValue();
        if (currentList != null) {
            currentList.clear();
            messageListLiveData.setValue(currentList);
        }
        geminiService.clearHistory();
        isWaitingResponseLiveData.setValue(false);
    }
}
