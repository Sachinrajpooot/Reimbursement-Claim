package com.reimbursement.claim;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import okhttp3.*;

public class PairingActivity extends Activity {
    private static final String SERVER = "wss://reimbursement-claim-signaling.onrender.com";
    private OkHttpClient http;
    private WebSocket socket;
    private TextView status, codeView;
    private boolean stopped=false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(28,40,28,28); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setBackgroundColor(Color.rgb(244,247,252));
        TextView title=new TextView(this); title.setText("Secure device support"); title.setTextSize(25); title.setTextColor(Color.rgb(20,40,75)); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView info=new TextView(this); info.setText("Pair only with an administrator you trust. Screen sharing is not active in this build."); info.setTextSize(15); info.setTextColor(Color.DKGRAY); info.setPadding(0,20,0,20); root.addView(info);
        codeView=new TextView(this); codeView.setText("------"); codeView.setTextSize(36); codeView.setTextColor(Color.rgb(35,85,190)); codeView.setGravity(Gravity.CENTER); root.addView(codeView);
        status=new TextView(this); status.setText("Connecting to secure server…"); status.setTextSize(14); status.setTextColor(Color.DKGRAY); status.setPadding(0,20,0,20); root.addView(status);
        Button stop=new Button(this); stop.setText("STOP / DISCONNECT"); root.addView(stop); stop.setOnClickListener(v->{stopped=true;if(socket!=null)socket.send("{\"type\":\"end-session\"}");if(socket!=null)socket.close(1000,"Stopped by device owner");status.setText("Disconnected by device owner.");});
        setContentView(root);
        http=new OkHttpClient();
        Request req=new Request.Builder().url(SERVER).build();
        socket=http.newWebSocket(req,new WebSocketListener(){
            @Override public void onOpen(WebSocket ws, Response response){socket=ws;ws.send("{\"type\":\"create-session\"}");}
            @Override public void onMessage(WebSocket ws,String text){runOnUiThread(()->{
                if(text.contains("\"session-created\"")){try{org.json.JSONObject j=new org.json.JSONObject(text);codeView.setText(j.optString("code","------"));status.setText("Pairing code ready. Share it only with the intended admin.");}catch(Exception e){status.setText("Could not read server response.");}}
                else if(text.contains("\"peer-joined\""))status.setText("Admin paired. Screen sharing has NOT started; this app does not transmit your screen.");
                else if(text.contains("\"error\""))status.setText("Server: "+text);
                else if(text.contains("\"session-ended\""))status.setText("Session ended.");
            });}
            @Override public void onFailure(WebSocket ws,Throwable t,Response r){runOnUiThread(()->status.setText("Connection failed: "+t.getMessage()));}
            @Override public void onClosed(WebSocket ws,int c,String reason){runOnUiThread(()->{if(!stopped)status.setText("Connection closed.");});}
        });
    }
    @Override protected void onDestroy(){if(socket!=null)socket.close(1000,"Activity closed");if(http!=null)http.dispatcher().executorService().shutdown();super.onDestroy();}
}