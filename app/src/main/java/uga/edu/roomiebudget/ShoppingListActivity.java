package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.LinkedHashMap;

public class ShoppingListActivity extends AppCompatActivity {

    private Button addItem;
    private EditText item_entry;
    private HousingDataBaseManager hdb;
    private ListItemAdapter list_adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        hdb = new HousingDataBaseManager(this);

        if (intent.getStringExtra("user") != null && hdb.getUser() == null) {
            hdb.storeUser(intent.getStringExtra("user"), new HousingDataBaseManager.FireBaseDataCallback() {
                @Override
                public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

                }

                @Override
                public void onItemsDataReceived(LinkedHashMap<String, String> data) {

                }

                @Override
                public void onPurchasedDataRecieved(LinkedHashMap<String, Double> data) {

                }

                @Override
                public void onLogin(String[] data) {
                    hdb.savePref(data[2],data[1],data[0]);
                }
            });
        }


        setContentView(R.layout.activity_shopping_list);
        addItem = findViewById(R.id.addButton);
        recyclerView = (RecyclerView) findViewById(R.id.shopping_list_recycler);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        item_entry = (EditText) findViewById(R.id.editTextTextPersonName2);
        hdb.getItems(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
            @Override
            public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

            }

            @Override
            public void onItemsDataReceived(LinkedHashMap<String, String> data) {
                list_adapter = new ListItemAdapter(data);
                Log.d(TAG, data.toString());
                recyclerView.setAdapter(list_adapter);
                list_adapter.notifyDataSetChanged();
            }

            @Override
            public void onPurchasedDataRecieved(LinkedHashMap<String, Double> data) {

            }

            @Override
            public void onLogin(String[] data) {

            }
        });

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = item_entry.getText().toString();
                hdb.addItem(hdb.getUser()[0], text);
                hdb.getItems(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
                    @Override
                    public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

                    }

                    @Override
                    public void onItemsDataReceived(LinkedHashMap<String, String> data) {
                        list_adapter = new ListItemAdapter(data);
                        Log.d(TAG, data.toString());
                        recyclerView.setAdapter(list_adapter);
                        list_adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onPurchasedDataRecieved(LinkedHashMap<String, Double> data) {

                    }

                    @Override
                    public void onLogin(String[] data) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_bar, menu);
        MenuItem shopList = menu.findItem(R.id.shopListItem);
        shopList.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.purchListItem:
                intent = new Intent(this, PurchasedListActivity.class);
                startActivity(intent);
                return true;
            case R.id.logoutItem:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_purchase, null);

        TextView nameView = findViewById(R.id.fb_item);
        String item = nameView.getText().toString();

        TextView itemLabel = popupView.findViewById(R.id.popupItemName);
        itemLabel.setText(item);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void removeItemOnClick(View view) {
        Log.d(TAG, "Need to finish method for this");
    }

    public void purchaseItemOnClick(View view) {
        Log.d(TAG, "Need to finish method for this");
    }

}