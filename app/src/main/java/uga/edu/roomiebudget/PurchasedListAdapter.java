package uga.edu.roomiebudget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * List item adapter class to display each individual item on the purchased list using the correct
 * layout for purchased list items.
 */
public class PurchasedListAdapter extends RecyclerView.Adapter<PurchasedListAdapter.PurchasedItemHolder> {
    private List<Map.Entry<String, Double>> purchased_list;

    /**
     * Constructor for a list item adapter object.
     * Sets up the list of items.
     * @param purchased_list
     */
    public PurchasedListAdapter(LinkedHashMap<String, Double> purchased_list) {
        this.purchased_list = new ArrayList<>(purchased_list.entrySet());
    }

    /**
     * Method to create a view holder to inflate the the correct layout for the item on the
     * shopping list.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return The new view to be used for the purchased list.
     */
    @NonNull
    @Override
    public PurchasedItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View purchased = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchased_list_items, parent, false);
        return new PurchasedItemHolder(purchased);
    }

    /**
     * The method to bind the item list to the view to correctly display the items.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PurchasedItemHolder holder, int position) {
        holder.bind(purchased_list.get(position).getKey(), purchased_list.get(position).getValue(), this, position);
    }

    /**
     * Method to get the size of the list of items.
     * @return The size of the shopping list.
     */
    @Override
    public int getItemCount() {
        return purchased_list.size();
    }

    /**
     * PurchasedItemHolder class to create the view/layout for the purchased list item.
     */
    public class PurchasedItemHolder extends RecyclerView.ViewHolder {
        private TextView purchased_name;
        private TextView purchased_price;
        private Button editButton;
        private HousingDataBaseManager hbd;
        private int position;
        PurchasedListAdapter pli;

        /**
         * Constructor for an ItemHolder object containing the text views and buttons corresponding
         * to the correct list item for the layout.
         * @param itemView The itemView corresponding to view for the list item.
         */
        public PurchasedItemHolder(@NonNull View itemView) {
            super(itemView);
            purchased_name = (TextView)  itemView.findViewById(R.id.purchased_name);
            purchased_price = (TextView) itemView.findViewById(R.id.purchased_price);
            editButton = (Button) itemView.findViewById(R.id.editButton);
            hbd = new HousingDataBaseManager(itemView.getContext());
        }

        /**
         * Method to bind the list item to the ListItemAdapter in order to access the correct views.
         * @param User The user currently logged in, using the app.
         * @param price The price of the current item being accessed.
         * @param pl The adapter for the corresponding list item.
         * @param pos The index position of the current list item.
         */
        public void bind(String User, Double price, PurchasedListAdapter pl, int pos) {
            purchased_name.setText(User);
            purchased_price.setText(String.valueOf(price));
            editButton.setOnClickListener(this::onButtonShowPopupWindowClick);
            pli = pl;
            position = pos;
            // onclick methods, similar to list item adapter
        }

        /**
         * Listener for the edit button which creates a popup.
         * The popup is inflated and then allows the user to input a different price for or
         * remove the corresponding list item.
         * @param view The button which was clicked.
         */
        public void onButtonShowPopupWindowClick(View view) {
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_edit_item, null);
            TextView itemLabel = popupView.findViewById(R.id.itemName);

            String itemName = purchased_name.getText().toString();
            itemLabel.setText(itemName);

            EditText priceET = popupView.findViewById(R.id.priceChange);
            Button saveButton = popupView.findViewById(R.id.saveButton2);
            Button removeButton = popupView.findViewById(R.id.removeButton3);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.showAtLocation(itemView.getRootView(), Gravity.CENTER, 0, 0);

            String[] userData = hbd.getUser();
            String group = userData[0];
            String name = userData[1];

            saveButton.setOnClickListener(new View.OnClickListener() {

                /**
                 * On click method for the save button to edit the corresponding list item. When
                 * clicked the price of the item is changed to what the user has inputted or an
                 * error message is displayed if the price is not inputted correctly.
                 * The popup method is then dismissed.
                 * @param view The button which was pressed.
                 */
                @Override
                public void onClick(View view) {
                    try {
                        Double price = Double.parseDouble(priceET.getText().toString());
                        hbd.purchasedItem(hbd.getUser()[0],hbd.getUser()[1], purchased_name.getText().toString(), price);
                        pli.purchased_list.set(position, new Map.Entry<String, Double>() {
                            @Override
                            public String getKey() {
                                return purchased_name.getText().toString();
                            }

                            @Override
                            public Double getValue() {
                                return price;
                            }

                            @Override
                            public Double setValue(Double aDouble) {
                                return null;
                            }
                        });
                        pli.notifyDataSetChanged();
                        popupWindow.dismiss();
                    } catch (NumberFormatException nfe) {
                        Toast.makeText(view.getContext(), "Enter a price", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            removeButton.setOnClickListener(new View.OnClickListener() {

                /**
                 * On click method for the remove button. When clicked the corresponding item is
                 * removed from the purchased list and moved to the shopping list.
                 * @param view The button which was clicked.
                 */
                @Override
                public void onClick(View view) {
                    hbd.removePurchased(hbd.getUser()[0],itemName, new HousingDataBaseManager.DeleteCallback() {
                        @Override
                        public void itemDeleted() {

                        }

                        @Override
                        public void purchasedCleared() {

                        }

                        /**
                         * Method to remove the method from the shopping list.
                         * The popup method is then dismissed.
                         */
                        @Override
                        public void purchasedDeleted() {
                            hbd.addItem(hbd.getUser()[0], itemName);
                            hbd.removePurchasedUser(hbd.getUser()[1],hbd.getUser()[0],itemName);
                            if (pli.purchased_list.size() > 0) {
                                pli.purchased_list.remove(position);
                                pli.notifyDataSetChanged();
                            }
                            popupWindow.dismiss();
                        }
                    });
                }
            });



            popupView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        }

    }
}
