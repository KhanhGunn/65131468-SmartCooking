package gun.edu.smartcooking;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class quản lý tất cả tương tác với Firebase Realtime Database
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;

    private final DatabaseReference rootRef;
    private final DatabaseReference recipesRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference inventoryRef;

    private FirebaseHelper() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        recipesRef = rootRef.child("recipes");
        usersRef = rootRef.child("users");
        inventoryRef = rootRef.child("inventory");
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // ==================== RECIPES ====================

    /**
     * Lấy tất cả công thức
     */
    public void getAllRecipes(final RecipeListCallback callback) {
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
                        recipe.setId(ds.getKey());
                        recipes.add(recipe);
                    }
                }
                callback.onRecipesLoaded(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading recipes: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Lấy công thức theo category
     */
    public void getRecipesByCategory(String category, final RecipeListCallback callback) {
        recipesRef.orderByChild("category").equalTo(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Recipe> recipes = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Recipe recipe = ds.getValue(Recipe.class);
                            if (recipe != null) {
                                recipe.setId(ds.getKey());
                                recipes.add(recipe);
                            }
                        }
                        callback.onRecipesLoaded(recipes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Lấy 1 công thức theo ID
     */
    public void getRecipeById(String recipeId, final RecipeCallback callback) {
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    recipe.setId(snapshot.getKey());
                    callback.onRecipeLoaded(recipe);
                } else {
                    callback.onError("Không tìm thấy công thức");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== USER PROFILE ====================

    /**
     * Lưu thông tin user mới
     */
    public void saveUserProfile(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("joinDate", System.currentTimeMillis());
        user.put("recipesCount", 0);

        usersRef.child(uid).setValue(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user profile", e));
    }

    /**
     * Lấy thông tin user
     */
    public void getUserProfile(String uid, final UserProfileCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    callback.onProfileLoaded(name, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== FAVORITES ====================

    /**
     * Toggle yêu thích
     */
    public void toggleFavorite(String uid, String recipeId, final FavoriteCallback callback) {
        DatabaseReference favRef = usersRef.child(uid).child("favorites").child(recipeId);
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favRef.removeValue();
                    callback.onFavoriteChanged(false);
                } else {
                    favRef.setValue(true);
                    callback.onFavoriteChanged(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error toggling favorite", error.toException());
            }
        });
    }

    /**
     * Kiểm tra xem recipe có được yêu thích không
     */
    public void isFavorite(String uid, String recipeId, final FavoriteCheckCallback callback) {
        usersRef.child(uid).child("favorites").child(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(false);
                    }
                });
    }

    /**
     * Lấy toàn bộ danh sách ID các món ăn yêu thích của user
     */
    public void getUserFavorites(String uid, final FavoritesListCallback callback) {
        usersRef.child(uid).child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> favoriteIds = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Boolean isFav = ds.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isFav)) {
                        favoriteIds.add(ds.getKey());
                    }
                }
                callback.onFavoritesLoaded(favoriteIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== SEED DATA ====================

    /**
     * Đẩy dữ liệu công thức ban đầu lên Firebase
     * Chỉ chạy 1 lần khi chưa có data
     */
    public void seedRecipesIfEmpty() {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    seedRecipes();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking recipes", error.toException());
            }
        });
    }

    private void seedRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        // 1. Phở Bò
        recipes.add(new Recipe("recipe_001", "Phở Bò Hà Nội",
                "Món phở truyền thống với nước dùng ninh xương bò thơm lừng, bánh phở mềm và thịt bò tái chín. Đậm đà hương vị Việt Nam.",
                "img_pho_bo", "lunch", 45, 380, 4.9f,
                Arrays.asList("500g xương bò", "300g thịt bò tái", "400g bánh phở tươi",
                        "1 củ hành tây", "Gừng 50g", "Hoa hồi 3 cánh", "Quế 1 thanh",
                        "Hành lá, ngò rí", "Giá đỗ, rau quế, chanh"),
                Arrays.asList("Ninh xương bò với nước trong 3-4 tiếng, vớt bọt thường xuyên",
                        "Nướng hành tây và gừng trên bếp cho thơm, cho vào nồi nước dùng",
                        "Cho hoa hồi, quế vào túi gia vị, thả vào nồi ninh thêm 30 phút",
                        "Nêm nước mắm, muối, đường cho vừa ăn",
                        "Trụng bánh phở qua nước sôi, xếp vào tô",
                        "Thái thịt bò tái mỏng, xếp lên bánh phở",
                        "Chan nước dùng nóng lên, rắc hành lá, ngò rí",
                        "Ăn kèm giá đỗ, rau quế, chanh và tương ớt")));

        // 2. Cơm Chiên Dương Châu
        recipes.add(new Recipe("recipe_002", "Cơm Chiên Dương Châu",
                "Cơm chiên thơm ngon với tôm, lạp xưởng, trứng và rau củ tươi. Món ăn nhanh gọn cho bữa trưa.",
                "img_fried_rice", "lunch", 20, 450, 4.7f,
                Arrays.asList("3 chén cơm nguội", "200g tôm bóc vỏ", "2 quả trứng",
                        "1 cây lạp xưởng", "100g đậu Hà Lan", "1 củ cà rốt",
                        "Hành lá, tỏi băm", "Nước mắm, tiêu, dầu ăn"),
                Arrays.asList("Đánh tan trứng, chiên thành trứng mỏng rồi cắt nhỏ",
                        "Xào tôm với tỏi cho chín, múc ra",
                        "Chiên lạp xưởng cắt hạt lựu cho giòn",
                        "Phi tỏi thơm, cho cơm nguội vào xào lửa lớn",
                        "Cho cà rốt, đậu Hà Lan vào xào cùng",
                        "Nêm nước mắm, tiêu cho vừa ăn",
                        "Cho tôm, trứng, lạp xưởng vào trộn đều",
                        "Rắc hành lá, dọn ra đĩa")));

        // 3. Gỏi Cuốn Tôm Thịt
        recipes.add(new Recipe("recipe_003", "Gỏi Cuốn Tôm Thịt",
                "Gỏi cuốn tươi mát với tôm, thịt heo, bún và rau thơm. Chấm với tương đậu phộng béo ngậy.",
                "img_spring_rolls", "healthy", 25, 180, 4.8f,
                Arrays.asList("200g tôm sú", "200g thịt ba chỉ luộc", "200g bún tươi",
                        "Bánh tráng 10 cái", "Rau xà lách, rau thơm", "Giá đỗ",
                        "Đậu phộng rang", "Tương hoisin, tỏi, ớt"),
                Arrays.asList("Luộc tôm, bóc vỏ, bổ đôi. Luộc thịt ba chỉ, thái lát mỏng",
                        "Chuẩn bị rau xà lách, rau thơm rửa sạch",
                        "Trụng bún qua nước sôi, để ráo",
                        "Nhúng bánh tráng vào nước ấm cho mềm",
                        "Đặt rau xà lách, bún, giá đỗ, rau thơm lên bánh tráng",
                        "Cuốn một vòng, xếp tôm và thịt vào, cuốn tiếp cho chặt",
                        "Pha nước chấm: tương hoisin + đậu phộng giã + tỏi ớt",
                        "Dọn ra đĩa, ăn kèm nước chấm đậu phộng")));

        // 4. Quinoa Power Bowl
        recipes.add(new Recipe("recipe_004", "Quinoa Power Bowl",
                "Bowl dinh dưỡng với hạt quinoa, bơ, rau tươi và sốt tahini chanh. Hoàn hảo cho bữa ăn healthy.",
                "img_quinoa_bowl", "healthy", 15, 320, 4.8f,
                Arrays.asList("200g hạt quinoa", "1 quả bơ chín", "100g cà chua bi",
                        "100g dưa leo", "50g đậu gà luộc", "Cải kale hoặc rau mầm",
                        "2 muỗng tahini", "Nước cốt chanh, muối, tiêu"),
                Arrays.asList("Vo sạch quinoa, nấu với nước tỉ lệ 1:2 trong 15 phút",
                        "Cắt bơ, cà chua bi, dưa leo thành miếng vừa ăn",
                        "Pha sốt tahini: tahini + nước cốt chanh + chút nước + muối",
                        "Xếp quinoa vào bát, sắp xếp rau củ và đậu gà lên trên",
                        "Rưới sốt tahini lên, rắc thêm hạt chia hoặc mè",
                        "Trang trí với rau mầm và ăn ngay")));

        // 5. Spring Greens & Egg Salad
        recipes.add(new Recipe("recipe_005", "Salad Rau Mầm & Trứng",
                "Salad nhẹ nhàng với rau mầm, trứng luộc lòng đào, cà chua bi và sốt vinaigrette thanh mát.",
                "img_egg_salad", "breakfast", 10, 250, 4.6f,
                Arrays.asList("3 quả trứng gà", "150g rau mầm mixed", "100g cà chua bi",
                        "50g phô mai feta", "Hạt hướng dương", "Dầu olive, giấm balsamic",
                        "Mật ong, muối, tiêu"),
                Arrays.asList("Luộc trứng lòng đào: cho trứng vào nước sôi, luộc 7 phút",
                        "Ngâm trứng vào nước đá, bóc vỏ, cắt đôi",
                        "Rửa sạch rau mầm, cà chua bi cắt đôi",
                        "Pha vinaigrette: dầu olive + giấm balsamic + mật ong + muối tiêu",
                        "Xếp rau vào đĩa, đặt trứng và cà chua lên",
                        "Rưới sốt, rắc phô mai feta và hạt hướng dương")));

        // 6. Margherita Pizza
        recipes.add(new Recipe("recipe_006", "Pizza Margherita",
                "Pizza truyền thống Ý với đế giòn xốp, sốt cà chua tươi, phô mai mozzarella và lá basil thơm lừng.",
                "img_pizza", "dinner", 40, 450, 4.9f,
                Arrays.asList("300g bột mì", "7g men khô", "200ml nước ấm",
                        "200g sốt cà chua", "250g phô mai mozzarella", "Lá basil tươi",
                        "Dầu olive, muối, đường"),
                Arrays.asList("Trộn bột mì, men, muối, đường. Thêm nước ấm và dầu olive, nhào 10 phút",
                        "Ủ bột trong 1 tiếng cho nở gấp đôi",
                        "Cán bột thành hình tròn, đặt lên khay nướng",
                        "Phết sốt cà chua đều lên mặt đế",
                        "Rải phô mai mozzarella xé nhỏ lên trên",
                        "Nướng ở 220°C trong 12-15 phút đến khi viền vàng",
                        "Lấy ra, xếp lá basil tươi lên, rưới chút dầu olive",
                        "Cắt miếng và thưởng thức ngay khi còn nóng")));

        // 7. Cá Hồi Nướng
        recipes.add(new Recipe("recipe_007", "Cá Hồi Nướng Chanh Thảo Mộc",
                "Phi lê cá hồi nướng với chanh và thảo mộc, ăn kèm măng tây. Giàu omega-3 và protein.",
                "img_grilled_salmon", "dinner", 25, 380, 4.8f,
                Arrays.asList("2 phi lê cá hồi (300g)", "1 quả chanh", "Tỏi 3 tép",
                        "Thì là, hương thảo tươi", "200g măng tây", "Dầu olive",
                        "Muối, tiêu đen", "Bơ 20g"),
                Arrays.asList("Ướp cá hồi với nước cốt chanh, tỏi băm, thì là, muối tiêu 15 phút",
                        "Làm nóng lò nướng ở 200°C",
                        "Đặt cá hồi lên khay nướng lót giấy bạc, rưới dầu olive",
                        "Nướng 12-15 phút tùy độ dày của cá",
                        "Xào măng tây với bơ và tỏi trên chảo nóng 3-4 phút",
                        "Bày cá hồi ra đĩa cùng măng tây",
                        "Garnish với lát chanh và thì là tươi")));

        // 8. Spicy Tomato Pasta
        recipes.add(new Recipe("recipe_008", "Pasta Cà Chua Cay",
                "Mì ống penne với sốt cà chua cay nồng kiểu Arrabbiata, rắc phô mai Parmesan và basil tươi.",
                "img_tomato_pasta", "dinner", 25, 420, 4.5f,
                Arrays.asList("300g mì penne", "400g cà chua đóng hộp xay nhuyễn",
                        "4 tép tỏi", "Ớt khô 2 trái", "Dầu olive 3 muỗng",
                        "Phô mai Parmesan", "Lá basil tươi", "Muối, tiêu"),
                Arrays.asList("Luộc mì penne theo hướng dẫn bao bì, để lại 1 cup nước luộc",
                        "Phi tỏi thái lát với dầu olive trên lửa vừa cho thơm",
                        "Cho ớt khô vào xào cùng 1 phút",
                        "Đổ cà chua xay vào, nêm muối tiêu, đun sôi rồi hạ lửa nhỏ",
                        "Nấu sốt 15 phút cho đặc lại",
                        "Cho mì đã luộc vào chảo sốt, trộn đều. Thêm nước luộc mì nếu khô",
                        "Dọn ra đĩa, rắc phô mai Parmesan bào và basil tươi")));

        // Push lên Firebase
        for (Recipe recipe : recipes) {
            recipesRef.child(recipe.getId()).setValue(recipe)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Seeded: " + recipe.getName()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error seeding: " + recipe.getName(), e));
        }
    }

    // ==================== CALLBACKS ====================

    public interface RecipeListCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onError(String error);
    }

    public interface RecipeCallback {
        void onRecipeLoaded(Recipe recipe);
        void onError(String error);
    }

    public interface UserProfileCallback {
        void onProfileLoaded(String name, String email);
        void onError(String error);
    }

    public interface FavoriteCallback {
        void onFavoriteChanged(boolean isFavorite);
    }

    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorite);
    }

    public interface FavoritesListCallback {
        void onFavoritesLoaded(List<String> favoriteIds);
        void onError(String error);
    }
}
