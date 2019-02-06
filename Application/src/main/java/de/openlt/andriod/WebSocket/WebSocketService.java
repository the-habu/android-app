package de.openlt.andriod.WebSocket;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class WebSocketService extends Service {
    private final static String TAG = WebSocketService.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_NEW_MESAGE = "com.example.myfirstapp.action.ACTION_NEW_MESAGE ";
    private static final String PARAM_MESAGE = "com.example.myfirstapp.param.ACTION_NEW_MESAGE ";


    private OkHttpClient client;
    private IBinder mBinder;

    @Override
    public void onCreate(){
        Log.i(TAG, "onCreate");
        client = new OkHttpClient();
        //Request request = new Request.Builder().url("ws://192.168.0.11").build();
        Request request = new Request.Builder().url("ws://192.168.1.106").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(TAG, "onUnbind");

        client.dispatcher().executorService().shutdown();
        return super.onUnbind(intent);
    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    //private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
    //    throw new UnsupportedOperationException("Not yet implemented");
    //}

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    //private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
    //    throw new UnsupportedOperationException("Not yet implemented");
    //}

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i(TAG+"EchoWebSocketListener", "onOpen");
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            webSocket.send(settings.getString("username","n/a"));
            webSocket.send("What's up ?");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.i(TAG+"EchoWebSocketListener", "onMessage");
            output("Receiving : " + text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.i(TAG+"EchoWebSocketListener", "onClosing");
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
            output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.i(TAG+"EchoWebSocketListener", "onFail");
            output("Error : " + t.getMessage());
        }
    }

    public  void  input(final String  txt){

    }


    private void output(final String txt) {
        Log.i(TAG, "output: " + txt);

        Intent intent = new Intent(ACTION_NEW_MESAGE);
        intent.putExtra(PARAM_MESAGE, txt);
        sendBroadcast(intent);
    }

}
