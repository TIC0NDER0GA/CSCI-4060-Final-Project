package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.util.Log;
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

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemHolder> {


    private List<Map.Entry<String, String>> item_list;


    public ListItemAdapter(LinkedHashMap<String, String> item_list) {
        this.item_list = new ArrayList<>(item_list.entrySet());
        // Log.e(TAG, this.item_list.toString());
    }



    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_items, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(item_list.get(position).getKey(), this, position);
    }


    @Override
    public int getItemCount() {
        // Log.e(TAG, String.valueOf(item_list.size()));
        return item_list.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        private TextView fb_item;
        private Button remove;
        private Button purchase;
        private HousingDataBaseManager hbd;
        private ListItemAdapter lia;

        private View view;

        private int position;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            fb_item = (TextView) itemView.findViewById(R.id.fb_item);
            purchase = (Button) itemView.findViewById(R.id.purchaseButton);
            remove = (Button) itemView.findViewById(R.id.removeButton);
            view = itemView;
            hbd = new HousingDataBaseManager(itemView.getContext());
        }

        public void bind(String item, ListItemAdapter li, int pos) {
            fb_item.setText(item);
            purchase.setOnClickListener(this::onButtonShowPopupWindowClick);
            lia = li;
            position = pos;
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    hbd.removeItem(fb_item.getText().toString(), hbd.getUser()[0], new HousingDataBaseManager.DeleteCallback() {
                        @Override
                        public void itemDeleted() {
                            // Log.e(TAG, "DELETE");
                            li.item_list.remove(pos);
                            li.notifyDataSetChanged();
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

        }






        public void onButtonShowPopupWindowClick(View view) {
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
                @Override
                public void onClick(View view) {
                    try {
                        Double price = Double.parseDouble(priceET.getText().toString());
                        hbd.purchasedItem(group, name, itemName, price);
                        hbd.removeItem(itemName, group, new HousingDataBaseManager.DeleteCallback() {
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
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        }

    }



}
