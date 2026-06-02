package gun.edu.smartcooking.firebase;

import android.util.Log;
import androidx.annotation.NonNull;
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

import gun.edu.smartcooking.model.Recipe;
import gun.edu.smartcooking.model.InventoryItem;

/**
 * [SavorSmart - Senior Update]
 * Quản lý tương tác với Firebase Realtime Database.
 * Việt hóa 100%, đồng bộ tuyệt đối với giao diện mới.
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;

    private final DatabaseReference rootRef;
    private final DatabaseReference recipesRef;
    private final DatabaseReference usersRef;

    private FirebaseHelper() {
        rootRef = FirebaseDatabase.getInstance("https://android-projects-cae81-default-rtdb.firebaseio.com/").getReference();
        recipesRef = rootRef.child("recipes");
        usersRef = rootRef.child("users");
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    // ==================== CÔNG THỨC ====================

    /**
     * Nạp toàn bộ công thức bằng Single Value Event để tối ưu hóa hiệu năng
     */
    public void getAllRecipes(final RecipeListCallback callback) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void getRecipeById(String recipeId, final RecipeCallback callback) {
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    recipe.setId(snapshot.getKey());
                    callback.onRecipeLoaded(recipe);
                } else callback.onError("Không tìm thấy món ăn");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== NGƯỜI DÙNG & YÊU THÍCH ====================

    public void saveUserProfile(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("joinDate", System.currentTimeMillis());
        user.put("level", "Đầu bếp tập sự");
        user.put("recipesCount", 24);
        user.put("followersCount", "1.2k");
        usersRef.child(uid).setValue(user);
    }

    public void getUserProfile(String uid, final UserProfileCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String level = snapshot.child("level").getValue(String.class);
                    Long countObj = snapshot.child("recipesCount").getValue(Long.class);
                    int recipesCount = countObj != null ? countObj.intValue() : 0;
                    String followers = snapshot.child("followersCount").getValue(String.class);
                    
                    callback.onProfileLoaded(name, email, level, recipesCount, followers);
                } else {
                    callback.onError("Không tìm thấy hồ sơ người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void isFavorite(String uid, String recipeId, final FavoriteCheckCallback callback) {
        usersRef.child(uid).child("favorites").child(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class)));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(false);
                    }
                });
    }

    public void toggleFavorite(String uid, String recipeId, final FavoriteCallback callback) {
        DatabaseReference favRef = usersRef.child(uid).child("favorites").child(recipeId);
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean newState = !snapshot.exists() || !Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                favRef.setValue(newState).addOnCompleteListener(task -> {
                    callback.onFavoriteChanged(newState);
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFavoriteChanged(false);
            }
        });
    }

    public void getUserFavorites(String uid, final FavoritesListCallback callback) {
        usersRef.child(uid).child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> favoriteIds = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) {
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

    // ==================== TỦ LẠNH (INVENTORY) DỰNG ĐỘNG ====================

    public void getInventory(String uid, final InventoryListCallback callback) {
        usersRef.child(uid).child("inventory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<InventoryItem> items = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    InventoryItem item = ds.getValue(InventoryItem.class);
                    if (item != null) {
                        item.setId(ds.getKey());
                        items.add(item);
                    }
                }
                callback.onInventoryLoaded(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void addInventoryItem(String uid, InventoryItem item, final OperationCallback callback) {
        DatabaseReference itemRef = usersRef.child(uid).child("inventory").push();
        item.setId(itemRef.getKey());
        itemRef.setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError("Không thể thêm thực phẩm mới");
        });
    }

    public void updateInventoryItem(String uid, InventoryItem item, final OperationCallback callback) {
        if (item.getId() == null) {
            callback.onError("ID thực phẩm không hợp lệ");
            return;
        }
        usersRef.child(uid).child("inventory").child(item.getId()).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError("Không thể cập nhật thực phẩm");
                });
    }

    public void deleteInventoryItem(String uid, String itemId, final OperationCallback callback) {
        usersRef.child(uid).child("inventory").child(itemId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onError("Không thể xóa thực phẩm");
                });
    }

    // ==================== DỮ LIỆU MẪU ====================

    public void seedAllDataIfEmpty(String uid) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) seedRecipes();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (uid != null) {
            usersRef.child(uid).child("inventory").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0) seedInventory(uid);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void seedRecipes() {
        List<Recipe> list = new ArrayList<>();
        list.add(new Recipe("r1", "Phở Bò Hà Nội", "Hương vị truyền thống Việt Nam ngọt thanh đậm đà.", "img_pho_bo", "lunch", 45, 450, 4.9f, Arrays.asList("Bánh phở", "Thịt bò thăn", "Xương ống bò", "Quế, hồi, thảo quả", "Hành lá, rau thơm"), Arrays.asList("Rửa xương bò thật sạch, chần qua nước sôi để khử mùi.", "Ninh xương ống bò trong 5-6 giờ cùng hành tây, hành tím và gừng nướng thơm.", "Rang quế, hồi, thảo quả chín vàng rồi cho vào túi lọc thả vào nồi nước dùng.", "Chần bánh phở qua nước ấm rồi xếp vào bát tô cùng thịt bò thái mỏng.", "Chan nước dùng nóng hổi thơm phức vào bát phở bò Hà Nội cổ truyền và thưởng thức cùng chanh ớt.")));
        list.add(new Recipe("r2", "Cơm Chiên Dương Châu", "Món cơm chiên màu sắc rực rỡ và giòn ngon hấp dẫn.", "img_fried_rice", "dinner", 20, 520, 4.7f, Arrays.asList("Cơm nguội", "Tôm tươi bóc vỏ", "Lạp xưởng thái hạt lựu", "Hạt đậu hà lan", "Trứng gà ta"), Arrays.asList("Đánh tan trứng gà ta rồi trộn đều với cơm nguội để cơm lên màu vàng óng.", "Sơ chế tôm tươi, lạp xưởng thái hạt lựu vừa ăn.", "Luộc qua đậu hà lan và cà rốt cắt nhỏ để giữ màu xanh tươi.", "Phi hành tỏi thơm rồi chiên săn tôm cùng lạp xưởng chín vàng ruộm.", "Trút cơm trộn trứng vào chảo đảo thật nhanh tay dưới lửa lớn rồi trút đậu hà lan cùng gia vị vào đảo đều bày ra đĩa thưởng thức.")));
        list.add(new Recipe("r3", "Salad Bơ Trứng Dinh Dưỡng", "Món chay nhẹ nhàng, cực tốt cho vóc dáng và tim mạch.", "img_salad", "healthy", 10, 280, 4.8f, Arrays.asList("Bơ sáp chín mềm", "Trứng gà luộc", "Xà lách tươi", "Cà chua bi", "Sốt dầu oliu chanh"), Arrays.asList("Rửa sạch rau xà lách tươi, ngâm nước muối loãng rồi vắt ráo.", "Cắt bơ sáp chín mềm thành các lát mỏng vừa ăn.", "Luộc trứng gà chín lòng đào hoặc chín kỹ tùy khẩu vị rồi bổ đôi.", "Trộn đều xà lách, bơ và trứng vào tô thủy tinh lớn.", "Rưới nước sốt dầu oliu chanh tỏi mật ong mát lành lên trên rồi đảo nhẹ và bày trí.")));
        list.add(new Recipe("r4", "Trứng Cuộn Bữa Sáng Nhanh", "Bữa sáng thơm lừng giàu năng lượng khởi đầu ngày mới.", "img_egg", "breakfast", 15, 310, 4.6f, Arrays.asList("Trứng gà 3 quả", "Hành lá thái nhỏ", "Cà rốt băm nhỏ", "Phô mai Cheddar bào sợi"), Arrays.asList("Đập trứng gà vào tô, đánh thật bông đều cùng chút gia vị.", "Thêm hành lá thái nhỏ và cà rốt băm nhuyễn mịn vào khuấy đều.", "Quét lớp dầu mỏng lên chảo chống dính nóng rồi trút 1/3 hỗn hợp trứng vào.", "Rắc phô mai Cheddar bào sợi lên trên rồi khéo léo cuộn tròn trứng lại.", "Trút tiếp hỗn hợp trứng còn lại vào cuộn tiếp cho dày dặn rồi cắt khúc đẹp mắt bày ra đĩa ăn kèm tương cà.")));

        for (Recipe r : list) recipesRef.child(r.getId()).setValue(r);
    }

    private void seedInventory(String uid) {
        List<InventoryItem> list = new ArrayList<>();
        list.add(new InventoryItem("i1", "Cà chua bi", "2 lbs", "vegetables", "fresh", "🍅"));
        list.add(new InventoryItem("i2", "Cải bó xôi", "1 bag", "vegetables", "use_soon", "🥬"));
        list.add(new InventoryItem("i3", "Thịt ức gà", "3 lbs", "proteins", "expired", "🍗"));
        list.add(new InventoryItem("i4", "Cá hồi Atlantic", "2 phi lê", "proteins", "fresh", "🐟"));
        list.add(new InventoryItem("i5", "Dầu oliu", "1 chai", "pantry", "fresh", "🫒"));
        list.add(new InventoryItem("i6", "Mì ống Penne", "2 hộp", "pantry", "fresh", "🍝"));

        for (InventoryItem item : list) {
            usersRef.child(uid).child("inventory").child(item.getId()).setValue(item);
        }
    }

    // ==================== INTERFACES ====================
    public interface RecipeListCallback { void onRecipesLoaded(List<Recipe> recipes); void onError(String error); }
    public interface RecipeCallback { void onRecipeLoaded(Recipe recipe); void onError(String error); }
    public interface UserProfileCallback { void onProfileLoaded(String name, String email, String level, int recipesCount, String followers); void onError(String error); }
    public interface FavoriteCallback { void onFavoriteChanged(boolean isFavorite); }
    public interface FavoriteCheckCallback { void onResult(boolean result); }
    public interface FavoritesListCallback { void onFavoritesLoaded(List<String> favoriteIds); void onError(String error); }
    public interface InventoryListCallback { void onInventoryLoaded(List<InventoryItem> items); void onError(String error); }
    public interface OperationCallback { void onSuccess(); void onError(String error); }
}
