package com.example.ilona.ilonarecorder.filter;

import java.util.LinkedList;
import java.util.Map;


public interface WiFiRSSIFilteringStrategy {

    Map<String, Double> filteringMethod(LinkedList<Map<String, Double>> linkedList);

}
