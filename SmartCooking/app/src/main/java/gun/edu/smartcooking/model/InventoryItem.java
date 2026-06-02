package gun.edu.smartcooking.model;

/**
 * Model class cho item trong kho thực phẩm
 */
public class InventoryItem {

    private String id;
    private String name;
    private String quantity;
    private String category;    // vegetables, proteins, pantry
    private String status;      // fresh, use_soon, expired
    private String iconEmoji;   // Emoji icon đại diện

    // Constructor mặc định (cần cho Firebase)
    public InventoryItem() {}

    public InventoryItem(String id, String name, String quantity,
                         String category, String status, String iconEmoji) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.status = status;
        this.iconEmoji = iconEmoji;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }
}
