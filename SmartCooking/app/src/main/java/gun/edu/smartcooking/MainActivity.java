package gun.edu.smartcooking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);

        // Ẩn vệt bo tròn màu tím (Active Indicator) của Material 3 đằng sau icon đang chọn để tăng thẩm mỹ
        bottomNav.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));

        // Seed dữ liệu công thức lên Firebase (chỉ chạy nếu chưa có)
        FirebaseHelper.getInstance().seedRecipesIfEmpty();

        // Thiết lập sự kiện khi chọn các tab ở thanh điều hướng bên dưới
        setupNavigationListener();

        // Mặc định chọn tab Home
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupNavigationListener() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_recipes) {
                loadFragment(new RecipesFragment());
                return true;
            } else if (id == R.id.nav_planner) {
                loadFragment(new ChefAssistantFragment());
                return true;
            } else if (id == R.id.nav_inventory) {
                loadFragment(new InventoryFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    /**
     * Điều hướng sang trang công thức với tùy chọn hiển thị yêu thích
     */
    public void navigateToRecipes(boolean showFavoritesOnly) {
        RecipesFragment fragment = new RecipesFragment();
        if (showFavoritesOnly) {
            Bundle args = new Bundle();
            args.putBoolean("show_favorites_only", true);
            fragment.setArguments(args);
        }
        loadFragment(fragment);

        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(R.id.nav_recipes);
        setupNavigationListener();
    }

    /**
     * Điều hướng sang Trợ lý AI và tự động gửi câu lệnh gợi ý
     */
    public void navigateToChefAssistant(String query) {
        ChefAssistantFragment fragment = new ChefAssistantFragment();
        if (query != null) {
            Bundle args = new Bundle();
            args.putString("auto_query", query);
            fragment.setArguments(args);
        }
        loadFragment(fragment);

        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(R.id.nav_planner);
        setupNavigationListener();
    }

    /**
     * Chuyển đổi Fragment hiển thị trong container
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}