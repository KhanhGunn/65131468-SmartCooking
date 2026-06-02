package gun.edu.smartcooking.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.adapter.InventoryAdapter;
import gun.edu.smartcooking.databinding.FragmentInventoryBinding;
import gun.edu.smartcooking.firebase.FirebaseHelper;
import gun.edu.smartcooking.model.InventoryItem;

/**
 * Fragment cho trang Tủ lạnh thực phẩm - Nâng cấp động hoàn toàn sử dụng RecyclerView.
 */
public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryItem> itemList = new ArrayList<>();
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uid = FirebaseAuth.getInstance().getUid();

        // Cấu hình RecyclerView dạng Grid 2 cột cực đẹp
        binding.rvInventory.setLayoutManager(new GridLayoutManager(getContext(), 2));
        inventoryAdapter = new InventoryAdapter(requireContext(), itemList);
        binding.rvInventory.setAdapter(inventoryAdapter);

        // Đăng ký sự kiện kéo làm mới (Pull-to-Refresh)
        binding.swipeRefreshInventory.setColorSchemeColors(getResources().getColor(R.color.primary, null));
        binding.swipeRefreshInventory.setOnRefreshListener(this::loadInventoryData);

        // Xử lý sự kiện click item
        inventoryAdapter.setOnItemClickListener(item -> showIngredientDetail(item));

        // Xử lý sự kiện click mở rộng More options
        inventoryAdapter.setOnMoreClickListener((item, anchorView) -> showMoreOptions(item));

        // Sự kiện click nút AI gợi ý nấu ăn từ tủ lạnh
        binding.btnGenerateRecipe.setOnClickListener(v -> {
            if (itemList.isEmpty()) {
                Toast.makeText(getContext(), "Tủ lạnh của bạn đang trống! Hãy thêm thực phẩm trước.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Đang kết nối Trợ lý AI...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                StringBuilder builder = new StringBuilder();
                builder.append("Tôi đang có các nguyên liệu sau trong tủ lạnh:\n");
                for (int i = 0; i < Math.min(itemList.size(), 6); i++) {
                    builder.append("- ").append(itemList.get(i).getName()).append(" (").append(itemList.get(i).getQuantity()).append(")\n");
                }
                builder.append("Hãy gợi ý cho tôi 2 công thức nấu ăn ngon nhất từ những nguyên liệu này nhé!");
                ((MainActivity) getActivity()).navigateToChefAssistant(builder.toString());
            }
        });

        // Click thêm thực phẩm mới
        binding.fabAddItem.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null, false);
            EditText etIngredientName = dialogView.findViewById(R.id.etIngredientName);
            EditText etIngredientQuantity = dialogView.findViewById(R.id.etIngredientQuantity);
            EditText etIngredientExpiry = dialogView.findViewById(R.id.etIngredientExpiry);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("➕ Thêm Thực Phẩm Mới")
                    .setView(dialogView)
                    .setPositiveButton("Thêm vào tủ lạnh", (dialog, which) -> {
                        String name = etIngredientName.getText().toString().trim();
                        String qty = etIngredientQuantity.getText().toString().trim();
                        String expiryText = etIngredientExpiry.getText().toString().trim();

                        if (name.isEmpty() || qty.isEmpty()) {
                            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Thuật toán thông minh tự động nhận diện phân loại & Emoji dựa trên từ khóa tiếng Việt
                        String category = "vegetables";
                        String emoji = "🥦";
                        String status = "fresh";

                        String lowerName = name.toLowerCase();
                        if (lowerName.contains("thịt") || lowerName.contains("cá") || lowerName.contains("gà") || 
                            lowerName.contains("bò") || lowerName.contains("heo") || lowerName.contains("salmon") || 
                            lowerName.contains("tôm") || lowerName.contains("trứng") || lowerName.contains("cua") || 
                            lowerName.contains("giò") || lowerName.contains("chả")) {
                            category = "proteins";
                            emoji = "🥩";
                        } else if (lowerName.contains("dầu") || lowerName.contains("mì") || lowerName.contains("bột") || 
                                   lowerName.contains("muối") || lowerName.contains("pasta") || lowerName.contains("tương") || 
                                   lowerName.contains("mắm") || lowerName.contains("đường") || lowerName.contains("tiêu") || 
                                   lowerName.contains("bơ") || lowerName.contains("phô mai") || lowerName.contains("sữa")) {
                            category = "pantry";
                            emoji = "🧂";
                        } else if (lowerName.contains("táo") || lowerName.contains("cam") || lowerName.contains("chuối") || 
                                   lowerName.contains("dâu") || lowerName.contains("quả") || lowerName.contains("chanh") || 
                                   lowerName.contains("xoài") || lowerName.contains("dưa")) {
                            category = "vegetables";
                            emoji = "🍎";
                        }

                        // Nhận diện trạng thái hạn dùng sơ bộ
                        if (expiryText.toLowerCase().contains("hết hạn") || expiryText.toLowerCase().contains("0") || expiryText.toLowerCase().contains("quá hạn")) {
                            status = "expired";
                        } else if (expiryText.toLowerCase().contains("1") || expiryText.toLowerCase().contains("2") || expiryText.toLowerCase().contains("gấp") || expiryText.toLowerCase().contains("sắp")) {
                            status = "use_soon";
                        }

                        InventoryItem newItem = new InventoryItem(null, name, qty, category, status, emoji);
                        FirebaseHelper.getInstance().addInventoryItem(uid, newItem, new FirebaseHelper.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Đã thêm thực phẩm '" + name + "' vào tủ lạnh! 🎉", Toast.LENGTH_SHORT).show();
                                loadInventoryData();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Thêm thất bại: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        loadInventoryData();
    }

    private void loadInventoryData() {
        if (uid == null) return;

        if (!binding.swipeRefreshInventory.isRefreshing()) {
            binding.swipeRefreshInventory.setRefreshing(true);
        }

        FirebaseHelper.getInstance().getInventory(uid, new FirebaseHelper.InventoryListCallback() {
            @Override
            public void onInventoryLoaded(List<InventoryItem> items) {
                if (!isAdded()) return;
                binding.swipeRefreshInventory.setRefreshing(false);

                itemList.clear();
                if (items != null) {
                    itemList.addAll(items);
                }

                inventoryAdapter.notifyDataSetChanged();

                // Quản lý trạng thái Trống (Empty State)
                if (itemList.isEmpty()) {
                    binding.layoutEmptyInventory.setVisibility(View.VISIBLE);
                    binding.rvInventory.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyInventory.setVisibility(View.GONE);
                    binding.rvInventory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                binding.swipeRefreshInventory.setRefreshing(false);
                Toast.makeText(getContext(), "Không thể tải danh sách thực phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIngredientDetail(InventoryItem item) {
        String statusLabel = translateStatus(item.getStatus());
        String categoryLabel = translateCategory(item.getCategory());
        String aiQuery = "Hãy gợi ý cho tôi công thức nấu ăn ngon với nguyên liệu chính là " + item.getName() + " nhé!";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getIconEmoji() + " " + item.getName())
                .setMessage("• Số lượng: " + item.getQuantity() + "\n" +
                        "• Phân loại: " + categoryLabel + "\n" +
                        "• Trạng thái hạn dùng: " + statusLabel)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("🍳 Nấu với AI", (dialog, which) -> {
                    Toast.makeText(getContext(), "Đang gửi ý tưởng tới Trợ lý AI...", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToChefAssistant(aiQuery);
                    }
                })
                .show();
    }

    private void showMoreOptions(InventoryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tùy chọn: " + item.getName())
                .setItems(new String[]{"Chỉnh sửa số lượng", "Đánh dấu sử dụng hết", "Đánh dấu hết hạn"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditQuantityDialog(item);
                    } else if (which == 1) {
                        deleteItem(item);
                    } else {
                        markExpired(item);
                    }
                })
                .show();
    }

    private void showEditQuantityDialog(InventoryItem item) {
        EditText etQty = new EditText(requireContext());
        etQty.setText(item.getQuantity());
        etQty.setPadding(60, 40, 60, 40);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chỉnh sửa số lượng")
                .setView(etQty)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newQty = etQty.getText().toString().trim();
                    if (!newQty.isEmpty()) {
                        item.setQuantity(newQty);
                        FirebaseHelper.getInstance().updateInventoryItem(uid, item, new FirebaseHelper.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Cập nhật số lượng thành công!", Toast.LENGTH_SHORT).show();
                                loadInventoryData();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteItem(InventoryItem item) {
        FirebaseHelper.getInstance().deleteInventoryItem(uid, item.getId(), new FirebaseHelper.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Đã dùng hết '" + item.getName() + "'! 🧼", Toast.LENGTH_SHORT).show();
                loadInventoryData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markExpired(InventoryItem item) {
        item.setStatus("expired");
        FirebaseHelper.getInstance().updateInventoryItem(uid, item, new FirebaseHelper.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Đã đánh dấu hết hạn!", Toast.LENGTH_SHORT).show();
                loadInventoryData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String translateCategory(String cat) {
        if (cat == null) return "Khác";
        switch (cat) {
            case "vegetables": return "Rau củ quả";
            case "proteins": return "Đạm / Thịt cá";
            case "pantry": return "Đồ khô / Gia vị";
            default: return cat;
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "Bình thường";
        switch (status) {
            case "fresh": return "Tươi mới";
            case "use_soon": return "Dùng ngay";
            case "expired": return "Đã hết hạn!";
            default: return status;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
