package app.dailyroutine;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class SmsWorker extends Worker {

    public SmsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        String nameUser = getInputData().getString("nameUser");
        String phoneNumber = getInputData().getString("phoneNumber");
        int totalAmount = getInputData().getInt("totalAmount", 0);

        Log.d("SENDSMS", "Worker started: " + nameUser + ", " + phoneNumber);

//        String message = " *Pocket Money Update*\n\n" +
//                "Hello " + nameUser + ",\n" +
//                "Your total expense recorded for today is ₹" + totalAmount + ".\n\n" +
//                "Thank you for using *Pocket Money*! ";
       String message = "Hello " + nameUser + ", your total expense today is ₹" + totalAmount + ".";
        try {
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
            }

            List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfos != null && !subscriptionInfos.isEmpty()) {
                SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionInfos.get(0).getSubscriptionId());
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } else {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }

            Log.d("SENDSMS", "SMS sent successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e("SENDSMS", "SMS failed: " + e.getMessage());
            return Result.failure();
        }
    }
}