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
        btnGenerateRecipe.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang kết nối Trợ lý AI...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                String prompt = "Hãy gợi ý cho tôi công thức nấu ăn ngon từ các nguyên liệu trong tủ lạnh của tôi: Cà chua bi, Cải bó xôi, Thịt ức gà, Cá hồi Atlantic, Dầu oliu và Mì ống Penne!";
                ((MainActivity) getActivity()).navigateToChefAssistant(prompt);
            }
        });

        // FAB - Thêm thực phẩm mới
        View fabAddItem = view.findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("➕ Thêm Thực Phẩm Mới")
                    .setView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null, false))
                    .setPositiveButton("Thêm vào tủ lạnh", (dialog, which) -> {
                        Toast.makeText(getContext(), "Đã thêm thực phẩm mới thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // View All buttons
        View tvViewAllVegetables = view.findViewById(R.id.tvViewAllVegetables);
        View tvViewAllProteins = view.findViewById(R.id.tvViewAllProteins);
        View tvViewAllPantry = view.findViewById(R.id.tvViewAllPantry);

        tvViewAllVegetables.setOnClickListener(v ->
                Toast.makeText(getContext(), "Danh mục Rau củ quả đã hiển thị đầy đủ", Toast.LENGTH_SHORT).show());
        tvViewAllProteins.setOnClickListener(v ->
                Toast.makeText(getContext(), "Danh mục Đạm/Thịt cá đã hiển thị đầy đủ", Toast.LENGTH_SHORT).show());
        tvViewAllPantry.setOnClickListener(v ->
                Toast.makeText(getContext(), "Danh mục Đồ khô/Gia vị đã hiển thị đầy đủ", Toast.LENGTH_SHORT).show());

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
            if (id == R.id.btnMoreCherryTomatoes) itemName = "Cà chua bi";
            else if (id == R.id.btnMoreBabySpinach) itemName = "Cải bó xôi";
            else if (id == R.id.btnMoreChickenBreast) itemName = "Thịt ức gà";
            else if (id == R.id.btnMoreAtlanticSalmon) itemName = "Cá hồi Atlantic";
            else if (id == R.id.btnMoreOliveOil) itemName = "Dầu oliu";
            else if (id == R.id.btnMorePennePasta) itemName = "Mì ống Penne";
            
            final String name = itemName;
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Tùy chọn: " + name)
                    .setItems(new String[]{"Chỉnh sửa số lượng", "Xóa khỏi tủ lạnh", "Đánh dấu hết hạn"}, (dialog, which) -> {
                        if (which == 0) {
                            Toast.makeText(getContext(), "Đang mở chỉnh sửa số lượng cho " + name, Toast.LENGTH_SHORT).show();
                        } else if (which == 1) {
                            Toast.makeText(getContext(), "Đã loại bỏ " + name + " khỏi tủ lạnh", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Đã đánh dấu " + name + " đã quá hạn", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
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

        cardCherryTomatoes.setOnClickListener(v -> showIngredientDetail("🍅 Cà chua bi (Cherry Tomatoes)", "Rau củ", "2 lbs", "Còn 5 ngày", "Tươi ngon (Fresh)", "Hãy gợi ý cho tôi 3 công thức nấu ăn sử dụng Cà chua bi làm nguyên liệu chính nhé!"));
        cardBabySpinach.setOnClickListener(v -> showIngredientDetail("🥬 Cải bó xôi (Baby Spinach)", "Rau củ", "1 bag", "Còn 2 ngày", "Nên dùng ngay (Use Soon)", "Hãy gợi ý cho tôi công thức nấu ăn ngon tốt cho sức khỏe với Cải bó xôi!"));
        cardChickenBreast.setOnClickListener(v -> showIngredientDetail("🍗 Thịt ức gà (Chicken Breast)", "Đạm", "3 lbs", "Đã quá hạn 1 ngày!", "Hết hạn (Expired)", "Hãy gợi ý công thức dinh dưỡng tốt cho việc tập gym với Thịt ức gà tươi ngon!"));
        cardAtlanticSalmon.setOnClickListener(v -> showIngredientDetail("🐟 Cá hồi Atlantic (Atlantic Salmon)", "Đạm", "2 phi lê", "Còn 4 ngày", "Tươi ngon (Fresh)", "Hãy gợi ý công thức chế biến Cá hồi Atlantic cao cấp cho bữa tối gia đình!"));
        cardOliveOil.setOnClickListener(v -> showIngredientDetail("🫒 Dầu oliu (Olive Oil)", "Gia vị / Đồ khô", "1 chai", "Còn 6 tháng", "Rất tốt (Fresh)", "Hãy gợi ý cách sử dụng Dầu oliu làm salad hoặc kết hợp nấu ăn lành mạnh!"));
        cardPennePasta.setOnClickListener(v -> showIngredientDetail("🍝 Mì ống Penne (Penne Pasta)", "Gia vị / Đồ khô", "2 hộp", "Còn 8 tháng", "Tốt (Fresh)", "Hãy gợi ý công thức làm mì Ý Penne Pasta ngon tuyệt đỉnh và dễ nấu!"));
    }

    private void showIngredientDetail(String title, String category, String quantity, String expiry, String status, String aiQuery) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage("• Phân loại: " + category + "\n" +
                        "• Số lượng hiện có: " + quantity + "\n" +
                        "• Hạn sử dụng: " + expiry + "\n" +
                        "• Trạng thái thực phẩm: " + status)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("🍳 Nấu với AI", (dialog, which) -> {
                    Toast.makeText(getContext(), "Đang gửi ý tưởng tới Trợ lý AI...", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToChefAssistant(aiQuery);
                    }
                })
                .show();
    }
}
