package com.john.chargemanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.john.chargemanager.DataModel.ServiceData;
import com.john.chargemanager.R;

import java.util.ArrayList;

/**
 * Created by dev on 11/8/2015.
 */
public class ServiceListAdapter extends BaseAdapter {

    Context context;
    ArrayList<ServiceData> serviceArray;

    private static LayoutInflater inflater = null;

    public ServiceListAdapter(
            Context context,
            ArrayList<ServiceData> serviceArray) {

        this.context = context;
        this.serviceArray = serviceArray;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return serviceArray.size();
    }

    @Override
    public Object getItem(int position) {
        return serviceArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ServiceItemHoler holder = null;
        final ServiceData selService = serviceArray.get(position);

        if (convertView != null) {
            holder = (ServiceItemHoler)convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.item_uuid, parent, false);
            holder = new ServiceItemHoler(convertView);
            convertView.setTag(holder);
        }

        holder.setUuid(selService.getUuid());
        holder.setName(selService.getName());
        holder.setValue(selService.getValue());

        return convertView;
    }


    public class ServiceItemHoler  {
        TextView lblUuid;
        TextView lblName;
        TextView lblValue;

        public ServiceItemHoler(View vwItem) {
            lblUuid = (TextView)vwItem.findViewById(R.id.lbl_Uuid);
            lblName = (TextView)vwItem.findViewById(R.id.lbl_Service);
            lblValue = (TextView)vwItem.findViewById(R.id.lbl_Value);
        }

        public void setUuid(String uuid) {
            if (uuid.length() != 0) {
                lblUuid.setText("UUID: " + uuid.toUpperCase());
            } else {
                lblUuid.setText("UUID: ");
            }
        }

        public void setName(String name) {
            if (name.length() != 0) {
                lblName.setText("Name: " + name);
            } else {
                lblName.setText("Name: ");
            }
        }

        public void setValue(String value) {
            if (value.length() != 0) {
                lblValue.setText("Value: " + value);
            } else {
                lblValue.setText("Value: ");
            }
        }
    }


}
