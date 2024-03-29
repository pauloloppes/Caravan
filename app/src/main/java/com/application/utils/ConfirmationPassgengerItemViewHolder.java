package com.application.utils;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ConfirmationPassgengerItemViewHolder extends RecyclerView.ViewHolder {

    private CheckBox itemCheckbox;

    private TextView itemTextView;

    public ConfirmationPassgengerItemViewHolder(View itemView) {
        super(itemView);
    }

    public CheckBox getItemCheckbox() {
        return itemCheckbox;
    }

    public void setItemCheckbox(CheckBox itemCheckbox) {
        this.itemCheckbox = itemCheckbox;
    }

    public TextView getItemTextView() {
        return itemTextView;
    }

    public void setItemTextView(TextView itemTextView) {
        this.itemTextView = itemTextView;
    }

}
