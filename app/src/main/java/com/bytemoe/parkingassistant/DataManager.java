package com.bytemoe.parkingassistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private static volatile DataManager INSTANCE;

    private final List<Garage> garageList = new ArrayList<>();
    private final HashMap<String, Object> store = new HashMap<>();

    private DataManager() {
    }

    public static DataManager getInstance() {
        if (INSTANCE == null) synchronized (DataManager.class) {
            if (INSTANCE == null) INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public List<Garage> getGarageList() {
        return garageList;
    }

    public HashMap<String, Object> getStore() {
        return store;
    }
}