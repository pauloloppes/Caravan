package com.application.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.application.caravan.R;
import com.application.entities.PackageTrip;
import com.application.entities.Passenger;

import java.util.ArrayList;

public class CustomAdapterPackage extends ArrayAdapter<PackageTrip> implements View.OnClickListener {

    private ArrayList<PackageTrip> dataSet;
    Context mContext;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView labelPackageListName;
        TextView labelPackageListPrice;
    }

    public CustomAdapterPackage(ArrayList<PackageTrip> data, Context context) {
        super(context, R.layout.package_list_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PackageTrip p = getItem(position);
        CustomAdapterPackage.ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new CustomAdapterPackage.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.package_list_row, parent, false);
            viewHolder.labelPackageListName = (TextView) convertView.findViewById(R.id.labelPackageListName);
            viewHolder.labelPackageListPrice = (TextView) convertView.findViewById(R.id.labelPackageListPrice);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomAdapterPackage.ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;
        viewHolder.labelPackageListName.setText(p.getNome());
        viewHolder.labelPackageListPrice.setText("R$ "+p.getPreco());

        return convertView;
    }

}
