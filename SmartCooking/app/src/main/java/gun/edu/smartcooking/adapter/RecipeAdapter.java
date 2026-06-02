package gun.edu.smartcooking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.model.Recipe;

/**
 * Adapter cho RecyclerView hiển thị danh sách công thức nấu ăn
 * Đã được tối ưu hóa click listeners để tăng hiệu năng cuộn tối đa.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private final List<Recipe> recipes;
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
        this.favoriteListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_card, parent, false);
        RecipeViewHolder holder = new RecipeViewHolder(view);

        // Thiết lập Click Listener tại đây thay vì trong onBindViewHolder để tránh Garbage Collector Overhead
        holder.cardRecipe.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onRecipeClick(recipes.get(pos));
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && favoriteListener != null) {
                favoriteListener.onFavoriteClick(recipes.get(pos), pos);
            }
        });

        return holder;
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

        // Hiển thị Category thuần Việt
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

        // Cập nhật hiển thị ảnh (Hỗ trợ Local & Cloud qua Glide cache)
        recipe.displayImage(context, holder.ivRecipeImage);

        // Trạng thái yêu thích
        holder.btnFavorite.setAlpha(recipe.isFavorite() ? 1.0f : 0.4f);
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
