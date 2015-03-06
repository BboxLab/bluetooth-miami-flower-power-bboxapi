package fr.bytel.bluetoothsample.bboxapi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import fr.bytel.bluetoothsample.R;

/**
 * @author Bertrand Martel Bouygues Telecom on 04/03/15.
 */
public class SubscribeRoomAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private String TAG = this.getClass().getName();

    private String ip="";

    private BboxManager manager=null;

    private Context context = null;

    private String uri = "";

    private String roomName = null;

    private String interfaceAppId="";

    public SubscribeRoomAsyncTask(String ip,BboxManager manager,Context context,String uri,String roomName,String interfaceAppId) {
        this.ip = ip;
        this.manager=manager;
        this.context=context;
        this.uri=uri;
        this.roomName=roomName;
        this.interfaceAppId=interfaceAppId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

/*
        JSONObject object = new JSONObject();
        try {
            object.put("appId", interfaceAppId);

            JSONArray resources = new JSONArray();

            JSONObject notificationObject = new JSONObject();
            notificationObject.put("resourceId", roomName);
            resources.put(notificationObject);

            object.put("resources", resources);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        buildPost(object.toString(),uri,manager.getSessionId());
*/

        // Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient();
        //HttpPost httpPost = new HttpPost("http://" + ip + ":8080/api.bbox.lan/v0/security/sessionId");
        System.out.println("http://" + ip + ":8080/" + uri);

        HttpPost httpPost = new HttpPost("http://" + ip  +":8080/" + uri);

        try {
            JSONObject object = new JSONObject();
            try {
                object.put("appId", interfaceAppId);

                JSONArray resources = new JSONArray();

                JSONObject notificationObject = new JSONObject();
                notificationObject.put("resourceId", roomName);
                resources.put(notificationObject);

                object.put("resources", resources);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

                //jsonObjectToken.put("token", token);
                httpPost.setEntity(new ByteArrayEntity(object.toString().getBytes("UTF8")));

                System.out.println(manager.getSessionId());

                httpPost.setHeader("x-sessionid",manager.getSessionId());

                // Execute HTTP Post Request
                // adb shell am startservice -a "fr.bouyguestelecom.bboxapi.StartService" --user 0
                HttpResponse response = httpClient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                Log.d(TAG, "statusCode: " + statusCode);
                if (statusCode == 204) {
                    Log.i(this.getClass().getName(),"204 received");
                    return true;
                } else {
                    Log.e(TAG, "Failed to get sessionId");
                    return false;
                }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i(this.getClass().getName(), "Notification has been sent");
    }

}
