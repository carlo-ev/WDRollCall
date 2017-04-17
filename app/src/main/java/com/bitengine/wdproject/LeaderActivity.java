package com.bitengine.wdproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.HashMap;
import java.util.Map;

public class LeaderActivity extends AppCompatActivity implements WifiP2pManager.ActionListener {
    private final String TAG = "LEADER_ACTIVITY";
    @BindView(R.id.leader_list_empty) FrameLayout listEmptyMessage;
    private String teamName = "";

    private boolean ON = false;

    private final int CLEAR_SERVICES = 0;
    private final int ADD_SERVICE = 1;
    private final int NONE = -1;

    private int ACTION = -1;
    private boolean RESTART = false;

    IntentFilter mFilter;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pDnsSdServiceInfo mService;

    WDReceiver mReceiver;

    private Handler intervalDiscoveryHandler = new Handler();
    private Runnable mServiceBroadCastingRunnable = new Runnable() {
        @Override
        public void run() {
            if(mManager != null){
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() { }
                    @Override
                    public void onFailure(int reason) { }
                });
            }
            if(ON) {
                intervalDiscoveryHandler.postDelayed(mServiceBroadCastingRunnable, 1500);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_leader);
        ButterKnife.bind(this);


        teamName = getIntent().getStringExtra("TEAM_NAME");
        setTitle(teamName + " - Team");
        Log.d(TAG, "Leader onCreate "+teamName);

        //Init P2P
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mReceiver = new WDReceiver(mManager,mChannel,this);


        //Register a Bonjour Service for the Team
        Map<String, String> record = new HashMap<String,String>();
        record.put("team_name",teamName);

        mService = WifiP2pDnsSdServiceInfo.newInstance(
                "_wdProject_"+teamName,
                "_ipp._tcp",
                record
        );

    }

    public void clearServices(){
        ON = false;
        if(mManager != null && mChannel != null){
            mManager.clearLocalServices(mChannel,this);
            ACTION = CLEAR_SERVICES;
        }
    }

    public void addService(){
        if(mManager != null && mChannel != null && mService != null){
            mManager.addLocalService(mChannel, mService, this);
            ACTION = ADD_SERVICE;
        }
    }

    //Action Listener Methods
    @Override
    public void onSuccess() {
        if(ACTION == CLEAR_SERVICES){
            Toast.makeText(this, "Succesfully clear all local services!",Toast.LENGTH_SHORT).show();
            if(RESTART){
                RESTART=false;
                addService();
            }
        }else{
            Toast.makeText(this,"Successfully added local service!",Toast.LENGTH_SHORT).show();
            ON = true;
            intervalDiscoveryHandler.postDelayed(mServiceBroadCastingRunnable,1500);
        }
        ACTION = NONE;
    }

    @Override
    public void onFailure(int reason) {
        if(ACTION == CLEAR_SERVICES){
            Toast.makeText(this,"Failed to clear local services! "+reason,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to add local service! "+reason,Toast.LENGTH_SHORT).show();
        }
    }


    private class WDReceiver extends BroadcastReceiver {
        private final String TAG = "LEADER_WD_RECEIVER";
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private LeaderActivity mActivity;

        //private ObservableEmitter<Integer> mPeersChangedObserver;
        public WDReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, LeaderActivity activity) {
            super();
            mManager = manager;
            mChannel = channel;
            mActivity = activity;
            /*
            Observable.create(new ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                    mPeersChangedObserver = e;
                }
            })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .debounce(3, TimeUnit.SECONDS)
            .subscribe(new Observer<Integer>() {
                @Override
                public void onSubscribe(Disposable d) { }
                @Override
                public void onNext(Integer aInt) {
                    Log.d(TAG, "Observer on next!");
                    mManager.requestPeers(mChannel,mActivity);
                }
                @Override
                public void onError(Throwable e) { }
                @Override
                public void onComplete() { }
            });
            */
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Wifi State Changed!!!");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context, "WIFI P2P Enabled!", Toast.LENGTH_SHORT).show();
                    /*
                    mActivity.RESTART=true;
                    mActivity.clearServices();
                    */
                } else {
                    Toast.makeText(context, "WIFI P2P Disabled!", Toast.LENGTH_SHORT).show();
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Peers Changed!");
                //mPeersChangedObserver.onNext(0);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "WIFI P2P Connection has changed!");
                mActivity.RESTART=true;
                mActivity.clearServices();
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "WIFI P2P Current device info has changed!");
            }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_top_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else if(item.getItemId() == R.id.main_menu_sync){
            RESTART=true;
            clearServices();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        clearServices();
        overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_right);
    }
}
