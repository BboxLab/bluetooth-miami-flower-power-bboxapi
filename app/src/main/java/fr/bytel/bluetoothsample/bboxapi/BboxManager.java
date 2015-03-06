package fr.bytel.bluetoothsample.bboxapi;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationType;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.WebSocket;
import fr.bytel.bluetoothsample.BluetoothActivity;
import fr.bytel.bluetoothsample.R;

/**
 * Bbox API manager : manage security token authentication and session id
 *
 * @author Bertrand Martel Bouygues Telecom on 04/03/15.
 */
public class BboxManager {

    /**
     * Notification Manager object used to manage all listening/subscribe from/to notifications
     */
    private NotificationManager notificationManager = null;

    private String LOG_TAG=this.getClass().getName();

    /**
     * Bbox API uri used to subscribe to room
     */
    private final String NOTIFICATION_ROOM_NAME="api.bbox.lan/v0/notification";

    /**
     * Room name designed to be created and used to send notifications
     */
    private final String ROOM_NAME = "Message/Bluetooth";

    /**
     * session id
     */
    private String sessionId = "";

    /**
     * current app context
     */
    private Context context = null;

    /**
     * local ip (first IP)
     */
    private String localIP="";

    /**
     * Bbox object used to authenticate
     */
    private Bbox bbox = null;


    /**
     * callback called when authentication process result is available
     */
    private IAuthCallback authenticationCall = new IAuthCallback() {
        @Override
        public void onAuthResult(int code, String msg) {
            Log.d(LOG_TAG, "onAuthResult msg=" + msg + " code=" + code);
            // We have to get our AppID in order to initiate a websocket connection.




            bbox.getApplicationsManager().getMyAppId("Remote_Controller", new ApplicationsManager.CallbackAppId() {
                @Override
                public void onResult(int statusCode, final String appId) {

                    sessionId=bbox.getSessionId();

                    SubscribeRoomAsyncTask task = new SubscribeRoomAsyncTask(localIP,BboxManager.this,context,NOTIFICATION_ROOM_NAME,ROOM_NAME,appId);
                    task.execute();

                    System.out.println("session id : " + bbox.getSessionId());
                    System.out.println("statusCode"  + statusCode);

                    // Now we have our AppID, we can therefor instantiate a NotificationManager with the WebSocket implementation.
                    notificationManager = WebSocket.getInstance(appId, bbox);
                    Log.i(this.getClass().getName(),"listening to websocket...");
                    //notificationManager.listen(websoscketConnectionListener);


                    notificationManager.subscribe(NotificationType.APPLICATION, new NotificationManager.CallbackSubscribed() {
                        @Override
                        public void onResult(int statusCode) {

                            // We can check if the subscription is a success with the http return code.
                            Log.d(LOG_TAG, "status subscribe:" + statusCode);

                            // We also subscribe to Applications and Media, but we do not provide a callback this time. We don't want to wait for the return.
                            notificationManager.subscribe(NotificationType.APPLICATION, null);
                            notificationManager.subscribe(NotificationType.MEDIA, null);

                            //SubscribeRoomAsyncTask task = new SubscribeRoomAsyncTask(localIP,BboxManager.this,context,NOTIFICATION_ROOM_NAME,ROOM_NAME,appId);


                            // We add a AllNotificationsListener to Log all the notifications we receive.
                            notificationManager.addAllNotificationsListener(new NotificationManager.Listener() {
                                @Override
                                public void onNotification(JSONObject jsonObject) {
                                    // We here add the received notification to a list, to be able to print it in our UI.
                                    //notificationsList.add(jsonObject.toString());
                                    Log.d(LOG_TAG, jsonObject.toString());
                                }
                            });

                            // Here we add a MessageListener. Inside we will only receive Message notifications.
                            // Message notification will still appear in the AllNotificationListener.
                            notificationManager.addMessageListener(new NotificationManager.Listener() {
                                @Override
                                public void onNotification(JSONObject jsonObject) {
                                    Log.d(LOG_TAG, jsonObject.toString());
                                }
                            });

                            // Same here with a ApplicationListener.
                            notificationManager.addApplicationListener(new NotificationManager.Listener() {
                                @Override
                                public void onNotification(JSONObject jsonObject) {
                                    Log.d(LOG_TAG, jsonObject.toString());
                                }
                            });

                            // Once we have set our listeners, we can start listening for notifications.
                            notificationManager.listen(new NotificationManager.CallbackConnected() {
                                @Override
                                public void onConnect() {
                                    Log.i(LOG_TAG, "WebSockets connected");

                                    // As soon as we are connected with the NotificationManager, we send a message to ourself.
                                    notificationManager.sendMessage(notificationManager.getChannelId(), "hello myself");
                                }
                            });
                        }
                    });

                }
            });
        }
    };

    /**
     * Build BboxManager object with app context and local IP address
     *
     * @param context
     * @param localIP
     */
    public BboxManager(Context context,String localIP)
    {
        this.context=context;
        this.localIP=localIP;
        //authenticate BBOX with cloud API
        bbox = new Bbox(this.localIP);
        bbox.authenticate(this.context.getString(R.string.app_id_value),this.context.getString(R.string.app_secret_value),authenticationCall);
    }

    /**
     * Build one notification element and send it to room name
     *
     * @param notificationText
     *      notification field text
     * @param value
     *      measured value
     * @param unit
     *      unit used
     */
    public void buildBleNotification(String notificationText, double value,String unit) {
        JSONObject object = new JSONObject();
        try {

            object.put(notificationText,value);
            object.put("unit",unit);
            object.put("packageName", BluetoothActivity.PACKAGE_NAME);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (notificationManager!=null) {

            try {
                notificationManager.sendRoomMessage(ROOM_NAME, object);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            //NotificationAsyncTask newTask = new NotificationAsyncTask(localIP, this, context, NOTIFICATION_ROOM_NAME, object);
            //newTask.execute();
        }
        else
        {
            Log.i(LOG_TAG,"notification manager is null");
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
