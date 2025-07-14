package app.dailyroutine;

import java.io.Serializable;

public class HistoryModel implements Serializable {
    private final String dateTime;
    private final int amount;
    private long id;

    public HistoryModel(String dateTime, int amount) {
        this.dateTime = dateTime;
        this.amount = amount;
        this.id = System.currentTimeMillis(); // Simple ID generation
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getAmount() {
        return amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        HistoryModel that = (HistoryModel) obj;
        return amount == that.amount &&
                dateTime != null ? dateTime.equals(that.dateTime) : that.dateTime == null;
    }

    @Override
    public int hashCode() {
        int result = dateTime != null ? dateTime.hashCode() : 0;
        result = 31 * result + amount;
        return result;
    }

    @Override
    public String toString() {
        return "HistoryModel{" +
                "dateTime='" + dateTime + '\'' +
                ", amount=" + amount +
                ", id=" + id +
                '}';
    }
}
