package com.bitengine.wdproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

interface DeviceListProtocol{
    public void deviceItemClicked(int position);
}

public class MemberActivity extends AppCompatActivity implements WifiP2pManager.ActionListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener, DeviceListProtocol, WifiP2pManager.ConnectionInfoListener {
    private final String TAG = "MEMBER_ACTIVITY";
    @BindView(R.id.member_list_empty) FrameLayout listEmptyMessage;
    @BindView(R.id.member_group_list) RecyclerView groupPeerList;

    private final int NONE = -1;
    private final int ADD_SERVICE_REQUEST = 0;
    private final int CLEAR_SERVICE_REQUESTS = 2;
    private final int DISCOVER_SERVICES = 1;
    private final int CONNECT_TO_SERVICE = 3;


    private int ACTION = -1;
    private boolean RESTART = false;

    private Map<String,String[]> availableServices;

    PeerAdapter mAdapter;
    IntentFilter intentFilter;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WDReceiver mReceiver;

    WifiP2pDnsSdServiceRequest mServiceRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_member);
        ButterKnife.bind(this);

        //Init Recycler Peer List
        mAdapter = new PeerAdapter(this,this, new ArrayList<String>());
        groupPeerList.setAdapter(mAdapter);
        groupPeerList.setLayoutManager(new LinearLayoutManager(this));

        availableServices = new ArrayMap<>();

        //Initialize P2P
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        intentFilter = new IntentFilter();
        //Wi-fi p2p status change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //Change in list of available peers
        //intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        //Connectivity status change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //device details have changed
        //intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mReceiver = new WDReceiver(mManager,mChannel,this);

        //Set the Register Listener for service requests
        mManager.setDnsSdResponseListeners(mChannel, this, this);

        //Only init the service request instance
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();//"_ipp._tcp.local");

        //clearServiceRequests();
    }

    public void clearServiceRequests(){
        if(mManager != null && mChannel != null && mServiceRequest != null){
            mManager.clearServiceRequests(mChannel,this);
            ACTION = CLEAR_SERVICE_REQUESTS;
        }
    }

    public void addServiceRequest(){
        if(mManager != null && mChannel != null && mServiceRequest != null){
            mManager.addServiceRequest(mChannel, mServiceRequest, this);
            ACTION = ADD_SERVICE_REQUEST;
        }
    }

    public void discoverServices(){
        if(mManager != null && mChannel != null) {
            mManager.discoverServices(mChannel, this);
            ACTION = DISCOVER_SERVICES;
        }
    }

    public void connectToService(String deviceTeamName, String deviceAddress){
        Log.d(TAG, "connectToService: "+deviceTeamName+" - "+deviceAddress);
        WifiP2pConfig toConnectConfig = new WifiP2pConfig();
        toConnectConfig.wps.setup = WpsInfo.PBC;
        toConnectConfig.groupOwnerIntent = 0;
        toConnectConfig.deviceAddress=deviceAddress;
        mManager.connect(mChannel, toConnectConfig, this);
        ACTION=CONNECT_TO_SERVICE;
    }

    public void updateAdapterList(){
        ArrayList<String> serviceNames = new ArrayList<String>();
        for(String serv : availableServices.keySet()){
            serviceNames.add(
                availableServices.get(serv)[1]
            );
        }
        listEmptyMessage.setVisibility( serviceNames.size() > 0 ? View.GONE : View.VISIBLE );
        mAdapter.setDevices(serviceNames);
    }

    //Device Item Protocol Methods
    public void deviceItemClicked(int position){
        //ArrayList<String> deviceKeys = mAdapter.getDevices();
        ArrayList<String> deviceKeys = new ArrayList<>();
        for(String k : availableServices.keySet()){
            deviceKeys.add(k);
        }
        if(position >= 0 && position < deviceKeys.size()){
            Log.d(TAG, "deviceItemClicked position valid");
            String deviceAddress = deviceKeys.get(position);

            if(availableServices.containsKey(deviceAddress)) {
                Log.d(TAG, "> Team name is registed");
                String[] deviceInfo = availableServices.get(deviceAddress);
                connectToService(deviceInfo[0], deviceAddress);
            }else{
                Log.d(TAG, "> Team name is not present ");
            }
        }else{
            Log.d(TAG, "deviceItemClicked position is not valid");
        }
    }

    //Connection Info Listener Protocol Methods


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Toast.makeText(this,"Connection info available!",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "New connection info available");
        Log.d(TAG, "> it this device group owner: "+info.isGroupOwner);
        Log.d(TAG, "> has a group been formed: "+info.groupFormed);
        Log.d(TAG, "> address of the group owner :"+info.groupOwnerAddress);
        Log.d(TAG, "> contents description: "+info.describeContents());
        if(info.isGroupOwner){
            Toast.makeText(this,"This device is the group owner!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"This device is not the group owner!",Toast.LENGTH_SHORT).show();
        }
    }

    //DnsSd Service Listener Methods
    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        Log.d(TAG, "onDnsSdServiceAvailable");
        Log.d(TAG, "> instance name: "+instanceName);
        Log.d(TAG, "> registration type: "+registrationType);
        Log.d(TAG, "> device name: "+srcDevice.deviceName);


        if(availableServices.containsKey(srcDevice.deviceAddress)){
            availableServices.get(srcDevice.deviceAddress)[0] = srcDevice.deviceName;
        }else{
            String[] values = new String[2];
            values[0] = srcDevice.deviceName;
            availableServices.put(srcDevice.deviceAddress,values);
        }
        updateAdapterList();
    }

    //DnsSd Service Record Listener Methods
    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        Log.d(TAG, "from - setDnsSdResponseListener - DnsSdTxtRecordListener - onDnsSdTxtRecordAvailable");
        Log.d(TAG, "> domain name - "+fullDomainName);
        Log.d(TAG, "> device name - "+srcDevice.deviceName);
        for(String key : txtRecordMap.keySet()){
            Log.d(TAG, "> "+key+" - "+txtRecordMap.get(key));
        }
        String deviceTeamName = txtRecordMap.get("team_name");
        if(availableServices.containsKey(srcDevice.deviceAddress)){
            availableServices.get(srcDevice.deviceAddress)[1] = deviceTeamName;
        }else{
            String[] values = new String[2];
            values[0] = srcDevice.deviceName;
            values[1] = deviceTeamName;
            availableServices.put(srcDevice.deviceAddress,values);
        }

    }

    //Action Listener Methods
    @Override
    public void onSuccess() {
        switch (ACTION){
            case ADD_SERVICE_REQUEST:
                Toast.makeText(this,"Successfully added service request!",Toast.LENGTH_SHORT).show();
                discoverServices();
                break;
            case CLEAR_SERVICE_REQUESTS:
                Toast.makeText(this, "Successfully clear all service request!",Toast.LENGTH_SHORT).show();
                ACTION=NONE;
                if(RESTART){
                    RESTART = false;
                    addServiceRequest();
                }
                break;
            case DISCOVER_SERVICES:
                Toast.makeText(this,"Successfully discovered services!",Toast.LENGTH_SHORT).show();
                ACTION = NONE;
                break;
            case CONNECT_TO_SERVICE:
                Toast.makeText(this,"Successfully connected to service!",Toast.LENGTH_SHORT).show();

                ACTION=NONE;
                break;
            default: break;
        }
    }
    @Override
    public void onFailure(int reason) {
        switch (ACTION) {
            case ADD_SERVICE_REQUEST:
                Toast.makeText(this, "Failed to add service request! - "+reason, Toast.LENGTH_SHORT).show();
                break;
            case CLEAR_SERVICE_REQUESTS:
                Toast.makeText(this, "Failed to clear service requests! - "+reason, Toast.LENGTH_SHORT).show();
                break;
            case DISCOVER_SERVICES:
                Toast.makeText(this,"Failed to discover sevices! - "+reason,Toast.LENGTH_SHORT).show();
                break;
            case CONNECT_TO_SERVICE:
                Toast.makeText(this,"Faild to connect to service! - "+reason,Toast.LENGTH_SHORT).show();
                break;
            default: break;
        }
        ACTION = NONE;

        //Log.d(TAG, "Discover Peers onFailure");
        //Toast.makeText(this, "Failure Searching for peers!",Toast.LENGTH_SHORT).show();
    }

    /*
    //Peer List Listener Methods
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        ArrayList<WifiP2pDevice> devices = new ArrayList<WifiP2pDevice>();
        for(WifiP2pDevice dev : peers.getDeviceList() ){
            devices.add(dev);
        }
        listEmptyMessage.setVisibility(devices.size() == 0 ? View.VISIBLE : View.GONE);
        Toast.makeText(this, devices.size()+" Peers Found!", Toast.LENGTH_SHORT).show();
        mAdapter.setDevices(devices);
    }
    */

    //Rest of activity setup-ish methods
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else if(item.getItemId() == R.id.main_menu_sync){
            availableServices.clear();
            updateAdapterList();
            RESTART=true;
            clearServiceRequests();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_top_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        super.finish();
        clearServiceRequests();
        overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_right);
    }



    private class WDReceiver extends BroadcastReceiver {
        private final String TAG = "MEMBER_WD_RECEIVER";
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MemberActivity mActivity;

        public WDReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MemberActivity activity){
            super();
            mManager = manager;
            mChannel = channel;
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
                Log.d(TAG, "Wifi State Changed!!!");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    mActivity.RESTART=true;
                    mActivity.clearServiceRequests();
                    Toast.makeText(context,"WIFI P2P Enabled!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context,"WIFI P2P Disabled!",Toast.LENGTH_SHORT).show();
                }
            }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
                Log.d(TAG, "Peers Changed!");
                //mPeersChangedObserver.onNext(0);
            }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                Log.d(TAG, "WIFI P2P Connection has changed!");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    Toast.makeText(mActivity,"Wifi P2P Connected!",Toast.LENGTH_SHORT).show();
                    mManager.requestConnectionInfo(mChannel,mActivity);
                }else{
                    Toast.makeText(mActivity,"Wifi P2P is Disconnected!",Toast.LENGTH_SHORT).show();
                }
            }else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
                Log.d(TAG, "WIFI P2P Current device info has changed!");
            }
        }
    }

}
