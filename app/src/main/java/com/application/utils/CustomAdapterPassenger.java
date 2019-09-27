package com.application.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.caravan.R;
import com.application.entities.Passenger;

import java.util.ArrayList;

public class CustomAdapterPassenger extends ArrayAdapter<Passenger> implements View.OnClickListener {

    private ArrayList<Passenger> dataSet;
    Context mContext;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView labelPassengerListName;
        TextView labelPassengerListIdentity;
    }

    public CustomAdapterPassenger(ArrayList<Passenger> data, Context context) {
        super(context, R.layout.passenger_list_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Passenger p = getItem(position);
        ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.passenger_list_row, parent, false);
            viewHolder.labelPassengerListName = (TextView) convertView.findViewById(R.id.labelPassengerListName);
            viewHolder.labelPassengerListIdentity = (TextView) convertView.findViewById(R.id.labelPassengerListIdentity);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;
        viewHolder.labelPassengerListName.setText(p.getNome());
        viewHolder.labelPassengerListIdentity.setText(p.getIdentidade());

        return convertView;
    }
}
