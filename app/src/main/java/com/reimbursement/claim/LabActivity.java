package com.reimbursement.claim;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.BatteryManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class LabActivity extends Activity {
    private LinearLayout logBox;
    private final int blue = Color.rgb(49, 92, 186);
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView label(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void addLog(String s) {
        TextView t = label("• " + s, 13, Color.rgb(52,64,84), false);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        logBox.addView(t, new LinearLayout.LayoutParams(-1, -2));
    }

    private Button action(String title, Runnable task) {
        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(blue, 12, 0));
        b.setOnClickListener(v -> task.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(50));
        p.topMargin = dp(9);
        returnButtonParent.addView(b, p);
        return b;
    }

    private LinearLayout returnButtonParent;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(243,246,252));
        getWindow().setNavigationBarColor(Color.rgb(243,246,252));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(243,246,252));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(26), dp(20), dp(28));
        scroll.addView(root);

        TextView title = label("Security Testing Lab", 26, Color.rgb(23,43,77), true);
        root.addView(title);
        TextView subtitle = label("Controlled tests for your own device", 14, Color.rgb(105,119,140), false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2);
        sp.topMargin = dp(5);
        root.addView(subtitle, sp);

        TextView notice = label("TEST MODE • No hidden collection • No data is sent to a server", 12, Color.rgb(35,110,75), true);
        notice.setPadding(dp(12),dp(12),dp(12),dp(12));
        notice.setBackground(shape(Color.rgb(226,245,235),12,Color.rgb(195,228,207)));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1,-2);
        np.topMargin = dp(18);
        root.addView(notice,np);

        TextView section = label("TEST MODULES", 13, Color.rgb(105,119,140), true);
        LinearLayout.LayoutParams secp = new LinearLayout.LayoutParams(-1,-2);
        secp.topMargin = dp(22);
        root.addView(section,secp);

        returnButtonParent = root;
        action("1. Device information check", this::deviceCheck);
        action("2. Network connection check", this::networkCheck);
        action("3. Camera permission test", () -> requestOne(Manifest.permission.CAMERA, 410));
        action("4. Microphone permission test", () -> requestOne(Manifest.permission.RECORD_AUDIO, 411));
        action("5. Location permission test", () -> requestOne(Manifest.permission.ACCESS_FINE_LOCATION, 412));
        action("6. Run all safe local checks", this::runAll);

        TextView logTitle = label("TEST RESULTS", 16, Color.rgb(23,43,77), true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(24);
        root.addView(logTitle,lp);

        logBox = new LinearLayout(this);
        logBox.setOrientation(LinearLayout.VERTICAL);
        logBox.setPadding(dp(8),dp(8),dp(8),dp(8));
        logBox.setBackground(shape(Color.WHITE,14,Color.rgb(226,232,241)));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1,-2);
        bp.topMargin = dp(10);
        root.addView(logBox,bp);
        addLog("Lab ready. Choose a test above.");
        setContentView(scroll);
    }

    private void deviceCheck() {
        int level = -1;
        Intent battery = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        addLog("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        addLog("Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        addLog("Battery: " + (level >= 0 ? level + "%" : "Not available"));
        addLog("App package: " + getPackageName());
        addLog("Result: device information check completed locally.");
    }

    private void networkCheck() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm == null ? null : cm.getActiveNetworkInfo();
        boolean connected = info != null && info.isConnected();
        addLog("Network: " + (connected ? info.getTypeName() + " connected" : "No active network"));
        addLog("No traffic was sent by this test.");
    }

    private void requestOne(String permission, int code) {
        if (Build.VERSION.SDK_INT < 23) {
            addLog(permission + ": runtime permission is not required on this Android version.");
            return;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            addLog(permission + ": already granted. No camera/microphone/location data was accessed.");
        } else {
            addLog("Android permission prompt requested for " + permission);
            requestPermissions(new String[]{permission}, code);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (results.length == 0) return;
        String name = permissions[0];
        addLog(name + (results[0] == PackageManager.PERMISSION_GRANTED
            ? ": user granted permission. This test did not activate the sensor."
            : ": permission denied by user."));
    }

    private void runAll() {
        deviceCheck();
        networkCheck();
        addLog("Permission posture: CAMERA=" + granted(Manifest.permission.CAMERA)
            + ", MICROPHONE=" + granted(Manifest.permission.RECORD_AUDIO)
            + ", LOCATION=" + granted(Manifest.permission.ACCESS_FINE_LOCATION));
        addLog("Combined safe local checks completed.");
    }

    private String granted(String p) {
        return checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "NOT GRANTED";
    }
}
