package gun.edu.smartcooking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InventoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút Generate Recipe Idea
        View btnGenerateRecipe = view.findViewById(R.id.btnGenerateRecipe);
        btnGenerateRecipe.setOnClickListener(v ->
                Toast.makeText(getContext(), "Đang tạo ý tưởng công thức...", Toast.LENGTH_SHORT).show());

        // FAB - Thêm thực phẩm mới
        View fabAddItem = view.findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v ->
                Toast.makeText(getContext(), "Thêm thực phẩm mới", Toast.LENGTH_SHORT).show());

        // View All buttons
        View tvViewAllVegetables = view.findViewById(R.id.tvViewAllVegetables);
        View tvViewAllProteins = view.findViewById(R.id.tvViewAllProteins);
        View tvViewAllPantry = view.findViewById(R.id.tvViewAllPantry);

        tvViewAllVegetables.setOnClickListener(v ->
                Toast.makeText(getContext(), "Xem tất cả rau củ", Toast.LENGTH_SHORT).show());
        tvViewAllProteins.setOnClickListener(v ->
                Toast.makeText(getContext(), "Xem tất cả protein", Toast.LENGTH_SHORT).show());
        tvViewAllPantry.setOnClickListener(v ->
                Toast.makeText(getContext(), "Xem tất cả pantry", Toast.LENGTH_SHORT).show());

        // Menu 3 chấm trên từng item
        View btnMoreCherryTomatoes = view.findViewById(R.id.btnMoreCherryTomatoes);
        View btnMoreBabySpinach = view.findViewById(R.id.btnMoreBabySpinach);
        View btnMoreChickenBreast = view.findViewById(R.id.btnMoreChickenBreast);
        View btnMoreAtlanticSalmon = view.findViewById(R.id.btnMoreAtlanticSalmon);
        View btnMoreOliveOil = view.findViewById(R.id.btnMoreOliveOil);
        View btnMorePennePasta = view.findViewById(R.id.btnMorePennePasta);

        View.OnClickListener moreClickListener = v -> {
            int id = v.getId();
            String itemName = "";
            if (id == R.id.btnMoreCherryTomatoes) {
                itemName = "Cherry Tomatoes";
            } else if (id == R.id.btnMoreBabySpinach) {
                itemName = "Baby Spinach";
            } else if (id == R.id.btnMoreChickenBreast) {
                itemName = "Chicken Breast";
            } else if (id == R.id.btnMoreAtlanticSalmon) {
                itemName = "Atlantic Salmon";
            } else if (id == R.id.btnMoreOliveOil) {
                itemName = "Olive Oil";
            } else if (id == R.id.btnMorePennePasta) {
                itemName = "Penne Pasta";
            }
            Toast.makeText(getContext(), "Tùy chọn: " + itemName, Toast.LENGTH_SHORT).show();
        };

        btnMoreCherryTomatoes.setOnClickListener(moreClickListener);
        btnMoreBabySpinach.setOnClickListener(moreClickListener);
        btnMoreChickenBreast.setOnClickListener(moreClickListener);
        btnMoreAtlanticSalmon.setOnClickListener(moreClickListener);
        btnMoreOliveOil.setOnClickListener(moreClickListener);
        btnMorePennePasta.setOnClickListener(moreClickListener);

        // Click vào card thực phẩm
        View cardCherryTomatoes = view.findViewById(R.id.cardCherryTomatoes);
        View cardBabySpinach = view.findViewById(R.id.cardBabySpinach);
        View cardChickenBreast = view.findViewById(R.id.cardChickenBreast);
        View cardAtlanticSalmon = view.findViewById(R.id.cardAtlanticSalmon);
        View cardOliveOil = view.findViewById(R.id.cardOliveOil);
        View cardPennePasta = view.findViewById(R.id.cardPennePasta);

        cardCherryTomatoes.setOnClickListener(v ->
                Toast.makeText(getContext(), "Cherry Tomatoes - Fresh", Toast.LENGTH_SHORT).show());
        cardBabySpinach.setOnClickListener(v ->
                Toast.makeText(getContext(), "Baby Spinach - Use Soon", Toast.LENGTH_SHORT).show());
        cardChickenBreast.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chicken Breast - Expired", Toast.LENGTH_SHORT).show());
        cardAtlanticSalmon.setOnClickListener(v ->
                Toast.makeText(getContext(), "Atlantic Salmon - Fresh", Toast.LENGTH_SHORT).show());
        cardOliveOil.setOnClickListener(v ->
                Toast.makeText(getContext(), "Olive Oil", Toast.LENGTH_SHORT).show());
        cardPennePasta.setOnClickListener(v ->
                Toast.makeText(getContext(), "Penne Pasta", Toast.LENGTH_SHORT).show());
    }
}
