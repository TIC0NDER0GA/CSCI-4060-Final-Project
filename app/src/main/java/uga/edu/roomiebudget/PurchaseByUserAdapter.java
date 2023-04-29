package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PurchaseByUserAdapter extends RecyclerView.Adapter<PurchaseByUserAdapter.ListContainer> {

    private SimpleData purchased_by_user;
    private ArrayList<ListContainer> listContainers = new ArrayList<>();


    public PurchaseByUserAdapter(LinkedHashMap<String, LinkedHashMap<String,Double>> purchased_by_user) {
        Log.d(TAG, purchased_by_user.toString());
        this.purchased_by_user = new SimpleData(purchased_by_user);
        Log.e(TAG, this.purchased_by_user.toString());
    }

    @NonNull
    @Override
    public ListContainer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View container = LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_purchased_by_user, parent,false);
        ListContainer list = new ListContainer(container);
        listContainers.add(list);
        return list;
    }

    @Override
    public void onBindViewHolder(@NonNull ListContainer holder, int position) {
        holder.bind(purchased_by_user.getTitle(position), purchased_by_user.getList(position));
    }




    @Override
    public int getItemCount() {
        return purchased_by_user.getSize();
    }

    public class ListContainer extends RecyclerView.ViewHolder {
        private TextView title;
        private RecyclerView inner_list;
        private PurchasedListAdapter adapter;

        public ListContainer(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.purchased_title);
            inner_list = itemView.findViewById(R.id.purchased_list);
            inner_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        public void bind(String title, LinkedHashMap<String,Double> list) {
            this.title.setText(title);
            adapter = new PurchasedListAdapter(list);
            inner_list.setAdapter(adapter);
        }

    }


}
