package com.lnquy065.uvzone.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lnquy065.uvzone.MapActivity;
import com.lnquy065.uvzone.R;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by LN Quy on 18/04/2018.
 */

public class BluetoothDiviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private Activity context;
    private int resource;
    private ArrayList<BluetoothDevice> objects;
    public BluetoothDiviceAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<BluetoothDevice> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View row = layoutInflater.inflate(resource, null);
        BluetoothDevice btd = objects.get(position);

        TextView lbBTName = row.findViewById(R.id.lbBTName);
        TextView lbBTMAC = row.findViewById(R.id.lbBTMAC);

        lbBTName.setText(btd.getName());
        lbBTMAC.setText(btd.getAddress());
        return row;
    }
}
