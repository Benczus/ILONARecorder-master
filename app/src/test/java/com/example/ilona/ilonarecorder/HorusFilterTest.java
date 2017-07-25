package com.example.ilona.ilonarecorder;

import com.example.ilona.ilonarecorder.filter.HorusFilter;
import com.example.ilona.ilonarecorder.filter.WiFiRSSIFilteringStrategy;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class HorusFilterTest {

    private static final int MEMORY_SIZE = 3;
    private WiFiRSSIFilteringStrategy filter;

    @Before
    public void setUp() {
        filter = new HorusFilter(MEMORY_SIZE);
    }

    @Test
    public void testWithOneMeasurement() {
        LinkedList<Map<String, Double>> measurements = new LinkedList<>();

        Map<String, Double> meas1 = new HashMap<>();
        meas1.put("AP1", (double) -20);
        meas1.put("AP2", (double) -40);
        meas1.put("AP3", (double) -60);

        measurements.push(meas1);

        Map<String, Double> expected = meas1;
        Map<String, Double> actual = filter.filteringMethod(measurements);

        assertEquals(expected, actual);
    }

    @Test
    public void testWithLessMeasurementsThanMemorySize() {
        LinkedList<Map<String, Double>> measurements = new LinkedList<>();

        Map<String, Double> meas1 = new HashMap<>();
        meas1.put("AP1", (double) -20);
        meas1.put("AP2", (double) -40);
        meas1.put("AP3", (double) -60);

        Map<String, Double> meas2 = new HashMap<>();
        meas2.put("AP1", (double) -21);
        meas2.put("AP2", (double) -41);
        meas2.put("AP3", (double) -61);

        measurements.push(meas1);
        measurements.push(meas2);

        Map<String, Double> expected = meas2;
        Map<String, Double> actual = filter.filteringMethod(measurements);

        assertEquals(expected, actual);
    }

    @Test
    public void testWhenMeasurementsCountEqualsMemorySize() {
        LinkedList<Map<String, Double>> measurements = new LinkedList<>();

        Map<String, Double> meas1 = new HashMap<>();
        meas1.put("AP1", (double) -20);
        meas1.put("AP2", (double) -40);
        meas1.put("AP3", (double) -60);

        Map<String, Double> meas2 = new HashMap<>();
        meas2.put("AP1", (double) -21);
        meas2.put("AP2", (double) -41);
        meas2.put("AP3", (double) -61);

        Map<String, Double> meas3 = new HashMap<>();
        meas3.put("AP1", (double) -22);
        meas3.put("AP2", (double) -42);
        meas3.put("AP3", (double) -62);

        measurements.push(meas1);
        measurements.push(meas2);
        measurements.push(meas3);

        //Calculated and set by us
        Map<String, Double> expected = new HashMap<>();
        expected.put("AP1", (double) -21);
        expected.put("AP2", (double) -41);
        expected.put("AP3", (double) -61);

        Map<String, Double> actual = filter.filteringMethod(measurements);

//        System.out.println("Expected ==> "+ expected);
//        System.out.println("Actual ====> "+actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testWithMoreMeasurementsThanMemorySize() {
        LinkedList<Map<String, Double>> measurements = new LinkedList<>();

        Map<String, Double> meas1 = new HashMap<>();
        meas1.put("AP1", (double) -20);
        meas1.put("AP2", (double) -40);
        meas1.put("AP3", (double) -60);

        Map<String, Double> meas2 = new HashMap<>();
        meas2.put("AP1", (double) -21);
        meas2.put("AP2", (double) -41);
        meas2.put("AP3", (double) -61);

        Map<String, Double> meas3 = new HashMap<>();
        meas3.put("AP1", (double) -22);
        meas3.put("AP2", (double) -42);
        meas3.put("AP3", (double) -62);

        Map<String, Double> meas4 = new HashMap<>();
        meas4.put("AP1", (double) -23);
        meas4.put("AP2", (double) -43);
        meas4.put("AP3", (double) -63);

        measurements.push(meas1);
        measurements.push(meas2);
        measurements.push(meas3);
        measurements.push(meas4);

        //Calculated and set by us
        Map<String, Double> expected = new HashMap<>();
        expected.put("AP1", (double) -22);
        expected.put("AP2", (double) -42);
        expected.put("AP3", (double) -62);

        Map<String, Double> actual = filter.filteringMethod(measurements);
        assertEquals(expected, actual);
    }

    @Test
    public void testWithMissingAPValues() {
        LinkedList<Map<String, Double>> measurements = new LinkedList<>();

        Map<String, Double> meas1 = new HashMap<>();
//        meas1.put("AP1", Double.valueOf(-20));
//        meas1.put("AP2", Double.valueOf(-40));

        Map<String, Double> meas2 = new HashMap<>();
//        meas2.put("AP1", Double.valueOf(-21));
//        meas2.put("AP2", Double.valueOf(-41));
        meas2.put("AP3", (double) -61);

        Map<String, Double> meas3 = new HashMap<>();
//        meas3.put("AP1", Double.valueOf(-22));
//        meas3.put("AP2", Double.valueOf(-42));
        meas3.put("AP3", (double) -62);

        measurements.push(meas1);
        measurements.push(meas2);
        measurements.push(meas3);

        //Calculated and set by us
        Map<String, Double> expected = new HashMap<>();
//        expected.put("AP1", Double.valueOf(-21));
//        expected.put("AP2", Double.valueOf(-41));
        expected.put("AP3", -61.5);

        Map<String, Double> actual = filter.filteringMethod(measurements);


        assertEquals(expected, actual);
    }
}
