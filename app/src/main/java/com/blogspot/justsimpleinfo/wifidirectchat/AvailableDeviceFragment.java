package com.blogspot.justsimpleinfo.wifidirectchat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AvailableDeviceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class AvailableDeviceFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener, AdapterView.OnItemClickListener {

    public final static String FRAGMENT_TAG = "AvailableDeviceFragment";
    private OnFragmentInteractionListener mListener;
    /**
     * @mPeers available device
     */
    private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();

    ListView mAvailableDeviceListView;
    CustomPeerListener mCustomPeerListener;
    MainActivity mMainActivity;


    private final IntentFilter intentFilter = new IntentFilter();



    public AvailableDeviceFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mCustomPeerListener = new CustomPeerListener();
        mMainActivity = (MainActivity) this.getActivity();


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mMainActivity.mManager = (WifiP2pManager) mMainActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mMainActivity.mChannel = mMainActivity.mManager.initialize(mMainActivity,mMainActivity.getMainLooper(), null);



    }

    public void search(){


        mMainActivity.mManager.discoverPeers(mMainActivity.mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(AvailableDeviceFragment.this.getContext(), "Discovery InitiAvailableDeviceFragment",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(AvailableDeviceFragment.this.getContext(), "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        mMainActivity.mReceiver = new CustomBroadcastReceiver(mMainActivity.mManager, mMainActivity.mChannel, mMainActivity);
        mMainActivity.registerReceiver(mMainActivity.mReceiver, intentFilter);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mMainActivity.unregisterReceiver(mMainActivity.mReceiver);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_available_device, container, false);
        mAvailableDeviceListView = (ListView) frameLayout.findViewById(R.id.available_device_listview);

        mAvailableDeviceListView.setOnItemClickListener(this);

        return frameLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        mMainActivity.mWifiP2pInfo = info;

        if (info.groupFormed && info.isGroupOwner) {

            // Do whatever you want the group owner to do

            Toast.makeText(this.getContext(),"Act as server",Toast.LENGTH_SHORT).show();

             //new ServerAsynctask().execute();


            mMainActivity.isServer = true;

            Intent serverChatService = new Intent(getContext().getApplicationContext(), ServerChatService.class);
            mMainActivity.startService(serverChatService);
            mMainActivity.bindService(serverChatService, mMainActivity.mServerServiceChatConnection,Context.BIND_AUTO_CREATE); //Binding to the service!

        } else if (info.groupFormed) {
            // The device now act as the slave device,
            // and the other connected device is group owner

            Toast.makeText(this.getContext(),"This device will act as a client. Click on Gallery button to pick a local(stored) file",Toast.LENGTH_SHORT).show();

            mMainActivity.isServer = false;
            connectSocketServiceStart();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) view.getTag();

        WifiP2pDevice device = mPeers.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        config.groupOwnerIntent = 15;
        mMainActivity.mManager.cancelConnect(mMainActivity.mChannel, null);



        mMainActivity.mManager.connect(mMainActivity.mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

                Toast.makeText(AvailableDeviceFragment.this.getContext(), "sUCCESS.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(AvailableDeviceFragment.this.getContext(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });


        Toast.makeText(this.getContext(),wifiP2pDevice+"",Toast.LENGTH_SHORT).show();
    }

    public void connectSocketServiceStart(){

        Intent serviceIntent = new Intent(getContext().getApplicationContext(), ClientChatService.class);

        serviceIntent.putExtra(ClientChatService.EXTRAS_GROUP_OWNER_ADDRESS, mMainActivity.mWifiP2pInfo.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(ClientChatService.EXTRAS_GROUP_OWNER_PORT, ClientChatService.PORT);

        mMainActivity.startService(serviceIntent);
        mMainActivity.bindService(serviceIntent, mMainActivity.mClientServiceChatConnection,Context.BIND_AUTO_CREATE); //Binding to the service!
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     *
     */
    class CustomPeerListener implements WifiP2pManager.PeerListListener{

          @Override
          public void onPeersAvailable(WifiP2pDeviceList peers) {

              AvailableDeviceFragment.this.mPeers.clear();;
              AvailableDeviceFragment.this.mPeers.addAll(peers.getDeviceList());

              mAvailableDeviceListView.setAdapter(new CustomAdapater(AvailableDeviceFragment.this.mPeers));


          }
      }

    /**
     *
     */
    class CustomAdapater extends BaseAdapter {

        List<WifiP2pDevice> mPeers;
        CustomAdapater(List<WifiP2pDevice> peers){

            mPeers = peers;
        }
        @Override
        public int getCount() {
            return mPeers.size();
        }

        @Override
        public Object getItem(int position) {
            return mPeers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            WifiP2pDevice wifiP2pDevice = mPeers.get(position);

            LinearLayout linearLayout = (LinearLayout) convertView;


            if(linearLayout == null){

                linearLayout = (android.widget.LinearLayout) LayoutInflater.from(AvailableDeviceFragment.this.getContext()).inflate(android.R.layout.two_line_list_item,null,false);

            }

            TextView textView1 = (TextView) linearLayout.getChildAt(0);
            TextView textView2 = (TextView) linearLayout.getChildAt(1);

            textView1.setText(wifiP2pDevice.deviceName);
            textView2.setText(wifiP2pDevice.deviceAddress);

            linearLayout.setTag(wifiP2pDevice);


            return linearLayout;
        }
    }

    /*class ServerAsynctask extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {


            try {
                ServerSocket serverSocket = new ServerSocket(ClientChatService.PORT);
                Socket client = serverSocket.accept();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }*/
}
