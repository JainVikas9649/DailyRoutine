//package app.dailyroutine;
//
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//
//import com.prolificinteractive.materialcalendarview.CalendarDay;
//import com.prolificinteractive.materialcalendarview.DayViewDecorator;
//import com.prolificinteractive.materialcalendarview.DayViewFacade;
//
//import java.text.SimpleDateFormat;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//
//public class EnhancedEventDecorator implements DayViewDecorator {
//
//    private final HashMap<String, List<ExpenseModel>> expenseMap;
//  //  private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//
//    public EnhancedEventDecorator(HashMap<String, List<ExpenseModel>> expenseMap) {
//        this.expenseMap = expenseMap;
//    }
//
//    @Override
//    public boolean shouldDecorate(CalendarDay day) {
//        String dateKey = sdf.format(day.getDate());
//        return expenseMap.containsKey(dateKey);
//    }
//
//    @Override
//    public void decorate(DayViewFacade view) {
//        // Get the current day to determine expense amount
////           CalendarDay day = view.getDate();
////          String dateKey = sdf.format(day.getDate());
////
////        if (expenseMap.containsKey(dateKey)) {
////            List<ExpenseModel> expenses = expenseMap.get(dateKey);
////            double totalAmount = 0;
////
////            for (ExpenseModel expense : expenses) {
////                totalAmount += expense.getAmount();
////            }
////
////             Drawable drawable;
////            if (totalAmount > 1000) {
////                drawable = new ColorDrawable(Color.parseColor("#FF5722"));
////            } else if (totalAmount > 500) {
////                drawable = new ColorDrawable(Color.parseColor("#FF9800"));
////            } else {
////                drawable = new ColorDrawable(Color.parseColor("#4CAF50"));
////            }
////
////            view.setBackgroundDrawable(drawable);
////        }
//    }
//}
package app.dailyroutine;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;

public class EnhancedEventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final Drawable backgroundDrawable;

    public EnhancedEventDecorator(HashSet<CalendarDay> dates, int color) {
        this.dates = dates;
        this.backgroundDrawable = new ColorDrawable(color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(backgroundDrawable);
    }
}