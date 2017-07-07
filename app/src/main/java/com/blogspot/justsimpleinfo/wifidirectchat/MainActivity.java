package com.blogspot.justsimpleinfo.wifidirectchat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements AvailableDeviceFragment.OnFragmentInteractionListener {



    protected BroadcastReceiver mReceiver = null;
    protected WifiP2pManager mManager;
    protected WifiP2pManager.Channel mChannel;
    WifiP2pInfo mWifiP2pInfo;

    ServerChatService mServerChatService;
    ClientChatService mClientChatService;

    boolean isServer = false;

    boolean doServerChatServiceBind = false;
    boolean doClientChatServiceBind = false;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;

            try {

                JSONObject jsonObject = new JSONObject(message);
                String from = jsonObject.getString(MessageConstantVariables.FROM);

                if(from.equals(MessageConstantVariables.FROM_SERVER)){

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentByTag(ChatFragment.FRAGMENT_TAG);

                    chatFragment.messages.add(jsonObject);

                    ChatFragment.MessageCustomAdapter messageCustomAdapter = (ChatFragment.MessageCustomAdapter) chatFragment.mMessageListview.getAdapter();
                    messageCustomAdapter.notifyDataSetChanged();

                }else{

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentByTag(ChatFragment.FRAGMENT_TAG);

                    chatFragment.messages.add(jsonObject);

                    ChatFragment.MessageCustomAdapter messageCustomAdapter = (ChatFragment.MessageCustomAdapter) chatFragment.mMessageListview.getAdapter();
                    messageCustomAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("message data",message+"");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();






    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(doClientChatServiceBind){
            unbindService(mClientServiceChatConnection);
        }

        if(doServerChatServiceBind){

            unbindService(mClientServiceChatConnection);
        }
       /* if(mServerServiceChatConnection !=null){
            unbindService(mServerServiceChatConnection);
        }
        if(mClientServiceChatConnection !=null){

            unbindService(mClientServiceChatConnection);
        }*/



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.show_available_device) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            AvailableDeviceFragment availableDeviceFragment = new AvailableDeviceFragment();
            processFragment(fragmentManager,availableDeviceFragment, AvailableDeviceFragment.FRAGMENT_TAG);




        }else if(id == R.id.disconnect){

            disconnect();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void processFragment(FragmentManager fragmentManager, Fragment fragment, String fragmentTag){

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.content_main, fragment, fragmentTag);
        fragmentTransaction.commit();








    }
    public  void disconnect() {



        Log.e("lauro","disconnect 1");

        if (mManager != null && mChannel != null) {

            Log.e("lauro","disconnect 2");

            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {

                    Log.e("lauro","disconnect 6 "+group.isGroupOwner()+" group "+group);
                    //if (group != null && mManager != null && mChannel != null && group.isGroupOwner()) {

                        Log.e("lauro","disconnect 5");

                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("lauro", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("lauro", "removeGroup onFailure -" + reason);
                            }
                        });
                   /* }else{

                        Log.e("lauro","disconnect 4");
                    }*/
                }
            });
        }else{

            Log.e("lauro","disconnect 3");
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    protected ServiceConnection mServerServiceChatConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();

            doServerChatServiceBind = true;

            ServerChatService.LocalBinder binder = (ServerChatService.LocalBinder) service;
            mServerChatService = binder.getServiceInstance();
            mServerChatService.registerActivity(MainActivity.this);


            FragmentManager fragmentManager = getSupportFragmentManager();
            ChatFragment chatFragment = new ChatFragment();
            processFragment(fragmentManager,chatFragment, ChatFragment.FRAGMENT_TAG);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            doServerChatServiceBind = false;

            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
        }
    };


    protected ServiceConnection mClientServiceChatConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();

            doClientChatServiceBind = true;

            ClientChatService.LocalBinder binder = (ClientChatService.LocalBinder) service;
            mClientChatService = binder.getServiceInstance();
            mClientChatService.registerActivity(MainActivity.this);


            FragmentManager fragmentManager = getSupportFragmentManager();
            ChatFragment chatFragment = new ChatFragment();
            processFragment(fragmentManager,chatFragment, ChatFragment.FRAGMENT_TAG);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();

            doClientChatServiceBind = false;
        }
    };


}
