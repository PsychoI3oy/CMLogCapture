package org.cyanogenmod.bugreportgrabber;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import android.widget.TextView;

public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        String jiraResponse = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String jiraResponseText = (getString(R.string.thanks) +" <a href=\"http://jira.cyanogenmod.org/browse/" + jiraResponse + "\">here</a>");
        TextView textView = new TextView(this);
        textView.setTextSize(24);
        textView.setGravity(0);
        textView.setText(Html.fromHtml(jiraResponseText));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        setContentView(textView);
    }
}
