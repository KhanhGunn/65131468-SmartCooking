package gun.edu.smartcooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private ImageView ivFeaturedImage, ivPopular1Image, ivPopular2Image;
    private TextView tvFeaturedName, tvPopular1Name, tvPopular2Name;
    private View cardFeatured, cardPopular1, cardPopular2, cardSuggestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ views
        tvGreeting = view.findViewById(R.id.tvGreeting);

        // Cập nhật lời chào với tên user thật
        updateGreeting();

        // Seed dữ liệu công thức nếu chưa có
        FirebaseHelper.getInstance().seedRecipesIfEmpty();

        // Load recipes từ Firebase để cập nhật hình ảnh
        loadRecipesData(view);
    }

    /**
     * Cập nhật lời chào dựa trên Firebase User
     */
    private void updateGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvGreeting.setText("Chào " + user.getDisplayName() + "! 👋");
        } else {
            tvGreeting.setText("Chào bạn! 👋");
        }
    }

    /**
     * Load recipes từ Firebase và cập nhật UI
     */
    private void loadRecipesData(View view) {
        FirebaseHelper.getInstance().getAllRecipes(new FirebaseHelper.RecipeListCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                if (!isAdded() || recipes.isEmpty()) return;

                // Featured recipe (first one)
                if (recipes.size() > 0) {
                    Recipe featured = recipes.get(0);
                    ImageView ivFeatured = view.findViewById(R.id.ivFeaturedImage);
                    TextView tvFeaturedName = view.findViewById(R.id.tvFeaturedName);
                    TextView tvFeaturedDesc = view.findViewById(R.id.tvFeaturedDesc);
                    if (ivFeatured != null) {
                        featured.displayImage(requireContext(), ivFeatured);
                        ivFeatured.setBackground(null);
                    }
                    if (tvFeaturedName != null) {
                        tvFeaturedName.setText(featured.getName());
                    }
                    if (tvFeaturedDesc != null) {
                        tvFeaturedDesc.setText(featured.getDescription());
                    }

                    // Card click → mở chi tiết
                    View cardFeatured = view.findViewById(R.id.cardFeatured);
                    if (cardFeatured != null) {
                        cardFeatured.setOnClickListener(v -> openRecipeDetail(featured.getId()));
                    }
                }

                // Popular recipe 1
                if (recipes.size() > 1) {
                    Recipe popular1 = recipes.get(1);
                    ImageView ivPop1 = view.findViewById(R.id.ivPopular1Image);
                    TextView tvPop1 = view.findViewById(R.id.tvPopular1Name);
                    if (ivPop1 != null) {
                        popular1.displayImage(requireContext(), ivPop1);
                        ivPop1.setBackground(null);
                    }
                    if (tvPop1 != null) {
                        tvPop1.setText(popular1.getName());
                    }

                    View cardPop1 = view.findViewById(R.id.cardPopular1);
                    if (cardPop1 != null) {
                        cardPop1.setOnClickListener(v -> openRecipeDetail(popular1.getId()));
                    }
                }

                // Popular recipe 2
                if (recipes.size() > 2) {
                    Recipe popular2 = recipes.get(2);
                    ImageView ivPop2 = view.findViewById(R.id.ivPopular2Image);
                    TextView tvPop2 = view.findViewById(R.id.tvPopular2Name);
                    if (ivPop2 != null) {
                        popular2.displayImage(requireContext(), ivPop2);
                        ivPop2.setBackground(null);
                    }
                    if (tvPop2 != null) {
                        tvPop2.setText(popular2.getName());
                    }

                    View cardPop2 = view.findViewById(R.id.cardPopular2);
                    if (cardPop2 != null) {
                        cardPop2.setOnClickListener(v -> openRecipeDetail(popular2.getId()));
                    }
                }

                // Plan Recipe (Today's Plan - Quinoa Power Bowl, typically index 3)
                if (recipes.size() > 3) {
                    Recipe planRecipe = recipes.get(3);
                    ImageView ivPlan = view.findViewById(R.id.ivPlanImage);
                    TextView tvPlanName = view.findViewById(R.id.tvPlanName);
                    View cardTodayPlan = view.findViewById(R.id.cardTodayPlan);
                    View btnStartPrep = view.findViewById(R.id.btnStartPrep);

                    if (ivPlan != null) {
                        planRecipe.displayImage(requireContext(), ivPlan);
                        ivPlan.setBackground(null);
                    }
                    if (tvPlanName != null) {
                        tvPlanName.setText(planRecipe.getName());
                    }
                    if (cardTodayPlan != null) {
                        cardTodayPlan.setOnClickListener(v -> openRecipeDetail(planRecipe.getId()));
                    }
                    if (btnStartPrep != null) {
                        btnStartPrep.setOnClickListener(v -> openRecipeDetail(planRecipe.getId()));
                    }
                }

                // Suggestion card (Using index 3 as well or index 0 for fallback)
                if (recipes.size() > 3) {
                    Recipe suggestion = recipes.get(3);
                    View cardSuggestion = view.findViewById(R.id.cardSuggestion);
                    TextView tvSuggestionName = view.findViewById(R.id.tvSuggestionName);
                    if (tvSuggestionName != null) {
                        tvSuggestionName.setText(suggestion.getName());
                    }
                    if (cardSuggestion != null) {
                        cardSuggestion.setOnClickListener(v -> openRecipeDetail(suggestion.getId()));
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Giữ dữ liệu mặc định từ layout
            }
        });
    }

    /**
     * Mở trang chi tiết công thức
     */
    private void openRecipeDetail(String recipeId) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }
}
