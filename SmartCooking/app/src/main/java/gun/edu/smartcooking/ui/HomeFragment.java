package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.databinding.FragmentHomeBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;
import gun.edu.smartcooking.model.Recipe;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateGreeting();
        loadRecipesData();

        // Đăng ký sự kiện Click cho các thành phần UI trang chủ giúp tăng tính tương tác thực tế
        binding.btnAIQuickAccess.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToChefAssistant(null);
            }
        });

        binding.tvViewInventory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToInventory();
            }
        });

        binding.searchContainer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRecipes(false);
            }
        });
    }

    private void updateGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            binding.tvGreeting.setText(getString(R.string.greeting_format, user.getDisplayName()));
        } else {
            binding.tvGreeting.setText("Chào bạn! 👋");
        }
    }

    private void loadRecipesData() {
        FirebaseHelper.getInstance().getAllRecipes(new FirebaseHelper.RecipeListCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                if (!isAdded() || recipes.isEmpty()) return;

                Recipe featured = recipes.get(0);
                featured.displayImage(requireContext(), binding.ivFeaturedImage);
                binding.tvFeaturedName.setText(featured.getName());
                
                binding.cardFeaturedHome.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, featured.getId());
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
