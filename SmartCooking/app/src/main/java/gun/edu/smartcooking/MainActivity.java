package gun.edu.smartcooking;

import android.os.Bundle;
import android.widget.Toast;

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

        // Thiết lập sự kiện khi chọn các tab ở thanh điều hướng bên dưới
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
                Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Mặc định chọn tab Home
        bottomNav.setSelectedItemId(R.id.nav_home);
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