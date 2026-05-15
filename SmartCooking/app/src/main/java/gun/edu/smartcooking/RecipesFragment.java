package gun.edu.smartcooking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RecipesFragment extends Fragment {

    private TextView chipAll, chipBreakfast, chipLunch, chipDinner, chipHealthy;
    private TextView[] chips;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các chip filter
        chipAll = view.findViewById(R.id.chipAll);
        chipBreakfast = view.findViewById(R.id.chipBreakfast);
        chipLunch = view.findViewById(R.id.chipLunch);
        chipDinner = view.findViewById(R.id.chipDinner);
        chipHealthy = view.findViewById(R.id.chipHealthy);

        chips = new TextView[] { chipAll, chipBreakfast, chipLunch, chipDinner, chipHealthy };

        // Thiết lập sự kiện click cho các chip
        for (TextView chip : chips) {
            chip.setOnClickListener(v -> selectChip((TextView) v));
        }

        // Thiết lập sự kiện click cho các card
        View cardFeatured = view.findViewById(R.id.cardFeatured);
        View cardRecipe2 = view.findViewById(R.id.cardRecipe2);
        View cardRecipe3 = view.findViewById(R.id.cardRecipe3);

        cardFeatured.setOnClickListener(
                v -> Toast.makeText(getContext(), "Vibrant Quinoa Power Bowl", Toast.LENGTH_SHORT).show());
        cardRecipe2.setOnClickListener(
                v -> Toast.makeText(getContext(), "Spring Greens & Egg Salad", Toast.LENGTH_SHORT).show());
        cardRecipe3.setOnClickListener(
                v -> Toast.makeText(getContext(), "Rustic Margherita Pizza", Toast.LENGTH_SHORT).show());

        // Thiết lập sự kiện yêu thích
        View btnFav2 = view.findViewById(R.id.btnFavorite2);
        View btnFav3 = view.findViewById(R.id.btnFavorite3);

        btnFav2.setOnClickListener(
                v -> Toast.makeText(getContext(), "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show());
        btnFav3.setOnClickListener(
                v -> Toast.makeText(getContext(), "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Xử lý khi chọn chip filter
     */
    private void selectChip(TextView selected) {
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getResources().getColor(R.color.text_primary, null));
                selectedCategory = chip.getText().toString();
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }

        // Hiển thị thông báo lọc
        Toast.makeText(getContext(), "Lọc: " + selectedCategory, Toast.LENGTH_SHORT).show();
    }
}
