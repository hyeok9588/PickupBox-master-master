package app.project.com.pickupbox.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;

public class LogoutService extends Service {

    public  static CountDownTimer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new CountDownTimer(1 * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //Some code
                Log.v("LogoutService", "Service Started");

                //로그아웃--------------------------------------------------------------------------------------//
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
                editor.clear();
                editor.commit();

            }

            public void onFinish() {
                Log.v("LogoutService", "Call Logout by Service");
                // Code for Logout
                stopSelf();
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
