package app.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MoneyTipsProvider {

    public static String getTip(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("TipsPrefs", Context.MODE_PRIVATE);
        String lastShownDate = prefs.getString("last_date", "");
        int lastIndex = prefs.getInt("last_index", -1);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        List<String> tips = loadTipsFromJson(context);
        if (tips.isEmpty()) return "Stay financially smart!";

        if (!today.equals(lastShownDate)) {
            int newIndex = new Random().nextInt(tips.size());
            prefs.edit()
                    .putString("last_date", today)
                    .putInt("last_index", newIndex)
                    .apply();
            return tips.get(newIndex);
        } else {
            return tips.get(lastIndex);
        }
    }

    private static List<String> loadTipsFromJson(Context context) {
        List<String> tips = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("money_tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                tips.add(jsonArray.getString(i));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return tips;
    }
}