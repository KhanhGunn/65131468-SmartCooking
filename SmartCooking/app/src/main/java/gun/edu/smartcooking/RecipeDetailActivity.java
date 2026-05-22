package gun.edu.smartcooking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Activity hiển thị chi tiết công thức nấu ăn
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private ImageView ivDetailImage, btnBack, btnDetailFavorite;
    private TextView tvDetailName, tvDetailDesc, tvDetailCategory;
    private TextView tvDetailTime, tvDetailCalories, tvDetailRating;
    private LinearLayout layoutIngredients, layoutSteps;

    private FirebaseHelper firebaseHelper;
    private String recipeId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_recipe_detail);

        // Init views
        ivDetailImage = findViewById(R.id.ivDetailImage);
        btnBack = findViewById(R.id.btnBack);
        btnDetailFavorite = findViewById(R.id.btnDetailFavorite);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailCalories = findViewById(R.id.tvDetailCalories);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        layoutIngredients = findViewById(R.id.layoutIngredients);
        layoutSteps = findViewById(R.id.layoutSteps);

        firebaseHelper = FirebaseHelper.getInstance();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId != null) {
            loadRecipe(recipeId);
            checkFavoriteStatus();
        }

        // Favorite button
        btnDetailFavorite.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * Load dữ liệu công thức từ Firebase
     */
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

    /**
     * Hiển thị dữ liệu công thức lên UI
     */
    private void displayRecipe(Recipe recipe) {
        // Image
        recipe.displayImage(this, ivDetailImage);

        // Basic info
        tvDetailName.setText(recipe.getName());
        tvDetailDesc.setText(recipe.getDescription());
        tvDetailTime.setText(recipe.getPrepTimeMinutes() + " phút");
        tvDetailCalories.setText(recipe.getCalories() + " kcal");
        tvDetailRating.setText(String.valueOf(recipe.getRating()));

        // Category
        String category = recipe.getCategory();
        if (category != null) {
            switch (category) {
                case "breakfast": tvDetailCategory.setText("🌅 Bữa sáng"); break;
                case "lunch": tvDetailCategory.setText("☀️ Bữa trưa"); break;
                case "dinner": tvDetailCategory.setText("🌙 Bữa tối"); break;
                case "healthy": tvDetailCategory.setText("🥗 Healthy"); break;
                default: tvDetailCategory.setText(category);
            }
        }

        // Ingredients
        buildIngredientsSection(recipe.getIngredients());

        // Steps
        buildStepsSection(recipe.getSteps());
    }

    /**
     * Tạo danh sách nguyên liệu
     */
    private void buildIngredientsSection(List<String> ingredients) {
        layoutIngredients.removeAllViews();

        if (ingredients == null) return;

        for (int i = 0; i < ingredients.size(); i++) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(dp(14), dp(12), dp(14), dp(12));
            itemLayout.setBackgroundResource(R.drawable.bg_ingredient_item);

            // Dot indicator
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(8), dp(8));
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_step_number);

            // Text
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

            // Margin between items
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) layoutParams.topMargin = dp(8);
            itemLayout.setLayoutParams(layoutParams);

            layoutIngredients.addView(itemLayout);
        }
    }

    /**
     * Tạo danh sách các bước thực hiện
     */
    private void buildStepsSection(List<String> steps) {
        layoutSteps.removeAllViews();

        if (steps == null) return;

        for (int i = 0; i < steps.size(); i++) {
            LinearLayout stepLayout = new LinearLayout(this);
            stepLayout.setOrientation(LinearLayout.HORIZONTAL);
            stepLayout.setPadding(0, dp(8), 0, dp(8));

            // Step number circle
            TextView tvNumber = new TextView(this);
            LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(dp(28), dp(28));
            numParams.topMargin = dp(2);
            tvNumber.setLayoutParams(numParams);
            tvNumber.setBackgroundResource(R.drawable.bg_step_number);
            tvNumber.setText(String.valueOf(i + 1));
            tvNumber.setTextColor(Color.WHITE);
            tvNumber.setTextSize(12);
            tvNumber.setGravity(Gravity.CENTER);

            // Step text
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

            // Margin between steps
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) layoutParams.topMargin = dp(4);
            stepLayout.setLayoutParams(layoutParams);

            layoutSteps.addView(stepLayout);

            // Divider line between steps (except last)
            if (i < steps.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                divParams.setMarginStart(dp(42));
                divParams.topMargin = dp(8);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(getColor(R.color.divider));
                layoutSteps.addView(divider);
            }
        }
    }

    /**
     * Check favorite status từ Firebase
     */
    private void checkFavoriteStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || recipeId == null) return;

        firebaseHelper.isFavorite(user.getUid(), recipeId, result -> {
            isFavorite = result;
            updateFavoriteIcon();
        });
    }

    /**
     * Toggle favorite
     */
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
        btnDetailFavorite.setAlpha(isFavorite ? 1.0f : 0.6f);
    }

    /**
     * Helper: dp to pixel
     */
    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
