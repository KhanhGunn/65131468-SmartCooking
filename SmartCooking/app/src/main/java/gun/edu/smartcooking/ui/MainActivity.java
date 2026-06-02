package gun.edu.smartcooking.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.databinding.ActivityMainBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setItemActiveIndicatorColor(null);

        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseHelper.getInstance().seedAllDataIfEmpty(uid);

        setupNavigation();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) loadFragment(new HomeFragment());
            else if (id == R.id.nav_recipes) loadFragment(new RecipesFragment());
            else if (id == R.id.nav_planner) loadFragment(new ChefAssistantFragment());
            else if (id == R.id.nav_inventory) loadFragment(new InventoryFragment());
            else if (id == R.id.nav_profile) loadFragment(new ProfileFragment());
            return true;
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public void navigateToChefAssistant(String query) {
        ChefAssistantFragment fragment = new ChefAssistantFragment();
        if (query != null) {
            Bundle args = new Bundle();
            args.putString("auto_query", query);
            fragment.setArguments(args);
        }
        loadFragment(fragment);
        binding.bottomNavigation.setSelectedItemId(R.id.nav_planner);
    }

    public void navigateToRecipes(boolean showFav) {
        RecipesFragment fragment = new RecipesFragment();
        if (showFav) {
            Bundle args = new Bundle();
            args.putBoolean("show_favorites_only", true);
            fragment.setArguments(args);
        }
        loadFragment(fragment);
        binding.bottomNavigation.setSelectedItemId(R.id.nav_recipes);
    }

    public void navigateToInventory() {
        loadFragment(new InventoryFragment());
        binding.bottomNavigation.setSelectedItemId(R.id.nav_inventory);
    }
}
