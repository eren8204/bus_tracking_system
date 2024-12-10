package com.example.hackathon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends ArrayAdapter<Route> implements Filterable {
    private final Context context;
    private List<Route> routes;
    private final List<Route> originalRoutes; // For holding the original list

    public RouteAdapter(Context context, List<Route> routes) {
        super(context, R.layout.route_item, routes);
        this.context = context;
        this.routes = routes;
        this.originalRoutes = new ArrayList<>(routes); // Copy of the original list
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
        pathView.setText(route.getSource() + " ➡️ " + route.getDest());
        routeView.setText("Route: " + route.getRoute());

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, buslocate.class);
            intent.putExtra("busnofromroute", route.getBusNo());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }

        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Route> filteredResults = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredResults.addAll(originalRoutes);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Route route : originalRoutes) {
                        if (route.getBusNo().toLowerCase().contains(filterPattern) ||
                                route.getRoute().toLowerCase().contains(filterPattern) ||
                                route.getSource().toLowerCase().contains(filterPattern) ||
                                route.getDest().toLowerCase().contains(filterPattern)) {
                            filteredResults.add(route);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                results.count = filteredResults.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                routes.clear();
                routes.addAll((List<Route>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
