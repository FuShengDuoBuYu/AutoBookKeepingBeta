package com.beta.autobookkeeping.service;

import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.beta.autobookkeeping.smsTools.SMSService;

import Util.ServiceUtil;

public class StartAutoBookTileService extends TileService {
    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        Context context = getApplicationContext();
        super.onTileAdded();
        context.startService(new Intent(context, NotificationReceiver.class));
        context.startService(new Intent(context, SMSService.class));
        context.startService(new Intent(context, TodoNotificationSender.class));
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        //启动一个service
        if(!ServiceUtil.isMyServiceRunning(NotificationReceiver.class,getApplicationContext())){
            Context context = getApplicationContext();
            context.startService(new Intent(context, NotificationReceiver.class));
            Toast.makeText(context, "启动自动记账服务", Toast.LENGTH_SHORT).show();
        }
        if(!ServiceUtil.isMyServiceRunning(TodoNotificationSender.class,getApplicationContext())){
            Context context = getApplicationContext();
            context.startService(new Intent(context, TodoNotificationSender.class));
            Toast.makeText(context, "启动待办提醒服务", Toast.LENGTH_SHORT).show();
        }
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
        super.onStartListening();
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        //收起通知栏
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.updateTile();
        super.onClick();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
