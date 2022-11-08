package com.beta.autobookkeeping.service;

import static Util.ConstVariable.IP;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.familyTodo.FamilyTodoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TodoNotificationSender extends Service {

    //要跳转的Activity

    Intent intent;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        intent = new Intent(TodoNotificationSender.this.getApplicationContext(), FamilyTodoActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //设置定时器,30min执行一次
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCheck();
                //再次执行
                onStart(intent, startId);
            }
        }, 1000 * 60 * 30);
        //开始轮询
        startCheck();
        super.onStart(intent, startId);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TodoNotification", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(int notificationId, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "TodoNotification")
                .setSmallIcon(R.drawable.start_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(notificationId, builder.build());
    }

    private void startCheck() {
        //要执行的操作
        new Thread(()->{
            String url = IP + "/todoItem/getNotificationEntry/"+ SpUtils.get(TodoNotificationSender.this,"phoneNum","");
            OkHttpClient client = new OkHttpClient();
            Request requst = new Request.Builder().url(url).get().build();
            try {
                Response response = client.newCall(requst).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                //转为JsonArray
                JSONArray jsonArray = jsonResponse.getJSONArray("data");
                //发送通知
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sendNotification(jsonObject.getInt("id"), jsonObject.getString("notificationTitle"), jsonObject.getString("notificationContent"));
                }
            } catch (IOException | JSONException e) {
                Looper.prepare();
                ProjectUtil.toastMsg(TodoNotificationSender.this,e.getMessage());
                e.printStackTrace();
                Looper.loop();
            }
        }).start();
    }
}
