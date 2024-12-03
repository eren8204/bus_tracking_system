package com.example.hackathon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RouteAdapter extends ArrayAdapter<Route> {
    private final Context context;
    private final List<Route> routes;

    public RouteAdapter(Context context, List<Route> routes) {
        super(context, R.layout.route_item, routes);
        this.context = context;
        this.routes = routes;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.route_item, parent, false);
        }

        Route route = routes.get(position);


        TextView busNoView = convertView.findViewById(R.id.bus_no);
        TextView departureTimeView = convertView.findViewById(R.id.departure_time);
        TextView arrivalTimeView = convertView.findViewById(R.id.arrival_time);
        TextView routeView = convertView.findViewById(R.id.route);
        TextView pathView = convertView.findViewById(R.id.path);


        busNoView.setText("Bus No: " + route.getBusNo());
        departureTimeView.setText("Departure: " + route.getDepartureTime());
        arrivalTimeView.setText("Arrival: " + route.getArrivalTime());
        pathView.setText(route.getSource()+" ➡️ "+route.getDest());
        routeView.setText("Route: " + route.getRoute());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, buslocate.class);
                intent.putExtra("busnofromroute",route.getBusNo());
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}

