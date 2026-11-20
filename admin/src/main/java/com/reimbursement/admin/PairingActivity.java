package com.reimbursement.admin;

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
    private TextView status;
    private EditText code;
    private Button disconnect;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(26,38,26,26);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setBackgroundColor(Color.rgb(244,247,252));
        TextView title=new TextView(this);title.setText("Reimbursement Claim Admin");title.setTextSize(24);title.setTextColor(Color.rgb(20,40,75));title.setGravity(Gravity.CENTER);root.addView(title);
        TextView info=new TextView(this);info.setText("Enter the six-digit code shown on the client phone. The client owner must approve any screen-sharing prompt separately.");info.setTextSize(15);info.setTextColor(Color.DKGRAY);info.setPadding(0,20,0,20);root.addView(info);
        code=new EditText(this);code.setHint("6-digit pairing code");code.setInputType(2);code.setGravity(Gravity.CENTER);code.setTextSize(24);root.addView(code,new LinearLayout.LayoutParams(-1,150));
        Button connect=new Button(this);connect.setText("CONNECT");root.addView(connect);
        disconnect=new Button(this);disconnect.setText("END SESSION");disconnect.setEnabled(false);root.addView(disconnect);
        status=new TextView(this);status.setText("Not connected");status.setTextColor(Color.DKGRAY);status.setPadding(0,22,0,0);root.addView(status);
        connect.setOnClickListener(v->join());
        disconnect.setOnClickListener(v->{ if(socket!=null){socket.send("{\"type\":\"end-session\"}");socket.close(1000,"Ended by admin");} status.setText("Session ended by admin.");disconnect.setEnabled(false); });
        setContentView(root);
    }
    private void join(){
        String pin=code.getText().toString().trim();
        if(!pin.matches("\\d{6}")){code.setError("Enter all 6 digits");return;}
        status.setText("Connecting securely…");
        http=new OkHttpClient();Request req=new Request.Builder().url(SERVER).build();
        socket=http.newWebSocket(req,new WebSocketListener(){
            @Override public void onOpen(WebSocket ws,Response response){socket=ws;ws.send("{\"type\":\"join-session\",\"code\":\""+pin+"\"}");}
            @Override public void onMessage(WebSocket ws,String text){runOnUiThread(()->{
                if(text.contains("\"paired\"")){status.setText("Paired successfully. Live screen viewing is not implemented yet.");disconnect.setEnabled(true);}
                else if(text.contains("\"error\""))status.setText("Server: "+text);
                else if(text.contains("\"session-ended\""))status.setText("Client ended the session.");
            });}
            @Override public void onFailure(WebSocket ws,Throwable t,Response r){runOnUiThread(()->status.setText("Connection failed: "+t.getMessage()));}
            @Override public void onClosed(WebSocket ws,int c,String reason){runOnUiThread(()->{status.setText("Disconnected.");disconnect.setEnabled(false);});}
        });
    }
    @Override protected void onDestroy(){if(socket!=null)socket.close(1000,"Admin closed");if(http!=null)http.dispatcher().executorService().shutdown();super.onDestroy();}
}