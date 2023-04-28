package uga.edu.roomiebudget;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SimpleData {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<LinkedHashMap<String, Double>> entries = new ArrayList<>();

    public SimpleData(LinkedHashMap<String, LinkedHashMap<String,Double>> data) {
        for (String key : data.keySet()) {
            titles.add(key);
            entries.add(data.get(key));
        }
    }


    public String getTitle(int pos) {
        return titles.get(pos);
    }

    public LinkedHashMap<String, Double> getList(int pos) {
        return entries.get(pos);
    }

    public int getSize() {
        return titles.size();
    }

    public String toString() {
        return entries.toString() + " " + titles.toString();
    }
}
