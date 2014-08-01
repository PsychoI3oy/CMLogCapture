package org.cyanogenmod.bugreportgrabber;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.net.Uri;


public class MainActivity extends Activity {
   
  
    public final static String EXTRA_MESSAGE = "org.cyanogenmod.bugreportgrabber.MESSAGE";
    private Uri reportURI = Uri.parse("content://com.android.shell/bugreports/bugreport-2014-07-24-10-40-37.txt"); // initializing for testing
    // private Uri sshotURI;
    private Button submitButton;
    ArrayList<Uri> attachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null){
            if ("application/vnd.android.bugreport".equals(type)){
                handleBugReport(intent);
            }
        }else{
            attachments = new ArrayList<Uri>();
            attachments.add(reportURI);    
        }
    }


    private void handleBugReport(Intent intent) {
        attachments = new ArrayList<Uri>();
        attachments = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void sendBug(View view){ 
        // disable send button to avoid doubleposts        
        submitButton = (Button) findViewById(R.id.button_submit);
        submitButton.setEnabled(false);

        // grab text entered
        EditText summaryEditText = (EditText) findViewById(R.id.summary);
        EditText descriptionEditText = (EditText) findViewById(R.id.description);
        
          
        
        String summary = summaryEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        // description = description + "     " + reportURI.toString();
               
        
        
        if (summary != null && description != null && !summary.isEmpty() && !description.isEmpty()){
            Intent intent = new Intent(this, BRGService.class);
            intent.putExtra(Intent.EXTRA_SUBJECT, summary);
            intent.putExtra(Intent.EXTRA_TEXT, description);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
            startService(intent);
            
            
            //make the screen go away
            finish();
        } else {
            //error message for blank text
            NoTextDialog nope = new NoTextDialog();
            nope.show(getFragmentManager(), "notext");
            
            //re-enable the button so they can put in text and hit button again
            submitButton.setEnabled(true);
         
        }

    }

    
    class NoTextDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.noText)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface arg0, int arg1) {
                        // nothing to do here. 
                        
                    }
            });
            return builder.create();
        }
    }
    
}
