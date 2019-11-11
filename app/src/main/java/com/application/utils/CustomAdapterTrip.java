package com.application.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.caravan.R;
import com.application.entities.Passenger;
import com.application.entities.Trip;

import java.util.ArrayList;

public class CustomAdapterTrip extends ArrayAdapter<Trip> implements View.OnClickListener {

    private ArrayList<Trip> dataSet;
    Context mContext;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView labelTripListName;
        TextView labelTripListDate;
        TextView labelTripListDestination;
    }

    public CustomAdapterTrip(ArrayList<Trip> data, Context context) {
        super(context, R.layout.trip_list_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trip t = getItem(position);
        CustomAdapterTrip.ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new CustomAdapterTrip.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.trip_list_row, parent, false);
            viewHolder.labelTripListName = (TextView) convertView.findViewById(R.id.labelTripListName);
            viewHolder.labelTripListDate = (TextView) convertView.findViewById(R.id.labelTripListDate);
            viewHolder.labelTripListDestination = (TextView) convertView.findViewById(R.id.labelTripListDestination);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomAdapterTrip.ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;
        viewHolder.labelTripListName.setText(t.getNome());
        viewHolder.labelTripListDate.setText(t.getPartida_data());
        viewHolder.labelTripListDestination.setText(t.getDestino());

        return convertView;
    }
}
