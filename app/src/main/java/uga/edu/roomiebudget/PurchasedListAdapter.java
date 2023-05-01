package uga.edu.roomiebudget;

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

public class PurchasedListAdapter extends RecyclerView.Adapter<PurchasedListAdapter.PurchasedItemHolder> {

    private List<Map.Entry<String, Double>> purchased_list;


    public PurchasedListAdapter(LinkedHashMap<String, Double> purchased_list) {
        this.purchased_list = new ArrayList<>(purchased_list.entrySet());
    }

    @NonNull
    @Override
    public PurchasedItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View purchased = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchased_list_items, parent, false);
        return new PurchasedItemHolder(purchased);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasedItemHolder holder, int position) {
        holder.bind(purchased_list.get(position).getKey(), purchased_list.get(position).getValue());
    }


    @Override
    public int getItemCount() {
        return purchased_list.size();
    }

    public void overwriteItem(int position, String key, double value) {
        Map.Entry<String, Double> entry = purchased_list.get(position);
        entry.setValue(value);
        notifyDataSetChanged();
    }

    public class PurchasedItemHolder extends RecyclerView.ViewHolder {
        TextView purchased_name;
        TextView purchased_price;
        public PurchasedItemHolder(@NonNull View itemView) {
            super(itemView);
            purchased_name = (TextView)  itemView.findViewById(R.id.purchased_name);
            purchased_price = (TextView) itemView.findViewById(R.id.purchased_price);
        }

        public void bind(String User, Double price) {
            purchased_name.setText(User);
            purchased_price.setText(String.valueOf(price));

            // onclick methods, similar to list item adapter
        }

    }
}
