package com.bytemoe.parkingassistant;

import java.util.List;

public class GarageBean {
    public String type;
    public Data data;

    public static class Data {
        public List<Garage> garages;

        public static class Garage {
            public String name;
            public int total;
            public int remaining;
            public int distance;
            public String[] parkId;
        }
    }
}
