package gun.edu.smartcooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private LinearLayout menuMyRecipes, menuCookingHistory, menuSubscribedPlans;
    private LinearLayout menuAccountSettings, menuHelpSupport;
    private LinearLayout btnLogout;
    private TextView tvProfileName, tvProfileBio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view
        menuMyRecipes = view.findViewById(R.id.menuMyRecipes);
        menuCookingHistory = view.findViewById(R.id.menuCookingHistory);
        menuSubscribedPlans = view.findViewById(R.id.menuSubscribedPlans);
        menuAccountSettings = view.findViewById(R.id.menuAccountSettings);
        menuHelpSupport = view.findViewById(R.id.menuHelpSupport);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);

        // Cập nhật thông tin user từ Firebase
        updateUserInfo();

        // Thiết lập sự kiện click cho các menu
        setupMenuClickListeners();
    }

    /**
     * Cập nhật thông tin user từ Firebase Auth
     */
    private void updateUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Hiển thị tên
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvProfileName.setText(user.getDisplayName());
            } else {
                tvProfileName.setText("Smart Chef");
            }

            // Hiển thị email trong bio
            if (user.getEmail() != null) {
                tvProfileBio.setText(user.getEmail());
            }
        }
    }

    /**
     * Thiết lập sự kiện click cho tất cả các mục menu
     */
    private void setupMenuClickListeners() {
        menuMyRecipes.setOnClickListener(v ->
                Toast.makeText(getContext(), "Công thức của tôi", Toast.LENGTH_SHORT).show()
        );

        menuCookingHistory.setOnClickListener(v ->
                Toast.makeText(getContext(), "Lịch sử nấu ăn", Toast.LENGTH_SHORT).show()
        );

        menuSubscribedPlans.setOnClickListener(v ->
                Toast.makeText(getContext(), "Gói đăng ký", Toast.LENGTH_SHORT).show()
        );

        menuAccountSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Cài đặt tài khoản", Toast.LENGTH_SHORT).show()
        );

        menuHelpSupport.setOnClickListener(v ->
                Toast.makeText(getContext(), "Trợ giúp & Hỗ trợ", Toast.LENGTH_SHORT).show()
        );

        // Nút đăng xuất - Firebase sign out
        btnLogout.setOnClickListener(v -> {
            // Đăng xuất Firebase
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
