package app.dailyroutine;

public class ExpenseModel {
    private double amount;
    private String note;
    private String date;
    private String category;
    private boolean recurring = false;
    private String recurrenceType = "";
    private boolean hasPhoto = false;
    private boolean hasLocation = false;
    private String photoPath = "";
    private String locationAddress = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private long timestamp;

    // Default constructor
    public ExpenseModel() {
        this.timestamp = System.currentTimeMillis();
        this.category = "Other";
    }

    // Basic constructor (backward compatibility)
    public ExpenseModel(double amount, String note, String date) {
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.category = detectCategoryFromNote(note);
        this.timestamp = System.currentTimeMillis();
    }

    // Full constructor with recurring
    public ExpenseModel(double amount, String note, String date, boolean recurring, String recurrenceType) {
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.recurring = recurring;
        this.recurrenceType = recurrenceType;
        this.category = detectCategoryFromNote(note);
        this.timestamp = System.currentTimeMillis();
    }

    // Complete constructor with all features
    public ExpenseModel(double amount, String note, String category, boolean recurring,
                        String recurrenceType, String date, boolean hasPhoto, boolean hasLocation,
                        String photoPath, String locationAddress) {
        this.amount = amount;
        this.note = note;
        this.category = category;
        this.recurring = recurring;
        this.recurrenceType = recurrenceType;
        this.date = date;
        this.hasPhoto = hasPhoto;
        this.hasLocation = hasLocation;
        this.photoPath = photoPath;
        this.locationAddress = locationAddress;
        this.timestamp = System.currentTimeMillis();
    }

     private String detectCategoryFromNote(String note) {
        if (note == null) return "Other";

        String lowerNote = note.toLowerCase();
        if (lowerNote.contains("food") || lowerNote.contains("restaurant") ||
                lowerNote.contains("coffee") || lowerNote.contains("lunch") ||
                lowerNote.contains("dinner") || lowerNote.contains("🍔") ||
                lowerNote.contains("🍕") || lowerNote.contains("☕")) {
            return "Food & Drinks";
        } else if (lowerNote.contains("taxi") || lowerNote.contains("uber") ||
                lowerNote.contains("bus") || lowerNote.contains("train") ||
                lowerNote.contains("fuel") || lowerNote.contains("🚗") ||
                lowerNote.contains("🚕")) {
            return "Transportation";
        } else if (lowerNote.contains("shopping") || lowerNote.contains("clothes") ||
                lowerNote.contains("amazon") || lowerNote.contains("flipkart") ||
                lowerNote.contains("🛒") || lowerNote.contains("🛍️")) {
            return "Shopping";
        } else if (lowerNote.contains("movie") || lowerNote.contains("game") ||
                lowerNote.contains("netflix") || lowerNote.contains("spotify") ||
                lowerNote.contains("🎬") || lowerNote.contains("🎮")) {
            return "Entertainment";
        } else if (lowerNote.contains("electricity") || lowerNote.contains("water") ||
                lowerNote.contains("internet") || lowerNote.contains("phone") ||
                lowerNote.contains("bill") || lowerNote.contains("💡")) {
            return "Bills & Utilities";
        } else if (lowerNote.contains("gym") || lowerNote.contains("doctor") ||
                lowerNote.contains("medicine") || lowerNote.contains("hospital") ||
                lowerNote.contains("🏥") || lowerNote.contains("💊")) {
            return "Health & Fitness";
        } else if (lowerNote.contains("book") || lowerNote.contains("course") ||
                lowerNote.contains("school") || lowerNote.contains("college") ||
                lowerNote.contains("📚") || lowerNote.contains("🎓")) {
            return "Education";
        }
        return "Other";
    }

    // Getters
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public boolean isRecurring() { return recurring; }
    public String getRecurrenceType() { return recurrenceType; }
    public boolean hasPhoto() { return hasPhoto; }
    public boolean hasLocation() { return hasLocation; }
    public String getPhotoPath() { return photoPath; }
    public String getLocationAddress() { return locationAddress; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setAmount(double amount) { this.amount = amount; }
    public void setNote(String note) {
        this.note = note;
        if (this.category.equals("Other")) {
            this.category = detectCategoryFromNote(note);
        }
    }
    public void setDate(String date) { this.date = date; }
    public void setCategory(String category) { this.category = category; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }
    public void setHasPhoto(boolean hasPhoto) { this.hasPhoto = hasPhoto; }
    public void setHasLocation(boolean hasLocation) { this.hasLocation = hasLocation; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Utility methods for UI
    public int getCategoryIcon() {
        switch (category.toLowerCase()) {
            case "food & drinks":
            case "food":
                return R.drawable.ic_food;
            case "transportation":
            case "transport":
                return R.drawable.ic_transport;
            case "shopping":
                return R.drawable.ic_shopping;
            case "entertainment":
                return R.drawable.ic_entertainment;
            case "bills & utilities":
            case "bills":
                return R.drawable.ic_bills;
            case "health & fitness":
            case "health":
                return R.drawable.ic_health;
            case "education":
                return R.drawable.ic_education;
            default:
                return R.drawable.ic_other;
        }
    }

    public int getCategoryColor() {
        switch (category.toLowerCase()) {
            case "food & drinks":
            case "food":
                return R.color.category_food;
            case "transportation":
            case "transport":
                return R.color.category_transport;
            case "shopping":
                return R.color.category_shopping;
            case "entertainment":
                return R.color.category_entertainment;
            case "bills & utilities":
            case "bills":
                return R.color.category_bills;
            case "health & fitness":
            case "health":
                return R.color.category_health;
            case "education":
                return R.color.category_education;
            default:
                return R.color.category_other;
        }
    }

    public String getFormattedAmount() {
        return String.format("₹%.2f", amount);
    }

    public String getCategoryDisplayName() {
        return category != null ? category : "Other";
    }
}
