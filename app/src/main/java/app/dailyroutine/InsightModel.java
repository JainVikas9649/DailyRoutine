package app.dailyroutine;

public class InsightModel {
    private String title;
    private String description;
    private InsightType type;

    public enum InsightType {
        TREND, WARNING, TIP, SAVINGS, INFO
    }

    public InsightModel(String title, String description, InsightType type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public InsightType getType() { return type; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setType(InsightType type) { this.type = type; }
}
