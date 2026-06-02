package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gun.edu.smartcooking.databinding.FragmentProfileBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUserInfo();
        setupMenuClickListeners();
    }

    private void updateUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.tvProfileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Đầu bếp SavorSmart");
            binding.tvProfileBio.setText(user.getEmail());

            FirebaseHelper.getInstance().getUserProfile(user.getUid(), new FirebaseHelper.UserProfileCallback() {
                @Override
                public void onProfileLoaded(String name, String email, String level, int recipesCount, String followers) {
                    if (!isAdded()) return;
                    if (name != null) binding.tvProfileName.setText(name);
                    if (email != null) binding.tvProfileBio.setText(email);
                    if (level != null) binding.tvLevel.setText(level);
                    binding.tvRecipesCount.setText(String.valueOf(recipesCount));
                    if (followers != null) binding.tvFollowersCount.setText(followers);
                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }

    private void setupMenuClickListeners() {
        binding.menuMyRecipes.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRecipes(true);
            }
        });

        View.OnClickListener devToast = v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Thông báo")
                    .setMessage("Tính năng này đang được phát triển và sẽ sớm ra mắt trong bản cập nhật tới!")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        };

        binding.menuCookingHistory.setOnClickListener(devToast);
        binding.menuSubscribedPlans.setOnClickListener(devToast);
        binding.menuAccountSettings.setOnClickListener(devToast);
        binding.menuHelpSupport.setOnClickListener(devToast);

        binding.btnLogout.setOnClickListener(v -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
