package com.john.chargemanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.john.chargemanager.MicroService.Bluetooth.SP25.Sp25Device;
import com.john.chargemanager.R;

import java.util.ArrayList;

/**
 * Created by dev on 11/6/2015.
 */
public class DeviceListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Sp25Device> devicesArray;

    private static LayoutInflater inflater = null;

    public DeviceListAdapter(
            Context context,
            ArrayList<Sp25Device> devicesArray) {

        this.context = context;
        this.devicesArray = devicesArray;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return devicesArray.size();
    }

    @Override
    public Object getItem(int position) {
        return devicesArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DeviceItemHolder holder = null;
        Sp25Device selDevice = devicesArray.get(position);

        if (convertView != null) {
            holder = (DeviceItemHolder)convertView.getTag();
        }
        else {
            convertView = inflater.inflate(R.layout.item_device, parent, false);

            holder = new DeviceItemHolder(convertView);

            convertView.setTag(holder);
        }

        holder.setDeviceName(selDevice.getName());
        holder.setRssi(selDevice.getRssi());
        holder.setAddress(selDevice.getAddress());

        return convertView;
    }




    public class DeviceItemHolder {
        TextView lblName;
        TextView lblRssi;
        TextView lblAddress;

        public DeviceItemHolder(View vwItem) {
            lblName = (TextView)vwItem.findViewById(R.id.lbl_Name);
            lblRssi = (TextView)vwItem.findViewById(R.id.lbl_Rssi);
            lblAddress = (TextView)vwItem.findViewById(R.id.lbl_Address);
        }

        public void setDeviceName(String devName) {
            if (devName.length() != 0) {
                lblName.setText(devName);
            }
        }

        public void setRssi(int rssi) {
            String szRssi;

            szRssi = "Rssi: " + String.valueOf(rssi);
            lblRssi.setText(szRssi);
        }

        public void setAddress(String address) {
            lblAddress.setText("Address: " + address);
        }
    }
}
