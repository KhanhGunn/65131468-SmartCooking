package gun.edu.smartcooking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.adapter.RecipeAdapter;
import gun.edu.smartcooking.databinding.FragmentRecipesBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;
import gun.edu.smartcooking.model.Recipe;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private TextView[] chips;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> filteredRecipes = new ArrayList<>();
    private String selectedCategory = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chips = new TextView[] { 
                binding.chipAll, 
                binding.chipBreakfast, 
                binding.chipLunch, 
                binding.chipDinner, 
                binding.chipHealthy 
        };

        recipeAdapter = new RecipeAdapter(requireContext(), filteredRecipes);
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecipes.setAdapter(recipeAdapter);

        recipeAdapter.setOnRecipeClickListener(recipe -> {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });

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

        binding.swipeRefreshRecipes.setColorSchemeColors(getResources().getColor(R.color.primary, null));
        binding.swipeRefreshRecipes.setOnRefreshListener(this::loadRecipes);

        loadRecipes();
    }

    private void loadRecipes() {
        if (!binding.swipeRefreshRecipes.isRefreshing()) {
            binding.progressRecipes.setVisibility(View.VISIBLE);
        }
        binding.layoutEmpty.setVisibility(View.GONE);

        FirebaseHelper.getInstance().getAllRecipes(new FirebaseHelper.RecipeListCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                if (!isAdded()) return;
                binding.swipeRefreshRecipes.setRefreshing(false);

                allRecipes.clear();
                allRecipes.addAll(recipes);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseHelper.getInstance().getUserFavorites(user.getUid(), new FirebaseHelper.FavoritesListCallback() {
                        @Override
                        public void onFavoritesLoaded(List<String> favoriteIds) {
                            if (!isAdded()) return;
                            binding.progressRecipes.setVisibility(View.GONE);
                            for (Recipe recipe : allRecipes) {
                                recipe.setFavorite(favoriteIds.contains(recipe.getId()));
                            }
                            filterRecipes();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            binding.progressRecipes.setVisibility(View.GONE);
                            filterRecipes();
                        }
                    });
                } else {
                    binding.progressRecipes.setVisibility(View.GONE);
                    filterRecipes();
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                binding.swipeRefreshRecipes.setRefreshing(false);
                binding.progressRecipes.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

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

        binding.layoutEmpty.setVisibility(filteredRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvRecipes.setVisibility(filteredRecipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void selectChip(TextView selected) {
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }

        if (selected == binding.chipAll) selectedCategory = "all";
        else if (selected == binding.chipBreakfast) selectedCategory = "breakfast";
        else if (selected == binding.chipLunch) selectedCategory = "lunch";
        else if (selected == binding.chipDinner) selectedCategory = "dinner";
        else if (selected == binding.chipHealthy) selectedCategory = "healthy";

        filterRecipes();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
