package com.example.ilona.ilonarecorder.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

//Implementation of the Horus filter
public class HorusFilter implements WiFiRSSIFilteringStrategy {

    private final int memsize;


    public HorusFilter(int memsize) {
        this.memsize = memsize;
    }

    //Main filtering method

    @Override
    public Map<String, Double> filteringMethod(LinkedList<Map<String, Double>> linkedList) {
        if (linkedList.size() < memsize) {
            return linkedList.getFirst();
        }
        Map<String, Double> result = new HashMap<>();
        for (String ssid : getKeys(linkedList)) {
            ArrayList<Double> rssiValues = getWiFiRSSIVector(ssid, linkedList);
            double filteredValue = filter(rssiValues);
            result.put(ssid, filteredValue);
        }
        return result;
    }

    private Set<String> getKeys(LinkedList<Map<String, Double>> linkedList) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < linkedList.size(); i++) {
            result.addAll(linkedList.get(i).keySet());
        }
        return result;
    }

    private ArrayList<Double> getWiFiRSSIVector(String ssid, LinkedList<Map<String, Double>> linkedList) {
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < memsize; i++) {
            if (linkedList.get(i).get(ssid) != null) {
                result.add(linkedList.get(i).get(ssid));
            }
        }
        return result;
    }

    private double filter(ArrayList<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }

}

