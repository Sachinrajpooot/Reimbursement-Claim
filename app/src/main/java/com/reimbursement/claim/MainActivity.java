package com.reimbursement.claim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(1), strokeColor);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void addLabel(LinearLayout parent, String value) {
        TextView label = text(value, 14, Color.rgb(52, 64, 84), true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(18);
        p.bottomMargin = dp(8);
        parent.addView(label, p);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(243, 246, 252));
        getWindow().setNavigationBarColor(Color.rgb(243, 246, 252));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(243, 246, 252));

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER_HORIZONTAL);
        outer.setPadding(dp(22), dp(38), dp(22), dp(24));
        scroll.addView(outer);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(28), dp(22), dp(20));
        card.setBackground(shape(Color.WHITE, 22, Color.rgb(230, 235, 243)));
        card.setElevation(dp(3));
        outer.addView(card, new LinearLayout.LayoutParams(-1, -2));

        TextView heading = text("Open Security Test Lab", 25, Color.rgb(23, 43, 77), true);
        heading.setGravity(Gravity.CENTER);
        card.addView(heading);
        TextView sub = text("Security test lab • Use test details only", 14, Color.rgb(113, 128, 150), false);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(-1, -2);
        subP.topMargin = dp(8);
        card.addView(sub, subP);

        addLabel(card, "Force Type");
        Spinner force = new Spinner(this);
        String[] forces = {"Select Force", "CRPF", "BSF", "CISF", "ITBP", "SSB", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, forces);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        force.setAdapter(adapter);
        force.setPadding(dp(12), dp(5), dp(12), dp(5));
        force.setBackground(shape(Color.WHITE, 12, Color.rgb(218, 225, 235)));
        card.addView(force, new LinearLayout.LayoutParams(-1, dp(52)));

        addLabel(card, "Login ID");
        EditText loginId = new EditText(this);
        loginId.setSingleLine(true);
        loginId.setTextSize(15);
        loginId.setHint("Enter a test Login ID");
        loginId.setPadding(dp(14), 0, dp(14), 0);
        loginId.setBackground(shape(Color.WHITE, 12, Color.rgb(218, 225, 235)));
        card.addView(loginId, new LinearLayout.LayoutParams(-1, dp(52)));

        Button login = new Button(this);
        login.setText("OPEN TEST LAB");
        login.setTextColor(Color.WHITE);
        login.setTextSize(14);
        login.setAllCaps(false);
        login.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        login.setBackground(shape(Color.rgb(49, 92, 186), 12, 0));
        LinearLayout.LayoutParams buttonP = new LinearLayout.LayoutParams(-1, dp(54));
        buttonP.topMargin = dp(24);
        card.addView(login, buttonP);

        TextView privacy = text("Privacy Policy", 14, Color.rgb(49, 92, 186), true);
        privacy.setGravity(Gravity.CENTER);
        privacy.setPadding(0, dp(22), 0, dp(4));
        privacy.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("This is a controlled security-testing application. Use a test Login ID only. This version does not send entered text to a server, collect OTPs, secretly record sensors, or transmit personal data. Sensitive permissions are requested only when you tap their individual test.")
            .setPositiveButton("Close", null).show());
        card.addView(privacy);

        login.setOnClickListener(v -> {
            if (force.getSelectedItemPosition() == 0) {
                new AlertDialog.Builder(this).setMessage("Please select a Force Type.").setPositiveButton("OK", null).show();
            } else if (loginId.getText().toString().trim().isEmpty()) {
                loginId.setError("Enter a test Login ID");
            } else {
                startActivity(new Intent(MainActivity.this, LabActivity.class));
            }
        });

        TextView footer = text("Reimbursement Claim", 13, Color.rgb(113, 128, 150), true);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerP = new LinearLayout.LayoutParams(-1, -2);
        footerP.topMargin = dp(18);
        outer.addView(footer, footerP);
        setContentView(scroll);
    }
}