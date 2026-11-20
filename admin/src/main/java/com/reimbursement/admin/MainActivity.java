package com.reimbursement.admin;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final int navy = Color.rgb(16,29,58);
    private final int blue = Color.rgb(36,87,197);
    private final int ink = Color.rgb(31,41,61);
    private final int muted = Color.rgb(105,116,138);
    private final int page = Color.rgb(244,246,251);
    private final List<Claim> claims = new ArrayList<>();
    private LinearLayout content;
    private LinearLayout logBox;

    static class Claim {
        String id, person, category, amount, date, status;
        Claim(String i,String p,String c,String a,String d,String s) {
            id=i; person=p; category=c; amount=a; date=d; status=s;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(page);
        seed();
        showLogin();
    }

    private void seed() {
        claims.clear();
        claims.add(new Claim("RC-DEMO-001","Sample Employee A","Travel","₹2,450","18 Nov 2026","Pending"));
        claims.add(new Claim("RC-DEMO-002","Sample Employee B","Medical","₹1,800","17 Nov 2026","Pending"));
        claims.add(new Claim("RC-DEMO-003","Sample Employee C","Daily Allowance","₹950","15 Nov 2026","Approved"));
        claims.add(new Claim("RC-DEMO-004","Sample Employee D","Travel","₹3,200","12 Nov 2026","Rejected"));
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private TextView text(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout row() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v,int n) { v.setPadding(n,n,n,n); }
    private TextView button(String label,int color) {
        TextView b=text(label,14,Color.WHITE,true); b.setGravity(Gravity.CENTER); b.setPadding(16,13,16,13); b.setBackground(bg(color,18)); return b;
    }
    private void base(String title,String subtitle) {
        LinearLayout root=column(); root.setBackgroundColor(page);
        LinearLayout header=column(); header.setPadding(22,24,22,22); header.setBackground(bg(navy,0));
        header.addView(text("REIMBURSEMENT CLAIM",12,Color.rgb(177,199,255),true));
        header.addView(text(title,25,Color.WHITE,true));
        TextView sub=text(subtitle,13,Color.rgb(218,226,244),false); sub.setPadding(0,5,0,0); header.addView(sub);
        root.addView(header);
        ScrollView scroll=new ScrollView(this); content=column(); content.setPadding(18,18,18,28); scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private void showLogin() {
        base("Admin workspace","Local training dashboard");
        LinearLayout card=column(); card.setPadding(20,22,20,22); card.setBackground(bg(Color.WHITE,22));
        card.addView(text("Welcome, Administrator",21,ink,true));
        TextView info=text("This is a simulated admin app. No official CAPF login, employee account, OTP or real claim data is used.",14,muted,false);
        info.setPadding(0,10,0,18); card.addView(info);
        TextView open=button("OPEN ADMIN DEMO",blue);
        open.setOnClickListener(v->showDashboard()); card.addView(open);
        TextView note=text("Demo data stays on this device session and resets when the app restarts.",12,muted,false);
        note.setPadding(0,16,0,0); card.addView(note);
        content.addView(card);
        TextView importReport=button("IMPORT SHARED TEST REPORT",navy);
        LinearLayout.LayoutParams irp=new LinearLayout.LayoutParams(-1,-2); irp.topMargin=14;
        importReport.setOnClickListener(v->pickSharedReport());
        content.addView(importReport,irp);
        content.addView(text("ADMIN ACCESS",12,blue,true));
        content.addView(text("Training-only access • No server connection",13,muted,false));
    }
    private static final int REQ_IMPORT_REPORT = 810;
    private void pickSharedReport() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/plain");
        startActivityForResult(i,REQ_IMPORT_REPORT);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=REQ_IMPORT_REPORT || resultCode!=RESULT_OK || data==null || data.getData()==null) return;
        try {
            StringBuilder report=new StringBuilder();
            BufferedReader reader=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData()),"UTF-8"));
            String line; int count=0;
            while((line=reader.readLine())!=null && count++<500) report.append(line).append("\\n");
            reader.close();
            new AlertDialog.Builder(this).setTitle("Imported shared test report")
                .setMessage(report.length()==0?"The selected report is empty.":report.toString())
                .setPositiveButton("Close",null).show();
        } catch(Exception e) {
            Toast.makeText(this,"Could not read selected report: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void showDashboard() {
        base("Admin Dashboard","Overview of sample reimbursement entries");
        LinearLayout notice=column(); notice.setPadding(15,14,15,14); notice.setBackground(bg(Color.rgb(232,240,255),16));
        notice.addView(text("DEMO MODE — NOT AN OFFICIAL PORTAL",12,blue,true));
        notice.addView(text("All names, IDs and amounts below are fictional examples.",13,ink,false)); content.addView(notice);
        int pending=0,approved=0,rejected=0;
        for(Claim c:claims){if(c.status.equals("Pending"))pending++;else if(c.status.equals("Approved"))approved++;else rejected++;}
        LinearLayout stats=row(); stats.setPadding(0,14,0,14);
        stats.addView(stat("TOTAL",String.valueOf(claims.size())),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(stat("PENDING",String.valueOf(pending)),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(stat("APPROVED",String.valueOf(approved)),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(stat("REJECTED",String.valueOf(rejected)),new LinearLayout.LayoutParams(0,-2,1));
        content.addView(stats);
        LinearLayout heading=row(); heading.addView(text("Test claim queue",19,ink,true));
        TextView refresh=button("RESET",blue); refresh.setTextSize(11); refresh.setOnClickListener(v->{seed();showDashboard();});
        heading.addView(refresh,new LinearLayout.LayoutParams(-2,-2)); content.addView(heading);
        for(Claim c:claims) content.addView(claimCard(c));
        TextView logTitle=text("Admin activity log",18,ink,true); logTitle.setPadding(0,20,0,8); content.addView(logTitle);
        logBox=column(); logBox.setPadding(14,12,14,12); logBox.setBackground(bg(Color.WHITE,16));
        logBox.addView(text("No actions in this session yet.",13,muted,false)); content.addView(logBox);
        TextView export=button("SHARE DEMO SUMMARY",navy); export.setOnClickListener(v->shareSummary()); export.setPadding(12,15,12,15);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2); ep.topMargin=16; content.addView(export,ep);
    }
    private View stat(String label,String value) {
        LinearLayout box=column(); box.setPadding(10,13,6,13); box.setBackground(bg(Color.WHITE,15));
        box.addView(text(value,22,blue,true)); box.addView(text(label,9,muted,true)); return box;
    }
    private View claimCard(Claim c) {
        LinearLayout card=column(); card.setPadding(16,15,16,15); card.setBackground(bg(Color.WHITE,18));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.bottomMargin=12; card.setLayoutParams(cp);
        LinearLayout top=row(); top.addView(text(c.id,12,blue,true),new LinearLayout.LayoutParams(0,-2,1));
        int sc=c.status.equals("Approved")?Color.rgb(20,130,83):c.status.equals("Rejected")?Color.rgb(190,55,55):Color.rgb(174,112,20);
        TextView status=text(c.status,11,sc,true); status.setPadding(9,5,9,5); status.setBackground(bg(Color.rgb(244,246,250),14)); top.addView(status);
        card.addView(top);
        TextView person=text(c.person,17,ink,true); person.setPadding(0,9,0,2); card.addView(person);
        card.addView(text(c.category+"  •  "+c.date,13,muted,false));
        TextView amount=text(c.amount,20,ink,true); amount.setPadding(0,10,0,10); card.addView(amount);
        LinearLayout actions=row();
        TextView approve=button("APPROVE",Color.rgb(20,130,83));
        TextView reject=button("REJECT",Color.rgb(190,55,55));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,-2,1); ap.rightMargin=8;
        actions.addView(approve,ap); actions.addView(reject,new LinearLayout.LayoutParams(0,-2,1));
        if(!c.status.equals("Pending")) { approve.setAlpha(.45f); reject.setAlpha(.45f); }
        approve.setOnClickListener(v->{if(c.status.equals("Pending")){c.status="Approved";addLog("Approved "+c.id);showDashboard();}else Toast.makeText(this,"Already processed in demo",Toast.LENGTH_SHORT).show();});
        reject.setOnClickListener(v->{if(c.status.equals("Pending")){c.status="Rejected";addLog("Rejected "+c.id);showDashboard();}else Toast.makeText(this,"Already processed in demo",Toast.LENGTH_SHORT).show();});
        card.addView(actions);
        return card;
    }
    private final List<String> logs=new ArrayList<>();
    private void addLog(String s){logs.add(0,s);}
    private void shareSummary() {
        StringBuilder b=new StringBuilder("Reimbursement Claim Admin — DEMO SUMMARY\n");
        for(Claim c:claims)b.append(c.id).append(" | ").append(c.person).append(" | ").append(c.amount).append(" | ").append(c.status).append("\n");
        b.append("\nTraining data only. Not an official CAPF record.");
        android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain"); i.putExtra(android.content.Intent.EXTRA_TEXT,b.toString());
        startActivity(android.content.Intent.createChooser(i,"Share demo summary"));
    }
}
