package com.blogspot.justsimpleinfo.wifidirectchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by Lauro-PC on 7/2/2017.
 */

public class CustomBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    public CustomBroadcastReceiver(){
        super();
    }
    public CustomBroadcastReceiver(WifiP2pManager manager,
                                   WifiP2pManager.Channel channel,
                                   MainActivity activity){
        super();

        mManager = manager;
        mChannel = channel;
        mActivity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled

                Log.e("=======","wifi direct enable");
            } else {

                Log.e("=======","wifi direct not enable");

            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (mManager != null) {
                /**
                 * disconver new devices
                 */
                android.support.v4.app.FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                AvailableDeviceFragment fragment = (AvailableDeviceFragment) fragmentManager.findFragmentByTag(AvailableDeviceFragment.FRAGMENT_TAG);

                if(fragment !=null){
                    mManager.requestPeers(mChannel, fragment.mCustomPeerListener);


                }


            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP


                android.support.v4.app.FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                AvailableDeviceFragment fragment = (AvailableDeviceFragment) fragmentManager.findFragmentByTag(AvailableDeviceFragment.FRAGMENT_TAG);
                mManager.requestConnectionInfo(mChannel, fragment);



            } else {
                // It's a disconnect


                android.support.v4.app.FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                AvailableDeviceFragment fragment = (AvailableDeviceFragment) fragmentManager.findFragmentByTag(AvailableDeviceFragment.FRAGMENT_TAG);

                fragment.search();
            }



        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {


            if (mManager == null) {
                return;
            }

            Log.e("lauro","WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION 2");
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (networkInfo!= null && networkInfo.isConnected()) {

                Log.e("lauro","WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION 3");
                // we are connected with the other device, request connection
                // info to find group owner IP

                /*DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);*/

                android.support.v4.app.FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                AvailableDeviceFragment fragment = (AvailableDeviceFragment) fragmentManager.findFragmentByTag(AvailableDeviceFragment.FRAGMENT_TAG);
                mManager.requestConnectionInfo(mChannel, fragment);



            } else {
                // It's a disconnect
                //activity.resetData();

                //mActivity.processFragment(mActivity.getSupportFragmentManager());
                Log.e("lauro","WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION 5");
            }

        }


    }
}
