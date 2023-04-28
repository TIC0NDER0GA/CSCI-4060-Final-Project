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

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemHolder> {


    private List<Map.Entry<String, String>> item_list;

    public ListItemAdapter(LinkedHashMap<String, String> item_list) {
        this.item_list = new ArrayList<>(item_list.entrySet());
        Log.e(TAG, this.item_list.toString());
    }



    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_items, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(item_list.get(position).getKey(), item_list.get(position).getValue());
    }


    @Override
    public int getItemCount() {
        Log.e(TAG, String.valueOf(item_list.size()));
        return item_list.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        private TextView fb_item;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            fb_item = (TextView) itemView.findViewById(R.id.fb_item);
        }

        public void bind(String item, String name) {
            fb_item.setText(item);
        }

    }



}
