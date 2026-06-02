package gun.edu.smartcooking.model;

import java.util.ArrayList;
import java.util.List;
import gun.edu.smartcooking.R;

/**
 * Model class cho công thức nấu ăn
 */
public class Recipe {

    private String id;
    private String name;
    private String description;
    private String imageRes;       // Có thể là tên drawable (vd: "img_pho") hoặc Link URL (http...)
    private String category;
    private int prepTimeMinutes;
    private int calories;
    private float rating;
    private List<String> ingredients;
    private List<String> steps;
    private boolean favorite;

    public Recipe() {
        ingredients = new ArrayList<>();
        steps = new ArrayList<>();
    }

    public Recipe(String id, String name, String description, String imageRes,
                  String category, int prepTimeMinutes, int calories, float rating,
                  List<String> ingredients, List<String> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageRes = imageRes;
        this.category = category;
        this.prepTimeMinutes = prepTimeMinutes;
        this.calories = calories;
        this.rating = rating;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.steps = steps != null ? steps : new ArrayList<>();
        this.favorite = false;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageRes() { return imageRes; }
    public void setImageRes(String imageRes) { this.imageRes = imageRes; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    /**
     * Phương thức thông minh để hiển thị ảnh từ Drawable hoặc URL
     */
    public void displayImage(android.content.Context context, android.widget.ImageView imageView) {
        if (imageRes == null || imageRes.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_utensils);
            return;
        }

        if (imageRes.startsWith("http")) {
            // Nếu là link web (Cloud), dùng Glide
            com.bumptech.glide.Glide.with(context)
                    .load(imageRes)
                    .placeholder(R.drawable.ic_utensils)
                    .error(R.drawable.ic_utensils)
                    .centerCrop()
                    .into(imageView);
        } else {
            // Nếu là tên file trong máy
            int resId = context.getResources().getIdentifier(imageRes, "drawable", context.getPackageName());
            imageView.setImageResource(resId != 0 ? resId : R.drawable.ic_utensils);
        }
    }
}
