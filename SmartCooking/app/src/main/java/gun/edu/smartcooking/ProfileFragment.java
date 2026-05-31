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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private LinearLayout menuMyRecipes, menuCookingHistory, menuSubscribedPlans;
    private LinearLayout menuAccountSettings, menuHelpSupport;
    private View btnLogout; // Khai báo dạng View để tránh ClassCastException với LinearLayout trong XML
    private TextView tvProfileName, tvProfileBio, tvRecipesCount, tvFollowersCount, tvLevel;

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

        // Ánh xạ các view theo đúng ID và kiểu dữ liệu trong layout mới
        menuMyRecipes = view.findViewById(R.id.menuMyRecipes);
        menuCookingHistory = view.findViewById(R.id.menuCookingHistory);
        menuSubscribedPlans = view.findViewById(R.id.menuSubscribedPlans);
        menuAccountSettings = view.findViewById(R.id.menuAccountSettings);
        menuHelpSupport = view.findViewById(R.id.menuHelpSupport);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvRecipesCount = view.findViewById(R.id.tvRecipesCount);
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount);
        tvLevel = view.findViewById(R.id.tvLevel);

        // Cập nhật thông tin người dùng từ Firebase
        updateUserInfo();

        // Thiết lập sự kiện click
        setupMenuClickListeners();
    }

    private void updateUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvProfileName.setText(user.getDisplayName());
            } else {
                tvProfileName.setText("Đầu bếp SavorSmart");
            }

            if (user.getEmail() != null) {
                tvProfileBio.setText(user.getEmail());
            }
        }
    }

    private void setupMenuClickListeners() {
        if (menuMyRecipes != null) {
            menuMyRecipes.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToRecipes(true);
                }
            });
        }

        // Các tính năng đang phát triển sử dụng Dialog chuyên nghiệp
        View.OnClickListener devToast = v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Thông báo")
                    .setMessage("Tính năng này đang được phát triển và sẽ sớm ra mắt trong bản cập nhật tới!")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        };

        if (menuCookingHistory != null) menuCookingHistory.setOnClickListener(devToast);
        if (menuSubscribedPlans != null) menuSubscribedPlans.setOnClickListener(devToast);
        if (menuAccountSettings != null) menuAccountSettings.setOnClickListener(devToast);
        if (menuHelpSupport != null) menuHelpSupport.setOnClickListener(devToast);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn thoát khỏi ứng dụng SavorSmart?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            if (getActivity() != null) getActivity().finish();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }
}
