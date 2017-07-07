package com.blogspot.justsimpleinfo.wifidirectchat;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Lauro-PC on 7/2/2017.
 */

public class ClientChatService extends Service {

    private static final int SOCKET_TIMEOUT = 5000;
    public static int PORT = 8988;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String ACTION_CONNECT_SOCKET = "connect_socket";
    public static final String ACTION_SEND_MESSAGE = "send_message";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    Socket socket;
    String host;
    int port;


    MainActivity mMainActivity;

    private final IBinder mBinder = new ClientChatService.LocalBinder();


    public ClientChatService(){



    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"Service Starated",Toast.LENGTH_SHORT).show();


       host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);


       port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);


        if(host !=null){
            new Thread(new CommunicationThread()).start();
        }



        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {

        if(intent.getAction().equals(ACTION_CONNECT_SOCKET)){

            Log.e("lauro","connectint socket");

            try {
                if(socket != null){

                    return;
                }
                String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);

                socket = new Socket();
                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                socket.setSoTimeout(1000);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("Hello this is the test message from client");


                Log.e("lauro","socket connected");

                *//*OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(FileServerAsyncTask.mFile.getAbsolutePath()));
                } catch (FileNotFoundException e) {
                    Log.e("lauro", e.toString());
                }
                FileServerAsyncTask.copyFile(is, stream);*//*


            }catch (Exception e){
                e.printStackTrace();
            }

            return;
        }
    }*/

    class CommunicationThread implements  Runnable{

        @Override
        public void run() {

            try {

                InetAddress serverAddress =  InetAddress.getByName(host);

                socket = new Socket(serverAddress,port);

                MessageReader messageReader = new MessageReader(socket);
                new Thread(messageReader).start();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class MessageReader implements Runnable{

        private Socket socket;
        private BufferedReader bufferedReader;

        MessageReader(Socket socket){

            this.socket = socket;

            try {

                this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() ) {

                try {

                    if(!socket.isClosed()){




                        String read = bufferedReader.readLine();

                        if(read != null){

                            Message message = Message.obtain();
                            message.obj = read;
                            mMainActivity.handler.sendMessage(message);

                        }




                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ClientChatService getServiceInstance(){

            return ClientChatService.this;
        }
    }

    public void registerActivity(MainActivity mainActivity){

        mMainActivity = mainActivity;
    }
}
