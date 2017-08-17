package com.duan.musicoco.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.duan.musicoco.app.RootService;
import com.duan.musicoco.app.manager.BroadcastManager;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 * 启动该服务的应用应确保获取了文件读取权限
 */

public class PlayService extends RootService {

    private static final String TAG = "PlayService";

    private PlayServiceIBinder iBinder;
    private BroadcastManager broadcastManager;
    private BroadcastReceiver serviceQuit;

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastManager = BroadcastManager.getInstance(this);
        iBinder = new PlayServiceIBinder(getApplicationContext());

        new ServiceInit(this, iBinder, mediaManager, playPreference, dbController).start();
        iBinder.notifyDataIsReady();

        initBroadcast();

    }

    private void initBroadcast() {
        serviceQuit = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (iBinder.status() == PlayController.STATUS_PLAYING) {
                    iBinder.pause();
                }
                iBinder.releaseMediaPlayer();
                stopSelf();
            }
        };
        broadcastManager.registerBroadReceiver(serviceQuit, BroadcastManager.FILTER_PLAY_SERVICE_QUIT);
    }

    @Override
    public IBinder onBind(Intent intent) {

        int check = checkCallingOrSelfPermission("com.duan.musicoco.permission.ACCESS_PLAY_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "you need declare permission 'com.duan.musicoco.permission.ACCESS_PLAY_SERVICE' to access this service.");
            //客户端的 onServiceConnected 方法不会被调用
            return null;
        }

        return iBinder;
    }

    @Override
    public void onDestroy() {
        if (iBinder.isBinderAlive()) {
            iBinder.releaseMediaPlayer();
        }
        unregisterReceiver();
        super.onDestroy();
    }

    private void unregisterReceiver() {
        if (serviceQuit != null) {
            broadcastManager.unregisterReceiver(serviceQuit);
        }
    }

}
