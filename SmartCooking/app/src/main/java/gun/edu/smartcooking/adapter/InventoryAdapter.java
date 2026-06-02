package gun.edu.smartcooking.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gun.edu.smartcooking.R;
import gun.edu.smartcooking.model.InventoryItem;

/**
 * Adapter hiển thị danh sách thực phẩm trong Tủ lạnh động.
 * Đã tối ưu hóa click listeners trong onCreateViewHolder để giảm thiểu GC Overhead.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final Context context;
    private final List<InventoryItem> items;
    private OnItemClickListener itemClickListener;
    private OnMoreClickListener moreClickListener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public interface OnMoreClickListener {
        void onMoreClick(InventoryItem item, View anchorView);
    }

    public InventoryAdapter(Context context, List<InventoryItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.moreClickListener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_card, parent, false);
        InventoryViewHolder holder = new InventoryViewHolder(view);

        // Đăng ký sự kiện Click tại đây để tối ưu hóa bộ nhớ
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && itemClickListener != null) {
                itemClickListener.onItemClick(items.get(pos));
            }
        });

        holder.btnItemMore.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && moreClickListener != null) {
                moreClickListener.onMoreClick(items.get(pos), holder.btnItemMore);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvItemQuantity.setText("Số lượng: " + item.getQuantity());

        // Thiết lập Emoji hiển thị
        String emoji = item.getIconEmoji();
        if (emoji == null || emoji.isEmpty()) {
            emoji = getDefaultEmoji(item.getCategory());
        }
        holder.tvItemEmoji.setText(emoji);

        // Thiết lập Nhãn Phân loại
        String category = item.getCategory();
        if (category != null) {
            switch (category) {
                case "vegetables":
                    holder.tvItemCategoryBadge.setText("Rau củ");
                    holder.tvItemCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
                    break;
                case "proteins":
                    holder.tvItemCategoryBadge.setText("Thịt cá");
                    holder.tvItemCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.secondary)));
                    break;
                case "pantry":
                    holder.tvItemCategoryBadge.setText("Gia vị");
                    holder.tvItemCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.text_secondary)));
                    break;
                default:
                    holder.tvItemCategoryBadge.setText(category);
                    holder.tvItemCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
            }
        }

        // Thiết lập Nhãn Trạng thái hạn dùng & ProgressBar
        String status = item.getStatus();
        if (status != null) {
            switch (status) {
                case "fresh":
                    holder.tvItemStatusBadge.setText("Tươi mới");
                    holder.tvItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_fresh);
                    holder.pbItemExpiry.setProgress(100);
                    holder.pbItemExpiry.setProgressTintList(ColorStateList.valueOf(context.getColor(R.color.success)));
                    break;
                case "use_soon":
                    holder.tvItemStatusBadge.setText("Dùng ngay");
                    holder.tvItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_use_soon);
                    holder.pbItemExpiry.setProgress(40);
                    holder.pbItemExpiry.setProgressTintList(ColorStateList.valueOf(context.getColor(R.color.warning)));
                    break;
                case "expired":
                    holder.tvItemStatusBadge.setText("Hết hạn");
                    holder.tvItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_expired);
                    holder.pbItemExpiry.setProgress(10);
                    holder.pbItemExpiry.setProgressTintList(ColorStateList.valueOf(context.getColor(R.color.error)));
                    break;
                default:
                    holder.tvItemStatusBadge.setText(status);
                    holder.tvItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_fresh);
                    holder.pbItemExpiry.setProgress(100);
                    holder.pbItemExpiry.setProgressTintList(ColorStateList.valueOf(context.getColor(R.color.success)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<InventoryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    private String getDefaultEmoji(String category) {
        if (category == null) return "🍎";
        switch (category) {
            case "vegetables": return "🥦";
            case "proteins": return "🥩";
            case "pantry": return "🧂";
            default: return "🍎";
        }
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemEmoji, tvItemStatusBadge, tvItemCategoryBadge, tvItemName, tvItemQuantity;
        ImageView btnItemMore;
        ProgressBar pbItemExpiry;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemEmoji = itemView.findViewById(R.id.tvItemEmoji);
            tvItemStatusBadge = itemView.findViewById(R.id.tvItemStatusBadge);
            tvItemCategoryBadge = itemView.findViewById(R.id.tvItemCategoryBadge);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            btnItemMore = itemView.findViewById(R.id.btnItemMore);
            pbItemExpiry = itemView.findViewById(R.id.pbItemExpiry);
        }
    }
}
