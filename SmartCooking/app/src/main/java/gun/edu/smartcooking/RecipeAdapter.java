package gun.edu.smartcooking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter hiển thị danh sách công thức trong RecyclerView
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final List<Recipe> recipes;
    private final Context context;
    private OnRecipeClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Recipe recipe, int position);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        // Đổ dữ liệu văn bản
        holder.tvRecipeName.setText(recipe.getName());
        holder.tvRecipeDesc.setText(recipe.getDescription());
        holder.tvPrepTime.setText(recipe.getPrepTimeMinutes() + " phút");
        holder.tvCalories.setText(recipe.getCalories() + " kcal");
        holder.tvRating.setText(String.valueOf(recipe.getRating()));

        // Hiển thị Category
        String category = recipe.getCategory();
        if (category != null) {
            switch (category) {
                case "breakfast": holder.tvCategory.setText("Bữa sáng"); break;
                case "lunch": holder.tvCategory.setText("Bữa trưa"); break;
                case "dinner": holder.tvCategory.setText("Bữa tối"); break;
                case "healthy": holder.tvCategory.setText("Dinh dưỡng"); break;
                default: holder.tvCategory.setText(category);
            }
        }

        // Cập nhật phương thức hiển thị ảnh: Hỗ trợ cả Local và URL Cloud
        recipe.displayImage(context, holder.ivRecipeImage);

        // Trạng thái yêu thích
        holder.btnFavorite.setAlpha(recipe.isFavorite() ? 1.0f : 0.4f);

        // Click listeners
        holder.cardRecipe.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) favoriteListener.onFavoriteClick(recipe, position);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardRecipe;
        ImageView ivRecipeImage, btnFavorite;
        TextView tvRecipeName, tvRecipeDesc, tvPrepTime, tvCalories, tvRating, tvCategory;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRecipe = itemView.findViewById(R.id.cardRecipe);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeDesc = itemView.findViewById(R.id.tvRecipeDesc);
            tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
