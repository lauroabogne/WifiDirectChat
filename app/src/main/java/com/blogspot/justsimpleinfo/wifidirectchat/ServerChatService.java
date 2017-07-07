package com.blogspot.justsimpleinfo.wifidirectchat;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Lauro-PC on 7/4/2017.
 */

public class ServerChatService extends Service {

    MainActivity mMainActivity;
    Socket mClientSocket;

    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {

            handler.postDelayed(this, 1000);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(new CommunicationThread()).start();

        Toast.makeText(this,"Server Chat started",Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }




    class CommunicationThread implements Runnable{

        boolean continueRunning = true;

        @Override
        public void run() {

            Log.e("server thread run","server thread run");
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(ClientChatService.PORT));
                mClientSocket = serverSocket.accept();

                while (!Thread.currentThread().isInterrupted() && continueRunning) {

                    try {

                        if(!serverSocket.isClosed()){



                            if(!mClientSocket.isClosed()){

                                BufferedReader input = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                                String read = input.readLine();

                                if(read == null){

                                    mClientSocket.close();
                                    continueRunning = false;

                                }else{

                                    Message message = Message.obtain();
                                    message.obj = read;
                                    mMainActivity.handler.sendMessage(message);

                                }
                                Log.e("Message to Server",read+"");
                            }


                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ServerChatService getServiceInstance(){
            return ServerChatService.this;
        }
    }

    public void registerActivity(MainActivity mainActivity){

        mMainActivity = mainActivity;
    }
}
