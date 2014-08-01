package org.cyanogenmod.bugreportgrabber;


import java.util.ArrayList;
import java.io.File;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;


public class MainActivity extends Activity {
    private final static String uNpW =     "QnVnQ29sbGVjdG9yOldlTE9WRWJ1Z3Mh"; // <--- BugCollector //"V2lraUJ1Z0NvbGxlY3RvcjpjeW5nbj10ZWFtZG91Y2hl"; <-- wikibot
    private final static String apiURL = "https://jira.cyanogenmod.org/rest/api/2/issue/";
    private final static String projectName = "11400"; // 11102 = WIKI 11400 = bugdump
    private final static String issueType = "1"; // 4 = improvement   1 = bug?
    
    
    
    public final static String EXTRA_MESSAGE = "org.cyanogenmod.bugreportgrabber.MESSAGE";
    private Uri reportURI = Uri.parse("content://com.android.shell/bugreports/bugreport-2014-07-24-10-40-37.txt"); // initializing for testing
    private Uri sshotURI;


    private JSONObject inputJSON = new JSONObject();
    private JSONObject outputJSON;    
    
    private Button submitButton;
    
    

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
        }
    }


    private void handleBugReport(Intent intent) {
        
        ArrayList<Uri> attachments = new ArrayList<Uri>();
        attachments = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        
        
        for (Uri u : attachments){
            if ( u.toString().contains("txt")){
                reportURI = u;
            }else if (u.toString().contains("png")){
                sshotURI = u;
            }else{
//something went wrong
            }
        }
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
            JSONObject fields = new JSONObject();
            JSONObject project = new JSONObject();
            JSONObject issuetype = new JSONObject();
            
            
            try {
            project.put("id", projectName);
            issuetype.put("id", issueType);
            
            fields.put("project",project);
            fields.put("summary", summary);
            fields.put("description", description);
            fields.put("issuetype", issuetype);
            
            inputJSON.put("fields", fields);
            } catch(JSONException e){
                Log.e("bugreportgrabber", "JSONexception: " + e.getMessage());
                //pop error message for JSON creation
                GenericErrorDialog uhoh = new GenericErrorDialog();
                uhoh.show(getFragmentManager(), "json failed");
                
                //re-enable the button so they can try again
                submitButton.setEnabled(true);
                
            }
            
            Context context = getApplicationContext();
            CharSequence text = "Uploading bugreport...";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            new CallAPI().execute(inputJSON);
        } else {
            //error message for blank text
            NoTextDialog nope = new NoTextDialog();
            nope.show(getFragmentManager(), "notext");
            
            //re-enable the button so they can put in text and hit button again
            submitButton.setEnabled(true);
         
        }

    }
    
    private class CallAPI extends AsyncTask<JSONObject, Void, String> {
        String responseString = "";
        
        @Override
        protected String doInBackground(JSONObject...params){
            
            
            //create the issue report
            //        Log.d("brg", "creating url");
            try {
                URI url = new URI(apiURL);
                
                DefaultHttpClient htClient = new DefaultHttpClient();             
                HttpPost httpost = new HttpPost(url);
                //turn the JSONObject being passed into a stringentity for http consumption
                StringEntity se = new StringEntity(params[0].toString());
                // Log.d("brg", params[0].toString());
                httpost.setEntity(se);
                httpost.setHeader("Accept","application/json");
                httpost.setHeader("Authorization","Basic " + uNpW);
                httpost.setHeader("Content-Type","application/json");
                //Log.d("brg", "header set");
                HttpResponse response = htClient.execute(httpost);
                //Log.d("brg", "connection executed");
                HttpEntity entity = response.getEntity();
                responseString = EntityUtils.toString(entity);
                Log.d("brg", "response: " + responseString );
                
            } catch (Exception e) {
                  Log.e("bugreportgrabber", "URLexception: " + e);
                  GenericErrorDialog uhoh = new GenericErrorDialog();
                  uhoh.show(getFragmentManager(), "http failed");
                  
                  //re-enable the button so they can try again
                  submitButton.setEnabled(true);
                  return e.getMessage();
//pop error message
            }
            // Log.d("brg", "got past url stuff");
                
            //issue hopefully created, let's get the ID so we can attach to it (and pass that ID to the results activity)
            
            //Log.d("brg", "uri passed " + reportURI.toString());
            
            String jiraResponse = responseString;
           
            String jiraBugID = "";
            try {
                outputJSON = new JSONObject(jiraResponse);
                jiraBugID = (String)  outputJSON.get("key");
            } catch (JSONException e) {
                e.printStackTrace();
                GenericErrorDialog uhoh = new GenericErrorDialog();
                uhoh.show(getFragmentManager(), "response bad");
                
                //re-enable the button so they can try again
                submitButton.setEnabled(true); 
// pop error message for bad response from server
                return e.getMessage();
            }
            
            
            
            
//pop waiting dialog "Uploading....."
            
            Log.d("brg", "creating attachment on issue " + jiraBugID);
            //now we attach the file
            if(!jiraBugID.isEmpty()){
                try {
                    URI url2 = new URI(apiURL + jiraBugID + "/attachments");
                    //Log.d("brg", "new URI: " + url2.toString());
                    DefaultHttpClient uplClient = new DefaultHttpClient();             
                    //Log.d("brg", "client created ");
                    HttpPost httpostUpl = new HttpPost(url2);
                    httpostUpl.setHeader("Authorization","Basic " + uNpW);
                    //httpostUpl.setHeader("Content-Type","multipart/form-data");
                    httpostUpl.setHeader("X-Atlassian-Token","nocheck");
                    
                    //Log.d("brg", "headers set ");
                    
                    File bugreportFile = new File("/data" + reportURI.getPath());
                    //long fileSize = bugreportFile.length();
                    
                    //FileEntity bugreportUploadEntity = new FileEntity(bugreportFile, "text/ascii"); 
                    //httpostUpl.setHeader("Content-Size", Objects.toString(fileSize));
                    Log.d("brg", "File object created");
                    MultipartEntityBuilder bugreportUploadEntityB = MultipartEntityBuilder.create();
                    //Log.d("brg", "entity created");
                    bugreportUploadEntityB.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    //Log.d("brg", "mode set");
                    bugreportUploadEntityB.addPart("file", new FileBody(bugreportFile));
                    //Log.d("brg", "file added");
                    httpostUpl.setEntity(bugreportUploadEntityB.build());
                    //Log.d("brg", "entity created and attached ");
                    HttpResponse uplResponse = uplClient.execute(httpostUpl);
                    Log.d("brg", "executed ");
                    HttpEntity entityResponse = uplResponse.getEntity();
                    responseString = EntityUtils.toString(entityResponse);
                    Log.d("brg", "response " + responseString);
                } catch (Exception e) {
                    Log.e("bugreportgrabber", "file upload exception: " + e);
                    //pop error message for file upload"
                    GenericErrorDialog uhoh = new GenericErrorDialog();
                    uhoh.show(getFragmentManager(), "upload failed");
                    
                    //re-enable the button so they can try again
                    submitButton.setEnabled(true);

                }
            
            } else { 
                // pop error message for bad response from server 
                GenericErrorDialog uhoh = new GenericErrorDialog();
                uhoh.show(getFragmentManager(), "JIRA failed");
                
                //re-enable the button so they can try again
                submitButton.setEnabled(true);
               
            }
//pop toast for file uploaded successfully? 
            //pass the bug ID to the results Activity
            return jiraBugID; //output;
        }
        
        protected void onPostExecute(String result){
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            
            intent.putExtra(EXTRA_MESSAGE, result);
            
            startActivity(intent);
                        
        }
    } // end CallApi
    
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
    class GenericErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.somethingWentWrong)
                .setNegativeButton(R.string.OK, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface arg0, int arg1) {
                        // nothing to do here.... unless we want to pop a browser window to jira.cm.org? 
                        
                    }
            });
            return builder.create();
            
        }
    }

    
    
    
    
    
    
    
    
    
}
