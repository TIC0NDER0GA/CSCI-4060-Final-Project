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

    private SimpleData purchased_by_user; // custom class to handle LinkedHashMap Conversion
    private ArrayList<ListContainer> listContainers = new ArrayList<>(); // holds the children ListContainers to be filled with their own recyclerview


    /**
     * The constructor that uses simple data to split the headers and
     * lists into two for easier processing.
     * @param purchased_by_user the key parent and children key value list
     */
    public PurchaseByUserAdapter(LinkedHashMap<String, LinkedHashMap<String,Double>> purchased_by_user) {
        Log.d(TAG, purchased_by_user.toString());
        this.purchased_by_user = new SimpleData(purchased_by_user);
        Log.e(TAG, this.purchased_by_user.toString());
    }

    /**
     * The initializer for a ListContainer to inflate onto the Activity.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public ListContainer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View container = LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_purchased_by_user, parent,false);
        ListContainer list = new ListContainer(container);
        listContainers.add(list);
        return list;
    }

    /**
     * Binds the SimpleData objects data to the ListContainer objects UI and recyclerview
     * data.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ListContainer holder, int position) {
        holder.bind(purchased_by_user.getTitle(position), purchased_by_user.getList(position));
    }


    /**
     * Gets the size of the SimpleData structure
     * determined by the number of parent keys.
     * @return int number of parent keys
     */
    @Override
    public int getItemCount() {
        return purchased_by_user.getSize();
    }

    /**
     * A Class made to represent a View that has an outer title + list recycler view
     * and nested recycler views for the lists from each key parent.
     */
    public class ListContainer extends RecyclerView.ViewHolder {
        private TextView title; // the title of the child list
        private RecyclerView inner_list; // the recycler view for the inner child data
        private PurchasedListAdapter adapter; // the adapter for the inner child view

        /**
         * Constructor that gets all the xml resources
         * necessary to create this view and sts the linear layout manager for the outer recyclerview.
         * @param itemView the layout xml reference that contains all the views for an ItemContainer.
         */
        public ListContainer(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.purchased_title);
            inner_list = itemView.findViewById(R.id.purchased_list);
            inner_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        /**
         * Inserts the title's name into the outer recycler view and makes
         * a new instance of a PurchasedListAdapter for the inner recyclerview using the
         * inner child list for the members .
         * @param title the child list's title
         * @param list the list of items
         */
        public void bind(String title, LinkedHashMap<String,Double> list) {
            this.title.setText(title);
            adapter = new PurchasedListAdapter(list);
            inner_list.setAdapter(adapter);
        }

    }


}
