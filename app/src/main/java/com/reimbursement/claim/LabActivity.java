package com.reimbursement.claim;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.net.Uri;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class LabActivity extends Activity {
    private static final int REQ_APK = 701;
    private static final int REQ_SCREEN = 702;
    private static final int REQ_REPORT = 703;
    private static final int REQ_CAMERA = 410, REQ_MIC = 411, REQ_LOCATION = 412, REQ_NOTIF = 413;
    private LinearLayout logBox;
    private LinearLayout buttonParent;
    private final StringBuilder report = new StringBuilder("Reimbursement Claim - Security Testing Report\n");
    private ConnectivityManager.NetworkCallback networkCallback;
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke); return d;
    }
    private TextView label(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private void addLog(String s) {
        report.append("\n• ").append(s);
        TextView t = label("• " + s, 13, Color.rgb(52,64,84), false);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        logBox.addView(t, new LinearLayout.LayoutParams(-1, -2));
    }
    private void action(String title, Runnable task) {
        Button b = new Button(this); b.setText(title); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setTextSize(14); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setBackground(shape(Color.rgb(49,92,186),12,0));
        b.setOnClickListener(v -> task.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(50)); p.topMargin = dp(8);
        buttonParent.addView(b,p);
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(243,246,252));
        getWindow().setNavigationBarColor(Color.rgb(243,246,252));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(Color.rgb(243,246,252));
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(24),dp(20),dp(28)); scroll.addView(root);
        root.addView(label("Security Testing Lab",26,Color.rgb(23,43,77),true));
        TextView sub=label("Advanced controlled tests • Your device, your consent",14,Color.rgb(105,119,140),false);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(5);root.addView(sub,sp);
        TextView notice=label("VISIBLE TEST MODE • No stealth • No private data upload",12,Color.rgb(35,110,75),true);
        notice.setPadding(dp(12),dp(12),dp(12),dp(12));notice.setBackground(shape(Color.rgb(226,245,235),12,Color.rgb(195,228,207)));
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(16);root.addView(notice,np);
        TextView heading=label("TEST MODULES",13,Color.rgb(105,119,140),true);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(20);root.addView(heading,hp);
        buttonParent=root;
        action("1. Full device posture check",this::deviceCheck);
        action("2. Live network change monitor",this::startNetworkMonitor);
        action("3. APK permission & signing analyzer",this::pickApk);
        action("4. Camera permission test",()->requestOne(Manifest.permission.CAMERA,REQ_CAMERA));
        action("5. Microphone permission test",()->requestOne(Manifest.permission.RECORD_AUDIO,REQ_MIC));
        action("6. Location permission test",()->requestOne(Manifest.permission.ACCESS_FINE_LOCATION,REQ_LOCATION));
        action("7. Local test notification",this::testNotification);
        action("8. Notification access status",this::notificationAccessInfo);
        action("9. Screen-capture consent test",this::screenConsentTest);
        action("10. Export test report",this::exportReport);
        action("11. Run all local checks",this::runAll);
        TextView logTitle=label("TEST RESULTS",16,Color.rgb(23,43,77),true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(22);root.addView(logTitle,lp);
        logBox=new LinearLayout(this);logBox.setOrientation(LinearLayout.VERTICAL);logBox.setPadding(dp(8),dp(8),dp(8),dp(8));
        logBox.setBackground(shape(Color.WHITE,14,Color.rgb(226,232,241)));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(9);root.addView(logBox,bp);
        addLog("Lab ready. Tests run only after you tap a module.");
        setContentView(scroll);
    }
    private void deviceCheck() {
        int level=-1; Intent battery=registerReceiver(null,new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(battery!=null)level=battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        addLog("Device: "+Build.MANUFACTURER+" "+Build.MODEL);
        addLog("Android: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");
        addLog("Battery: "+(level>=0?level+"%":"Not available"));
        addLog("App version: "+getPackageName()+" / "+getPackageManager().getInstallerPackageName(getPackageName()));
        addLog("Camera permission: "+granted(Manifest.permission.CAMERA));
        addLog("Microphone permission: "+granted(Manifest.permission.RECORD_AUDIO));
        addLog("Location permission: "+granted(Manifest.permission.ACCESS_FINE_LOCATION));
        addLog("Device posture check completed locally.");
    }
    private String granted(String p) {
        return checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED?"GRANTED":"NOT GRANTED";
    }
    private void requestOne(String permission,int code) {
        if(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED) {
            addLog(permission+": already granted. Sensor was not activated.");
        } else { addLog("Requesting Android system permission: "+permission);requestPermissions(new String[]{permission},code); }
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(results.length>0)addLog(permissions[0]+(results[0]==PackageManager.PERMISSION_GRANTED?": permission granted; sensor not activated.":": permission denied."));
    }
    private void startNetworkMonitor() {
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm==null){addLog("Connectivity service unavailable.");return;}
        if(networkCallback!=null){addLog("Network monitor is already running.");return;}
        networkCallback=new ConnectivityManager.NetworkCallback(){
            @Override public void onAvailable(Network n){runOnUiThread(()->addLog("Network became available."));}
            @Override public void onLost(Network n){runOnUiThread(()->addLog("Network was disconnected."));}
            @Override public void onCapabilitiesChanged(Network n,NetworkCapabilities c){
                runOnUiThread(()->addLog("Network type: "+(c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Mobile data":"Other")));
            }
        };
        cm.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),networkCallback);
        addLog("Live network state monitor started. No packet contents or browsing data are collected.");
    }
    private void pickApk() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/vnd.android.package-archive");i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,REQ_APK);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK||data==null)return;
        if(requestCode==REQ_APK&&data.getData()!=null)analyzeApk(data.getData());
        if(requestCode==REQ_SCREEN) {
            if(resultCode==RESULT_OK) addLog("Android screen-capture consent was granted. This build did not start capture, record, or transmit screen contents.");
            else addLog("Screen capture consent was denied or cancelled.");
        }
        if(requestCode==REQ_REPORT&&data.getData()!=null)writeReport(data.getData());
    }
    private void analyzeApk(Uri uri) {
        try {
            PackageManager pm=getPackageManager();PackageInfo info;
            if(Build.VERSION.SDK_INT>=33)info=pm.getPackageArchiveInfo(getCacheDir().getAbsolutePath()+"/not_used.apk",PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS|PackageManager.GET_SIGNING_CERTIFICATES));
            else info=pm.getPackageArchiveInfo(getCacheDir().getAbsolutePath()+"/not_used.apk",PackageManager.GET_PERMISSIONS);
            // SAF-selected APK is copied to a temporary app-private file before parsing.
            java.io.InputStream in=getContentResolver().openInputStream(uri);
            java.io.File apk=new java.io.File(getCacheDir(),"selected-test.apk");
            java.io.FileOutputStream out=new java.io.FileOutputStream(apk);
            byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))>0)out.write(buffer,0,n);in.close();out.close();
            if(Build.VERSION.SDK_INT>=33)info=pm.getPackageArchiveInfo(apk.getAbsolutePath(),PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS|PackageManager.GET_SIGNING_CERTIFICATES));
            else info=pm.getPackageArchiveInfo(apk.getAbsolutePath(),PackageManager.GET_PERMISSIONS);
            if(info==null){addLog("APK analysis failed: file is not a readable Android APK.");return;}
            addLog("APK package: "+info.packageName);
            addLog("Version: "+(Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode));
            String[] perms=info.requestedPermissions;
            addLog("Declared permissions: "+(perms==null?0:perms.length));
            if(perms!=null)for(String p:perms)addLog("APK permission: "+p);
            if(Build.VERSION.SDK_INT>=28&&info.signingInfo!=null) {
                android.content.pm.Signature[] sigs=info.signingInfo.getApkContentsSigners();
                if(sigs!=null&&sigs.length>0)addLog("Signer SHA-256: "+sha256(sigs[0].toByteArray()));
            } else addLog("Signing certificate details unavailable from this APK/parser.");
            addLog("Static manifest review only; this does not prove an APK is safe or malicious.");
            apk.delete();
        } catch(Exception e){addLog("APK analyzer error: "+e.getClass().getSimpleName()+" - "+e.getMessage());}
    }
    private String sha256(byte[] bytes)throws Exception {
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder s=new StringBuilder();
        for(byte b:digest)s.append(String.format("%02X",b));return s.toString();
    }
    private void testNotification() {
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIF);return;
        }
        showTestNotification();
    }
    private void showTestNotification() {
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("lab_test","Lab test notifications",NotificationManager.IMPORTANCE_DEFAULT));
        android.app.Notification.Builder b=Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(this,"lab_test"):new android.app.Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Reimbursement Claim test").setContentText("This is a locally generated test notification.").setAutoCancel(true);
        nm.notify(9001,b.build());addLog("Local test notification posted by this app.");
    }
    private void notificationAccessInfo() {
        addLog("Notification listener access is not enabled or read by this app.");
        new AlertDialog.Builder(this).setTitle("Notification access test")
            .setMessage("Android can show the system Notification Access settings. This app does not read other apps' notifications in this test build. You can inspect the setting without granting access.")
            .setPositiveButton("Open Android settings",(d,w)->{try{startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));}catch(Exception e){addLog("Notification settings unavailable.");}})
            .setNegativeButton("Cancel",null).show();
    }
    private void screenConsentTest() {
        new AlertDialog.Builder(this).setTitle("Screen-capture consent")
            .setMessage("Android will show its own screen-sharing consent prompt. This test only verifies the consent flow; it will not capture, record, or transmit screen contents.")
            .setPositiveButton("Continue",(d,w)->{
                MediaProjectionManager m=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(m.createScreenCaptureIntent(),REQ_SCREEN);
            }).setNegativeButton("Cancel",null).show();
    }
    private void exportReport() {
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/plain");i.putExtra(Intent.EXTRA_TITLE,"Reimbursement-Claim-Security-Test.txt");
        startActivityForResult(i,REQ_REPORT);
    }
    private void writeReport(Uri uri) {
        try {OutputStream out=getContentResolver().openOutputStream(uri);out.write(report.toString().getBytes("UTF-8"));out.close();addLog("Report exported to the location you selected.");}
        catch(Exception e){addLog("Report export failed: "+e.getMessage());}
    }
    private void runAll() {
        deviceCheck();
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo ni=cm==null?null:cm.getActiveNetworkInfo();
        addLog("Current network: "+(ni!=null&&ni.isConnected()?ni.getTypeName()+" connected":"offline"));
        addLog("Combined local posture checks completed. No personal content was accessed.");
    }
    @Override protected void onDestroy() {
        if(networkCallback!=null){try{((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).unregisterNetworkCallback(networkCallback);}catch(Exception ignored){}}
        super.onDestroy();
    }
}