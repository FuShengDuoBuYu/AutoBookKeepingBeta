package com.beta.autobookkeeping.service;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.core.app.NotificationManagerCompat;

public class StartAutoBookTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!hasNotificationAccess()) {
            startActivityAndCollapse(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return;
        }
        NotificationListenerService.requestRebind(new ComponentName(this, NotificationReceiver.class));
        updateTileState();
    }

    private boolean hasNotificationAccess() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean enabled = hasNotificationAccess();
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(enabled ? "自动记账已开启" : "开启自动记账");
        tile.updateTile();
    }
}
