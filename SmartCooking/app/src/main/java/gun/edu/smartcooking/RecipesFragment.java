package gun.edu.smartcooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class RecipesFragment extends Fragment {

    private TextView chipAll, chipBreakfast, chipLunch, chipDinner, chipHealthy;
    private TextView[] chips;
    private RecyclerView rvRecipes;
    private ProgressBar progressRecipes;
    private LinearLayout layoutEmpty;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> filteredRecipes = new ArrayList<>();
    private String selectedCategory = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        chipAll = view.findViewById(R.id.chipAll);
        chipBreakfast = view.findViewById(R.id.chipBreakfast);
        chipLunch = view.findViewById(R.id.chipLunch);
        chipDinner = view.findViewById(R.id.chipDinner);
        chipHealthy = view.findViewById(R.id.chipHealthy);
        rvRecipes = view.findViewById(R.id.rvRecipes);
        progressRecipes = view.findViewById(R.id.progressRecipes);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        chips = new TextView[] { chipAll, chipBreakfast, chipLunch, chipDinner, chipHealthy };

        // Setup RecyclerView
        recipeAdapter = new RecipeAdapter(requireContext(), filteredRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecipes.setAdapter(recipeAdapter);

        // Click listener cho recipe card
        recipeAdapter.setOnRecipeClickListener(recipe -> {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });

        // Favorite listener
        recipeAdapter.setOnFavoriteClickListener((recipe, position) -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseHelper.getInstance().toggleFavorite(user.getUid(), recipe.getId(), isFav -> {
                recipe.setFavorite(isFav);
                recipeAdapter.notifyItemChanged(position);
                Toast.makeText(getContext(),
                        isFav ? "Đã thêm vào yêu thích ❤️" : "Đã bỏ yêu thích",
                        Toast.LENGTH_SHORT).show();
            });
        });

        // Chip click listeners
        for (TextView chip : chips) {
            chip.setOnClickListener(v -> selectChip((TextView) v));
        }

        Bundle args = getArguments();
        if (args != null && args.getBoolean("show_favorites_only", false)) {
            selectedCategory = "favorites";
            for (TextView chip : chips) {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }

        // Load recipes từ Firebase
        loadRecipes();
    }

    /**
     * Load tất cả recipes từ Firebase và đồng bộ trạng thái yêu thích
     */
    private void loadRecipes() {
        progressRecipes.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        FirebaseHelper.getInstance().getAllRecipes(new FirebaseHelper.RecipeListCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                if (!isAdded()) return;

                allRecipes.clear();
                allRecipes.addAll(recipes);

                // Fetch actual favorites of the logged-in user to set the dynamic heart states
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseHelper.getInstance().getUserFavorites(user.getUid(), new FirebaseHelper.FavoritesListCallback() {
                        @Override
                        public void onFavoritesLoaded(List<String> favoriteIds) {
                            if (!isAdded()) return;
                            progressRecipes.setVisibility(View.GONE);
                            for (Recipe recipe : allRecipes) {
                                recipe.setFavorite(favoriteIds.contains(recipe.getId()));
                            }
                            filterRecipes();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            progressRecipes.setVisibility(View.GONE);
                            filterRecipes();
                        }
                    });
                } else {
                    progressRecipes.setVisibility(View.GONE);
                    filterRecipes();
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressRecipes.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lọc recipes theo category hoặc yêu thích đã chọn
     */
    private void filterRecipes() {
        filteredRecipes.clear();

        if (selectedCategory.equals("all")) {
            filteredRecipes.addAll(allRecipes);
        } else if (selectedCategory.equals("favorites")) {
            for (Recipe recipe : allRecipes) {
                if (recipe.isFavorite()) {
                    filteredRecipes.add(recipe);
                }
            }
        } else {
            for (Recipe recipe : allRecipes) {
                if (recipe.getCategory() != null && recipe.getCategory().equals(selectedCategory)) {
                    filteredRecipes.add(recipe);
                }
            }
        }

        recipeAdapter.notifyDataSetChanged();

        // Hiện empty state nếu không có kết quả
        layoutEmpty.setVisibility(filteredRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        rvRecipes.setVisibility(filteredRecipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Xử lý khi chọn chip filter
     */
    private void selectChip(TextView selected) {
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getResources().getColor(R.color.text_primary, null));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }

        // Map chip to category
        if (selected == chipAll) selectedCategory = "all";
        else if (selected == chipBreakfast) selectedCategory = "breakfast";
        else if (selected == chipLunch) selectedCategory = "lunch";
        else if (selected == chipDinner) selectedCategory = "dinner";
        else if (selected == chipHealthy) selectedCategory = "healthy";

        filterRecipes();
    }
}
