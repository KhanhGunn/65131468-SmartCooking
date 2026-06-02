package gun.edu.smartcooking.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.databinding.ActivityRecipeDetailBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;
import gun.edu.smartcooking.model.Recipe;

/**
 * Activity hiển thị chi tiết công thức nấu ăn
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private ActivityRecipeDetailBinding binding;
    private FirebaseHelper firebaseHelper;
    private String recipeId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        binding.btnBack.setOnClickListener(v -> finish());

        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId != null) {
            loadRecipe(recipeId);
            checkFavoriteStatus();
        }

        binding.btnDetailFavorite.setOnClickListener(v -> toggleFavorite());

        binding.btnStartCooking.setOnClickListener(v -> {
            // Rung phản hồi xúc giác nhẹ (Haptic Feedback) giúp UX chân thật
            v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);

            // Hiển thị Snackbar thông báo thuần Việt có nút hành động
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(),
                    "Đầu bếp SavorSmart ơi! Hãy chuẩn bị nguyên liệu và bắt đầu nấu ăn nhé! 🍳🔥",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .setAction("Đồng ý", view1 -> {})
                    .setActionTextColor(getResources().getColor(R.color.secondary, null))
                    .show();
        });
    }

    private void loadRecipe(String recipeId) {
        firebaseHelper.getRecipeById(recipeId, new FirebaseHelper.RecipeCallback() {
            @Override
            public void onRecipeLoaded(Recipe recipe) {
                displayRecipe(recipe);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RecipeDetailActivity.this,
                        "Lỗi tải công thức: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayRecipe(Recipe recipe) {
        recipe.displayImage(this, binding.ivDetailImage);

        binding.tvDetailName.setText(recipe.getName());
        binding.tvDetailDesc.setText(recipe.getDescription());
        binding.tvDetailTime.setText(recipe.getPrepTimeMinutes() + " phút");
        binding.tvDetailCalories.setText(recipe.getCalories() + " kcal");
        binding.tvDetailRating.setText(String.valueOf(recipe.getRating()));

        String category = recipe.getCategory();
        if (category != null) {
            switch (category) {
                case "breakfast": binding.tvDetailCategory.setText("🌅 Bữa sáng"); break;
                case "lunch": binding.tvDetailCategory.setText("☀️ Bữa trưa"); break;
                case "dinner": binding.tvDetailCategory.setText("🌙 Bữa tối"); break;
                case "healthy": binding.tvDetailCategory.setText("🥗 Dinh dưỡng"); break;
                default: binding.tvDetailCategory.setText(category);
            }
        }

        buildIngredientsSection(recipe.getIngredients());
        buildStepsSection(recipe.getSteps());
    }

    private void buildIngredientsSection(List<String> ingredients) {
        binding.layoutIngredients.removeAllViews();

        if (ingredients == null) return;

        for (int i = 0; i < ingredients.size(); i++) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(dp(14), dp(12), dp(14), dp(12));
            itemLayout.setBackgroundResource(R.drawable.bg_ingredient_item);

            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(8), dp(8));
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_step_number);

            TextView tvIngredient = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMarginStart(dp(14));
            tvIngredient.setLayoutParams(textParams);
            tvIngredient.setText(ingredients.get(i));
            tvIngredient.setTextColor(getColor(R.color.text_primary));
            tvIngredient.setTextSize(14);

            itemLayout.addView(dot);
            itemLayout.addView(tvIngredient);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) layoutParams.topMargin = dp(8);
            itemLayout.setLayoutParams(layoutParams);

            binding.layoutIngredients.addView(itemLayout);
        }
    }

    private void buildStepsSection(List<String> steps) {
        binding.layoutSteps.removeAllViews();

        if (steps == null) return;

        for (int i = 0; i < steps.size(); i++) {
            LinearLayout stepLayout = new LinearLayout(this);
            stepLayout.setOrientation(LinearLayout.HORIZONTAL);
            stepLayout.setPadding(0, dp(8), 0, dp(8));

            TextView tvNumber = new TextView(this);
            LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(dp(28), dp(28));
            numParams.topMargin = dp(2);
            tvNumber.setLayoutParams(numParams);
            tvNumber.setBackgroundResource(R.drawable.bg_step_number);
            tvNumber.setText(String.valueOf(i + 1));
            tvNumber.setTextColor(Color.WHITE);
            tvNumber.setTextSize(12);
            tvNumber.setGravity(Gravity.CENTER);

            TextView tvStep = new TextView(this);
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            stepParams.setMarginStart(dp(14));
            tvStep.setLayoutParams(stepParams);
            tvStep.setText(steps.get(i));
            tvStep.setTextColor(getColor(R.color.text_primary));
            tvStep.setTextSize(14);
            tvStep.setLineSpacing(dp(3), 1);

            stepLayout.addView(tvNumber);
            stepLayout.addView(tvStep);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) layoutParams.topMargin = dp(4);
            stepLayout.setLayoutParams(layoutParams);

            binding.layoutSteps.addView(stepLayout);

            if (i < steps.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                divParams.setMarginStart(dp(42));
                divParams.topMargin = dp(8);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(getColor(R.color.divider));
                binding.layoutSteps.addView(divider);
            }
        }
    }

    private void checkFavoriteStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || recipeId == null) return;

        firebaseHelper.isFavorite(user.getUid(), recipeId, result -> {
            isFavorite = result;
            updateFavoriteIcon();
        });
    }

    private void toggleFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.toggleFavorite(user.getUid(), recipeId, fav -> {
            isFavorite = fav;
            updateFavoriteIcon();
            Toast.makeText(this,
                    fav ? "Đã thêm vào yêu thích ❤️" : "Đã bỏ yêu thích",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFavoriteIcon() {
        binding.btnDetailFavorite.setAlpha(isFavorite ? 1.0f : 0.6f);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
