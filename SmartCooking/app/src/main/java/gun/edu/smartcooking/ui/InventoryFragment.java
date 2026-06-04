package gun.edu.smartcooking.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private ValueEventListener inventoryListener;
    private DatabaseReference inventoryDbRef;

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

        // Lấy User ID của người dùng hiện tại đang đăng nhập từ Firebase Auth
        uid = FirebaseAuth.getInstance().getUid();

        // [TỐI ƯU GIAO DIỆN] Cấu hình RecyclerView hiển thị dạng lưới (Grid) 2 cột
        binding.rvInventory.setLayoutManager(new GridLayoutManager(getContext(), 2));
        inventoryAdapter = new InventoryAdapter(requireContext(), itemList);
        binding.rvInventory.setAdapter(inventoryAdapter); // Gán adapter kết nối dữ liệu với RecyclerView

        // Cài đặt sự kiện vuốt từ trên xuống để làm mới dữ liệu (Pull-to-Refresh)
        binding.swipeRefreshInventory.setColorSchemeColors(getResources().getColor(R.color.primary, null));
        binding.swipeRefreshInventory.setOnRefreshListener(() -> {
            // Khi kéo làm mới, gỡ listener cũ ra để tránh đăng ký trùng lặp và tải lại
            if (inventoryDbRef != null && inventoryListener != null) {
                inventoryDbRef.removeEventListener(inventoryListener);
            }
            loadInventoryData();
        });

        // Thiết lập sự kiện khi click vào từng ô thực phẩm (Hiện Dialog chi tiết thực phẩm)
        inventoryAdapter.setOnItemClickListener(item -> showIngredientDetail(item));

        // Thiết lập sự kiện khi click vào nút ba chấm "Xem thêm" (Hiện menu Sửa/Xóa/Hết hạn)
        inventoryAdapter.setOnMoreClickListener((item, anchorView) -> showMoreOptions(item));

        // [TÍNH NĂNG AI] Sự kiện click nút AI gợi ý nấu ăn từ danh sách thực phẩm trong kho
        binding.btnGenerateRecipe.setOnClickListener(v -> {
            if (itemList.isEmpty()) {
                Toast.makeText(getContext(), "Tủ lạnh của bạn đang trống! Hãy thêm thực phẩm trước.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Đang kết nối Trợ lý AI...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                // Tạo câu lệnh (Prompt) gửi đi chứa tối đa 6 nguyên liệu hiện có trong tủ lạnh
                StringBuilder builder = new StringBuilder();
                builder.append("Tôi đang có các nguyên liệu sau trong tủ lạnh:\n");
                for (int i = 0; i < Math.min(itemList.size(), 6); i++) {
                    builder.append("- ").append(itemList.get(i).getName()).append(" (").append(itemList.get(i).getQuantity()).append(")\n");
                }
                builder.append("Hãy gợi ý cho tôi 2 công thức nấu ăn ngon nhất từ những nguyên liệu này nhé!");
                
                // Chuyển sang màn hình Trợ lý AI chat và tự động gửi câu hỏi đi
                ((MainActivity) getActivity()).navigateToChefAssistant(builder.toString());
            }
        });

        // [THÊM MỚI THỰC PHẨM] Click vào nút Floating Action Button (+) để thêm mới thực phẩm
        binding.fabAddItem.setOnClickListener(v -> {
            // Nạp layout Dialog nhập liệu từ file xml dialog_add_ingredient
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null, false);
            EditText etIngredientName = dialogView.findViewById(R.id.etIngredientName);
            EditText etIngredientQuantity = dialogView.findViewById(R.id.etIngredientQuantity);
            EditText etIngredientExpiry = dialogView.findViewById(R.id.etIngredientExpiry);

            // Hiển thị Dialog thiết kế bằng Material Design
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

                        // Thuật toán nhận diện phân loại & Emoji tự động dựa trên từ khóa tiếng Việt
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

                        // Nhận diện trạng thái hạn sử dụng (fresh, use_soon, expired)
                        if (expiryText.toLowerCase().contains("hết hạn") || expiryText.toLowerCase().contains("0") || expiryText.toLowerCase().contains("quá hạn")) {
                            status = "expired";
                        } else if (expiryText.toLowerCase().contains("1") || expiryText.toLowerCase().contains("2") || expiryText.toLowerCase().contains("gấp") || expiryText.toLowerCase().contains("sắp")) {
                            status = "use_soon";
                        }

                        // Khởi tạo đối tượng thực phẩm mới
                        InventoryItem newItem = new InventoryItem(null, name, qty, category, status, emoji);
                        // Gọi phương thức trong FirebaseHelper để đẩy dữ liệu lên Firebase
                        FirebaseHelper.getInstance().addInventoryItem(uid, newItem, new FirebaseHelper.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Thêm thành công -> Listener của Firebase sẽ tự bắt sự kiện và vẽ lại màn hình ngay lập tức!
                                Toast.makeText(getContext(), "Đã thêm thực phẩm '" + name + "' vào tủ lạnh! 🎉", Toast.LENGTH_SHORT).show();
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

        // Bắt đầu lắng nghe và tải dữ liệu từ Firebase
        loadInventoryData();
    }

    // [REAL-TIME SYNCHRONIZATION] ĐỒNG BỘ HÓA DỮ LIỆU TỦ LẠNH THỜI GIAN THỰC TỪ FIREBASE
    private void loadInventoryData() {
        if (uid == null) {
            Log.e("InventoryFragment", "loadInventoryData: uid is null");
            return;
        }

        Log.d("InventoryFragment", "loadInventoryData: starting listener for uid = " + uid);
        if (binding != null && !binding.swipeRefreshInventory.isRefreshing()) {
            binding.swipeRefreshInventory.setRefreshing(true); // Hiển thị xoay tròn loading làm mới
        }

        // Đảm bảo không đăng ký trùng lặp listener để tránh rò rỉ bộ nhớ
        if (inventoryDbRef != null && inventoryListener != null) {
            inventoryDbRef.removeEventListener(inventoryListener);
        }

        // Lấy tham chiếu tới node lưu trữ tủ lạnh của người dùng: /users/{uid}/inventory
        inventoryDbRef = FirebaseDatabase.getInstance("https://android-projects-cae81-default-rtdb.firebaseio.com/")
                .getReference().child("users").child(uid).child("inventory");

        // Đăng ký bộ lắng nghe dữ liệu thời gian thực (ValueEventListener)
        inventoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("InventoryFragment", "onDataChange: snapshot exists = " + snapshot.exists() + ", children count = " + snapshot.getChildrenCount());
                if (binding == null) {
                    Log.w("InventoryFragment", "onDataChange: binding is null, skipping UI update");
                    return; // Tránh NullPointerException nếu giao diện đã bị đóng
                }
                binding.swipeRefreshInventory.setRefreshing(false); // Tắt xoay tròn loading

                itemList.clear(); // Xóa dữ liệu cũ đi để chuẩn bị nạp dữ liệu mới
                // Duyệt qua tất cả các con (các thực phẩm) có trong node inventory
                for (DataSnapshot ds : snapshot.getChildren()) {
                    InventoryItem item = ds.getValue(InventoryItem.class); // Tự động map JSON sang Object Java
                    if (item != null) {
                        item.setId(ds.getKey()); // Lưu khóa ID của Firebase vào item
                        itemList.add(item);     // Thêm vào danh sách cục bộ
                    }
                }
                Log.d("InventoryFragment", "onDataChange: loaded " + itemList.size() + " items from Firebase");

                inventoryAdapter.notifyDataSetChanged(); // Kích hoạt vẽ lại toàn bộ danh sách lên giao diện RecyclerView

                // Quản lý trạng thái Trống (Empty State) - Nếu rỗng thì hiện layout thông báo trống, ngược lại hiện danh sách
                if (itemList.isEmpty()) {
                    binding.layoutEmptyInventory.setVisibility(View.VISIBLE);
                    binding.rvInventory.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyInventory.setVisibility(View.GONE);
                    binding.rvInventory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InventoryFragment", "onCancelled: Firebase error: " + error.getMessage());
                if (binding == null) return;
                binding.swipeRefreshInventory.setRefreshing(false);
                // Hiển thị Toast thông báo nếu gặp lỗi đọc dữ liệu từ Firebase (ví dụ: lỗi Rules chặn quyền)
                Toast.makeText(getContext(), "Không thể tải danh sách thực phẩm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        // Gắn listener vào cơ sở dữ liệu Firebase
        inventoryDbRef.addValueEventListener(inventoryListener);
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
        if (inventoryDbRef != null && inventoryListener != null) {
            inventoryDbRef.removeEventListener(inventoryListener);
        }
        binding = null;
    }
}
