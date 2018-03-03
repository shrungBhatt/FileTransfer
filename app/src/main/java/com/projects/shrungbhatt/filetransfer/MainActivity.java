package com.projects.shrungbhatt.filetransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener {

    private static final String TAG = "MainActivity";

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter;
    @BindView(R.id.discover_peers_button)
    Button mDiscoverPeersButton;
    @BindView(R.id.connect_peers_button)
    Button mConnectButton;
    @BindView(R.id.text_view)
    TextView mTextView;


    private ArrayList<DeviceDTO> mDeviceDTOs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        setUpIntentFilters();

    }


    private void setUpIntentFilters() {

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mIntentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @OnClick({R.id.discover_peers_button, R.id.connect_peers_button})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.discover_peers_button:
                discoverPeers();
                break;
            case R.id.connect_peers_button:
                connectPeers(mDeviceDTOs.get(0));
                break;
        }

    }

    private void discoverPeers() {

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "Started discovering peers");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Stopped discovering peers");
            }
        });

    }

    private void connectPeers(final DeviceDTO deviceDTO) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceDTO.getIp();
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 4;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                Log.e(TAG, "Connection with the device " + deviceDTO.getPlayerName() + "established.");
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                Log.e(TAG, "Unable to connect with the device " + deviceDTO.getPlayerName());
            }
        });

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        ArrayList<DeviceDTO> deviceDTOs = new ArrayList<>();

        List<WifiP2pDevice> devices = (new ArrayList<>());
        devices.addAll(peerList.getDeviceList());
        for (WifiP2pDevice device : devices) {
            DeviceDTO deviceDTO = new DeviceDTO();
            deviceDTO.setIp(device.deviceAddress);
            deviceDTO.setPlayerName(device.deviceName);
            deviceDTO.setDeviceName(new String());
            deviceDTO.setOsVersion(new String());
            deviceDTO.setPort(-1);
            deviceDTOs.add(deviceDTO);
        }

        mDeviceDTOs = deviceDTOs;


    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }
}
