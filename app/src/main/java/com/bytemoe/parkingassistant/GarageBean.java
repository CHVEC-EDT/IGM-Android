package com.bytemoe.parkingassistant;

import java.util.List;

@SuppressWarnings("unused")
class GarageBean {
    public String type;
    Data data;

    static class Data {
        List<Garage> garages;

        public static class Garage {
            public String name;
            int total;
            int remaining;
            int distance;
            String[] parkId;
        }
    }
}
