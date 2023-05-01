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
        holder.bind(purchased_list.get(position).getKey(), purchased_list.get(position).getValue(), this, position);
    }


    @Override
    public int getItemCount() {
        return purchased_list.size();
    }



    public class PurchasedItemHolder extends RecyclerView.ViewHolder {
        private TextView purchased_name;
        private TextView purchased_price;
        private Button editButton;
        private HousingDataBaseManager hbd;
        private int position;

        PurchasedListAdapter pli;
        public PurchasedItemHolder(@NonNull View itemView) {
            super(itemView);
            purchased_name = (TextView)  itemView.findViewById(R.id.purchased_name);
            purchased_price = (TextView) itemView.findViewById(R.id.purchased_price);
            editButton = (Button) itemView.findViewById(R.id.editButton);
            hbd = new HousingDataBaseManager(itemView.getContext());
        }

        public void bind(String User, Double price, PurchasedListAdapter pl, int pos) {
            purchased_name.setText(User);
            purchased_price.setText(String.valueOf(price));
            editButton.setOnClickListener(this::onButtonShowPopupWindowClick);
            pli = pl;
            position = pos;
            // onclick methods, similar to list item adapter
        }

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
                @Override
                public void onClick(View view) {
                    hbd.removePurchased(hbd.getUser()[2],hbd.getUser()[0],itemName, new HousingDataBaseManager.DeleteCallback() {
                        @Override
                        public void itemDeleted() {

                        }

                        @Override
                        public void purchasedCleared() {

                        }

                        @Override
                        public void purchasedDeleted() {
                            hbd.addItem(hbd.getUser()[0], itemName);
                            hbd.removePurchasedUser(hbd.getUser()[2],hbd.getUser()[0],itemName);
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
