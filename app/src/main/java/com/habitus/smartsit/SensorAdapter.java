package com.habitus.smartsit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by brunodocarmo on 23/02/18.
 */

public class SensorAdapter extends BaseAdapter {

    private Context context;
    private Sensor[] sensors;

    public SensorAdapter(Context context, Sensor[] sensors) {
        this.context = context;
        this.sensors = sensors;
    }

    @Override
    public int getCount() {
        return sensors.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Sensor sensor = sensors[position];

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            layoutInflater.inflate(R.layout.sensor, null);

            final View view = convertView.findViewById(R.id.sensor);

        }
        return convertView;
    }
}
