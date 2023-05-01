package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PurchasedListActivity extends AppCompatActivity {


    private HousingDataBaseManager hdb;
    private PurchaseByUserAdapter purchased_list_adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Button settleCosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_list);
        settleCosts = findViewById(R.id.settleCostButton);
        settleCosts.setOnClickListener(new ButtonClickListener());
        recyclerView = (RecyclerView) findViewById(R.id.purchased_by_user_recycler);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        hdb = new HousingDataBaseManager(this);
        hdb.getRoomatesPurchased(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
            @Override
            public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {
                purchased_list_adapter = new PurchaseByUserAdapter(data);
                Log.d(TAG, data.toString());
                recyclerView.setAdapter(purchased_list_adapter);
                purchased_list_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCalculationsReceived(LinkedHashMap<String,Double> data) {

            }


            @Override
            public void onItemsDataReceived(LinkedHashMap<String, String> data) {

            }

            @Override
            public void onPurchasedDataRecieved(LinkedHashMap<String, Double> data) {

            }

            @Override
            public void onLogin(String[] data) {

            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_bar, menu);
        MenuItem purchList = menu.findItem(R.id.purchListItem);
        purchList.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.shopListItem:
                intent = new Intent(this, ShoppingListActivity.class);
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
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_item, null);

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

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), SettleCostsActivity.class);

            startActivity(intent);
        }
    }

}