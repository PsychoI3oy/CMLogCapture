package org.cyanogenmod.bugreportgrabber;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


public class BRGService extends IntentService {
    
    private final static String projectName = "11400"; // 11102 = WIKI 11400 = bugdump
    private final static String issueType = "1"; // 4 = improvement   1 = bug?
    String bugID = "";
    private final static String uNpW =     "QnVnQ29sbGVjdG9yOldlTE9WRWJ1Z3Mh"; // <--- BugCollector 
    //"V2lraUJ1Z0NvbGxlY3RvcjpjeW5nbj10ZWFtZG91Y2hl"; <-- wikibot
    private final static String apiURL = "https://jira.cyanogenmod.org/rest/api/2/issue/";

    public final static String EXTRA_MESSAGE = "org.cyanogenmod.bugreportgrabber.MESSAGE";
    private Uri reportURI; // initializing for testing


    private JSONObject inputJSON = new JSONObject();
    private JSONObject outputJSON;    

    private int notifID = 546924;
    
    
    public BRGService() {
        super("BRGService");
        
    }
    
    @Override
    protected void onHandleIntent(Intent arg0) {
        Intent intent = arg0;
        
        ArrayList<Uri> attachments = new ArrayList<Uri>();
        attachments = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        
        
        for (Uri u : attachments){
            if ( u.toString().contains("txt")){
                reportURI = u;
            }
        }
        
        String summary = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String description = intent.getStringExtra(Intent.EXTRA_TEXT);
        
        
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
            notifyUploadFailed("There was a problem creating the issue");
            
        }
        
        
        
        
        notifyOfUpload();
        
        new CallAPI().execute(inputJSON);
        
        
    }
        
        private void notifyOfUpload() {
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("CyanogenMod Bug Report Grabber")
                    .setContentText("Creating issue and uploading report...");
             NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setProgress(0, 0, true);
            mNotificationManager.notify(notifID, mBuilder.build());
            
            
        }
              
        
        private void notifyUploadFinished(String issueNumber) {
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("CyanogenMod Bug Report Grabber")
                    .setContentText("Thank you for your submission.");
            NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(notifID, mBuilder.build());
            
            
        }
        private void notifyUploadFailed(String reason) {
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("CyanogenMod Bug Report Grabber")
                    .setContentText("Upload failed: " + reason );
            NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(notifID, mBuilder.build());
            
            
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
                      notifyUploadFailed("problem connecting to the server");
                      return e.getMessage();
                }
        
                    
                //issue hopefully created, let's get the ID so we can attach to it (and pass that ID to the results activity)
                
                String jiraResponse = responseString;
               
                String jiraBugID = "";
                try {
                    outputJSON = new JSONObject(jiraResponse);
                    jiraBugID = (String)  outputJSON.get("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                    notifyUploadFailed("Bad response from server");                    
                    //re-enable the button so they can try again
                    return e.getMessage();
                }
                
                
                
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
                        MultipartEntity bugreportUploadEntity = new MultipartEntity();
                        Log.d("brg", "entity created");
                        Log.d("brg", "mode set");
                        bugreportUploadEntity.addPart("file", new FileBody(bugreportFile));
                        Log.d("brg", "file added");
                        httpostUpl.setEntity(bugreportUploadEntity);
                        Log.d("brg", "entity created and attached ");
                        HttpResponse uplResponse = uplClient.execute(httpostUpl);
                        Log.d("brg", "executed ");
                        HttpEntity entityResponse = uplResponse.getEntity();
                        responseString = EntityUtils.toString(entityResponse);
                        Log.d("brg", "response " + responseString);
                    } catch (Exception e) {
                        Log.e("bugreportgrabber", "file upload exception: " + e);
                        //pop error message for file upload"
                        
                        notifyUploadFailed("The file failed to upload");
                    }
                
                } else { 
                    // pop error message for bad response from server 
                    notifyUploadFailed("Bad response from server");
                    
                }
                
                return jiraBugID; //output;
            }
            
            protected void onPostExecute(String result){
                stopForeground(true);
                notifyUploadFinished(result);
                stopSelf();            
            }
        } // end CallApi
       
}
