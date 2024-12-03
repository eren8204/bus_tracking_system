package com.example.hackathon;

public class Route {
    private final int id;
    private final String busNo;
    private final String departureTime;
    private final String arrivalTime;
    private final String route;

    public Route(int id, String busNo, String departureTime, String arrivalTime, String route) {
        this.id = id;
        this.busNo = busNo;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.route = route;
    }

    public int getId() {
        return id;
    }

    public String getBusNo() {
        return busNo;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getRoute() {
        return route;
    }
}
