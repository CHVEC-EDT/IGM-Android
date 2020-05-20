package com.bytemoe.parkingassistant;

class Garage {
    private String name;
    private int distance;
    private int total;
    private int remaining;
    private String[] remainingList;

    Garage(String name, int distance, int total, int remaining, String[] remainingList) {
        this.name = name;
        this.distance = distance;
        this.total = total;
        this.remaining = remaining;
        this.remainingList = remainingList;
    }

    public String getName() {
        return name;
    }

    int getDistance() {
        return distance;
    }

    int getTotal() {
        return total;
    }

    int getRemaining() {
        return remaining;
    }

    String[] getRemainingList() {
        return remainingList;
    }
}
