package com.bitengine.wdproject;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by carlo on 04-13-17.
 */
public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.ViewHolder> implements View.OnClickListener {
    private final String TAG = "PEER_ADAPTER";
    Context mContext;
    ArrayList<String> serviceNames;
    DeviceListProtocol mDeviceItemListener;

    public PeerAdapter(Context pContext, DeviceListProtocol itemListener, ArrayList<String> pDeviceList){
        mContext = pContext;
        mDeviceItemListener = itemListener;
        serviceNames = pDeviceList;
    }

    public void setDevices(ArrayList<String> pDeviceList){
        serviceNames = pDeviceList;
        notifyDataSetChanged();
    }

    public ArrayList<String> getDevices(){
        return serviceNames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate(R.layout.peer_item,parent,false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "List row was clicked!!!!!");
        mDeviceItemListener.deviceItemClicked((int) v.getTag());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.itemView.setTag(position);
        holder.deviceName.setText( serviceNames.get(position) );
    }


    @Override
    public int getItemCount(){
        return serviceNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView deviceName;
        public ViewHolder(View root){
            super(root);
            deviceName = (TextView) root.findViewById(R.id.peer_item_name);
        }
    }

}
