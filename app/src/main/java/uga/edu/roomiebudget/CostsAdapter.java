package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Class whose purpose is to
 * organize and display the Settled costs for
 * a group.
 */
public class CostsAdapter extends RecyclerView.Adapter<CostsAdapter.CostHolder> {

    private List<Map.Entry<String, Double>> data; // the labels and values for settled costs

    /**
     * Constructor that takes in the
     * groups settled cost data.
     * @param data
     */
    public CostsAdapter(LinkedHashMap<String, Double> data) {
        // Log.d(TAG, data.toString());
        this.data = new ArrayList<>(data.entrySet());
        // Log.d(TAG, this.data.toString());

    }

    /**
     * Method to create a view holder to inflate the the correct layout for the item on the
     * shopping list.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public CostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cost_item, parent, false);
        return new CostHolder(view);
    }

    /**
     * The method to bind the item list to the view to correctly display the items using the
     * settled costs data .
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CostHolder holder, int position) {
        holder.bind(data.get(position).getKey(), data.get(position).getValue());
    }

    /**
     * Gets the amount of different
     * settled costs for a group.
     * @return int # of setteled costs
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * A class whose purpose is to hold the
     * type of cost and values and bind them to the
     * appropriate view.
     */
    public static class CostHolder extends RecyclerView.ViewHolder {
        public TextView lbl; // the cost label
        public TextView amount; // the cost amount

        /**
         * A constructor that retrieves the
         * xml resources.
         * @param itemView
         */
        public CostHolder(@NonNull View itemView) {
            super(itemView);
            lbl = (TextView) itemView.findViewById(R.id.label);
            amount = (TextView) itemView.findViewById(R.id.amount);
        }

        /**
         * Changes the label of the cost
         * and converts the double value to a
         * string for use within a TextView
         * @param label the cost labbel
         * @param value the cost's value
         */
        public void bind(String label, double value) {
            lbl.setText(label);
            String formattedValue = String.format("%.2f", value);
            amount.setText(formattedValue);
        }
    }
}