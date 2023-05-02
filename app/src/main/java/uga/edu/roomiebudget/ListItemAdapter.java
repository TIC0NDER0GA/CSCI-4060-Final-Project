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
 * List item adapter class to display each individual item on the shopping list using the correct
 * layout for shopping list items.
 */
public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemHolder> {
    private List<Map.Entry<String, String>> item_list;

    /**
     * Constructor for a list item adapter object.
     * Sets up the list of items.
     * @param item_list Hashmap of items.
     */
    public ListItemAdapter(LinkedHashMap<String, String> item_list) {
        this.item_list = new ArrayList<>(item_list.entrySet());
    }

    /**
     * Method to create a view holder to inflate the the correct layout for the item on the
     * shopping list.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return The new view to be used for the shopping list.
     */
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_items, parent, false);
        return new ItemHolder(itemView);
    }

    /**
     * The method to bind the item list to the view to correctly display the items.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(item_list.get(position).getKey(), this, position);
    }

    /**
     * Method to get the size of the list of items.
     * @return The size of the shopping list.
     */
    @Override
    public int getItemCount() {
        return item_list.size();
    }

    /**
     * ItemHolder class to create the view/layout for the shopping list item.
     */
    public static class ItemHolder extends RecyclerView.ViewHolder {
        private TextView fb_item;
        private Button remove;
        private Button editName;
        private Button purchase;
        private HousingDataBaseManager hbd;
        private ListItemAdapter lia;
        private View view;
        private int position;

        /**
         * Constructor for an ItemHolder object containing the text views and buttons corresponding
         * to the correct list item for the layout.
         * @param itemView The itemView corresponding to view for the list item.
         */
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            fb_item = (TextView) itemView.findViewById(R.id.fb_item);
            purchase = (Button) itemView.findViewById(R.id.purchaseButton);
            editName = (Button) itemView.findViewById(R.id.editNameButton);
            view = itemView;
            hbd = new HousingDataBaseManager(itemView.getContext());
        }

        /**
         * Method to bind the list item to the ListItemAdapter in order to access the correct views.
         * @param item List item currently accessed.
         * @param li ListItemAdapter corresponding to the list item.
         * @param pos The index position of the list item.
         */
        public void bind(String item, ListItemAdapter li, int pos) {
            lia = li;
            position = pos;
            fb_item.setText(item);
            purchase.setOnClickListener(this::onButtonShowPurchasePopupClick);
            editName.setOnClickListener(this::onButtonShowEditPopupClick);
        }

        /**
         * Listener for the purchase button which creates a popup.
         * The popup is inflated and then allows the user to input a price for the corresponding
         * list item.
         * @param view The button which was clicked.
         */
        public void onButtonShowPurchasePopupClick(View view) {
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_purchase, null);
            TextView itemLabel = popupView.findViewById(R.id.popupItemName);
            String itemName = fb_item.getText().toString();
            itemLabel.setText(itemName);
            EditText priceET = popupView.findViewById(R.id.priceEditText);

            Button purchaseButton = popupView.findViewById(R.id.popupPurchButton);
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.showAtLocation(itemView.getRootView(), Gravity.CENTER, 0, 0);

            String[] userData = hbd.getUser();
            String group = userData[0];
            String name = userData[1];

            purchaseButton.setOnClickListener(new View.OnClickListener() {

                /**
                 * On click method for the purchase button. It moves the shopping list item to the
                 * purchased item list with the price inputted by the user. It also removed the item
                 * from the shopping list.
                 * The popup method is then dismissed.
                 * @param view The button which was pressed.
                 */
                @Override
                public void onClick(View view) {
                    try {
                        Double price = Double.parseDouble(priceET.getText().toString());
                        hbd.purchasedItem(group, name, itemName, price);
                        hbd.removeItem(itemName, group, new HousingDataBaseManager.DeleteCallback() {

                            /**
                             * Method to delete the list item from the shopping list.
                             */
                            @Override
                            public void itemDeleted() {
                                lia.item_list.remove(position);
                                lia.notifyDataSetChanged();
                            }

                            @Override
                            public void purchasedCleared() {

                            }

                            @Override
                            public void purchasedDeleted() {

                            }
                        });
                        popupWindow.dismiss();
                    } catch (NumberFormatException nfe) {
                        Toast.makeText(view.getContext(), "Enter a price", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            popupView.setOnTouchListener(new View.OnTouchListener() {

                /**
                 * Method to dismiss the popup when the user click off to the side.
                 * @param v The view which was clicked.
                 * @param event The click event from the user.
                 * @return boolean stating whether or not the method was completed.
                 */
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        }

        /**
         * Listener for the purchase button which creates a popup.
         * The popup is inflated and then allows the user to edit the corresponding
         * list item.
         * The user can edit the name of the item or remove the item from the shopping list.
         * @param view The button which was clicked.
         */
    public void onButtonShowEditPopupClick(View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_item_name, null);

        String itemName = fb_item.getText().toString();
        EditText itemNameET = popupView.findViewById(R.id.itemName);
        itemNameET.setText(itemName);

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
             * The on click method for the save button.
             * The new name that the user has inputted is saved as the name for the item.
             * The popup method is then dismissed.
             * @param view The button that was clicked.
             */
            @Override
            public void onClick(View view) {
                try {
                    hbd.addItem(group, itemName);
                    hbd.removeItem(itemName, group, new HousingDataBaseManager.DeleteCallback() {

                        /**
                         * The method to delete and then resave the button with the new name.
                         */
                        @Override
                        public void itemDeleted() {
                            lia.item_list.remove(position);
                            lia.item_list.add(new Map.Entry<String, String>() {
                                @Override
                                public String getKey() {
                                    return itemNameET.getText().toString();
                                }

                                @Override
                                public String getValue() {
                                    return "";
                                }

                                @Override
                                public String setValue(String s) {
                                    return null;
                                }
                            });
                            lia.notifyDataSetChanged();
                            hbd.addItem(group, itemNameET.getText().toString());
                        }

                        @Override
                        public void purchasedCleared() {
                        }

                        @Override
                        public void purchasedDeleted() {
                        }
                    });
                    popupWindow.dismiss();
                } catch (NumberFormatException nfe) {

                }
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {

            /**
             * The on click method for the remove button.
             * The corresponding list item is removed from the shopping list.
             * @param view The button which was clicked.
             */
            @Override
            public void onClick(View view) {
                hbd.removeItem(fb_item.getText().toString(), hbd.getUser()[0], new HousingDataBaseManager.DeleteCallback() {

                    /**
                     * The method to remove the item from the shopping list.
                     */
                    @Override
                    public void itemDeleted() {
                        // Log.e(TAG, "DELETE");
                        lia.item_list.remove(position);
                        lia.notifyDataSetChanged();
                    }

                    @Override
                    public void purchasedCleared() {

                    }

                    @Override
                    public void purchasedDeleted() {

                    }
                });
            }
        });


        // finish this for removing from shopping list
        popupView.setOnTouchListener(new View.OnTouchListener() {

            /**
             * Method to dismiss the popup when the user click off to the side.
             * @param v The view which was clicked.
             * @param event The click event from the user.
             * @return boolean stating whether or not the method was completed.
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}



}
