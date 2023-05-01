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

public class CostsAdapter extends RecyclerView.Adapter<CostsAdapter.CostHolder> {

    private List<Map.Entry<String, Double>> data;

    public CostsAdapter(LinkedHashMap<String, Double> data) {
        Log.d(TAG, data.toString());
        this.data = new ArrayList<>(data.entrySet());
        Log.d(TAG, this.data.toString());

    }

    @NonNull
    @Override
    public CostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cost_item, parent, false);
        return new CostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CostHolder holder, int position) {
        holder.bind(data.get(position).getKey(), data.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class CostHolder extends RecyclerView.ViewHolder {
        public TextView lbl;
        public TextView amount;

        public CostHolder(@NonNull View itemView) {
            super(itemView);
            lbl = (TextView) itemView.findViewById(R.id.label);
            amount = (TextView) itemView.findViewById(R.id.amount);
        }

        public void bind(String label, double value) {
            lbl.setText(label);
            amount.setText(String.valueOf(value));
        }
    }
}