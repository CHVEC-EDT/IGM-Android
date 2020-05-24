package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

class GarageAdapter extends ArrayAdapter<Garage> {

    private final int resourceId;

    @SuppressWarnings("SameParameterValue")
    GarageAdapter(@NonNull Context context, int resource, @NonNull List<Garage> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Nullable
    @Override
    public Garage getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable Garage item) {
        return super.getPosition(item);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Garage garage = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = view.findViewById(R.id.tv_name);
            viewHolder.tv_distance = view.findViewById(R.id.tv_distance);
            viewHolder.tv_space_total = view.findViewById(R.id.tv_space_total);
            viewHolder.tv_space_remaining = view.findViewById(R.id.tv_space_remaining);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        assert garage != null;
        viewHolder.tv_name.setText(garage.getName());
        viewHolder.tv_distance.setText("距你 " + garage.getDistance() + " KM");
        viewHolder.tv_space_total.setText("/ " + garage.getTotal() + " 车位");
        viewHolder.tv_space_remaining.setText(garage.getRemaining() + "");
        return view;
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_distance;
        TextView tv_space_total;
        TextView tv_space_remaining;
    }
}
