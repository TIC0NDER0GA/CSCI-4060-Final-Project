package uga.edu.roomiebudget;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * A class that transforms nested linked hashmaps into
 * data that is more easily iterable and indexable by
 * recyclerview adapters.
 */
public class SimpleData {

    ArrayList<String> titles = new ArrayList<>(); // the parent node of the incoming data
    ArrayList<LinkedHashMap<String, Double>> entries = new ArrayList<>(); // the children nodes of the incoming data

    /**
     * A constructor that takes in the data and seprates it into two more easily
     * usable lists.
     * @param data a nested linked hashmap due to the way Firebase encodes key value data
     */
    public SimpleData(LinkedHashMap<String, LinkedHashMap<String,Double>> data) {
        for (String key : data.keySet()) {
            titles.add(key);
            entries.add(data.get(key));
        }
    }


    /**
     * Gets the Parent key node of the underlying list
     * @param pos the position of the parent node
     * @return
     */
    public String getTitle(int pos) {
        return titles.get(pos);
    }

    /**
     * Gets the list of children key value pairs for recycler view
     * adapters.
     * @param pos the position of the children key value list
     * @return
     */
    public LinkedHashMap<String, Double> getList(int pos) {
        return entries.get(pos);
    }

    /**
     * Gets the size of the list
     * parent key nodes along with
     * their children are counted as
     * one regardless of the children list size;
     * @return int size of nested list
     */
    public int getSize() {
        return titles.size();
    }


    /**
     * Gives a string representation of the
     * whole data structure.
     * @return String the data in plain text
     */
    public String toString() {
        return entries.toString() + " " + titles.toString();
    }
}
