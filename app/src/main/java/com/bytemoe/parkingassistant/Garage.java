package com.bytemoe.parkingassistant;

class Garage {
    private String name;
    private int distance;
    private int total;
    private int remaining;

    public Garage(String name, int distance, int total, int remaining) {
        this.name = name;
        this.distance = distance;
        this.total = total;
        this.remaining = remaining;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }

    public int getTotal() {
        return total;
    }

    public int getRemaining() {
        return remaining;
    }
}
