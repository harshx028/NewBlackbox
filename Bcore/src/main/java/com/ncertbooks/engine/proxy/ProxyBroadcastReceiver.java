package com.ncertbooks.engine.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import com.ncertbooks.engine.BlackBoxCore;
import com.ncertbooks.engine.entity.am.PendingResultData;
import com.ncertbooks.engine.proxy.record.ProxyBroadcastRecord;


public class ProxyBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "ProxyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setExtrasClassLoader(context.getClassLoader());
        ProxyBroadcastRecord record = ProxyBroadcastRecord.create(intent);
        if (record.mIntent == null) {
            return;
        }
        PendingResult pendingResult = goAsync();
        // Safety net: finish before the 10s broadcast ANR fires even if black process dies
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable safeFinish = pendingResult::finish;
        handler.postDelayed(safeFinish, 8000);
        try {
            BlackBoxCore.getBActivityManager().scheduleBroadcastReceiver(record.mIntent, new PendingResultData(pendingResult), record.mUserId);
        } catch (RemoteException e) {
            handler.removeCallbacks(safeFinish);
            pendingResult.finish();
        }
    }
}