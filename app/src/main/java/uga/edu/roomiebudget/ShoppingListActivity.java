package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedHashMap;

/**
 * The activity class for the shopping list.
 */
public class ShoppingListActivity extends AppCompatActivity {

    private Button addItem;
    private EditText item_entry;
    private HousingDataBaseManager hdb;
    private ListItemAdapter list_adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView groupListTitle;
    private Intent intent;

    /**
     * Creates the shopping list activity to display the groups shopping list to the user.
     * Also handles adding items to the shopping list.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        hdb = new HousingDataBaseManager(this);

        if (intent.getStringExtra("user") != null && hdb.getUser()[0] == null) {
            Log.d(TAG, "USER GOT AND PREF GOT");
            hdb.storeUser(intent.getStringExtra("user"), new HousingDataBaseManager.FireBaseDataCallback() {
                @Override
                public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

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
        groupListTitle = findViewById(R.id.listTitle);
        groupListTitle.setText(hdb.getUser()[0] + "'s Shopping List");

        hdb.getItems(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
            @Override
            public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

            }

            @Override
            public void onCalculationsReceived(LinkedHashMap<String,Double> data) {

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
            /**
             * The on click method used to add an item to the shopping list.
             * @param view The button which was clicked.
             */
            @Override
            public void onClick(View view) {
                String text = item_entry.getText().toString();
                hdb.addItem(hdb.getUser()[0], text);
                hdb.getItems(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
                    @Override
                    public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

                    }

                    @Override
                    public void onCalculationsReceived(LinkedHashMap<String,Double> data) {

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

    /**
     * The method to inflate the nav bar.
     * @param menu The menu to be used for the nav bar.
     * @return boolean stating whether or not the nav bar was inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_bar, menu);
        MenuItem shopList = menu.findItem(R.id.shopListItem);
        shopList.setVisible(false);
        return true;
    }

    /**
     * Sets up the correct actions for the nav bar options.
     * Starts the activity corresponding to the option selected.
     * The options are navigating the the purchased list or logging out of the app.
     * @param item The option which was selected.
     * @return boolean stating if the action was correctly carried out.
     */
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
                HousingDataBaseManager.clearAppData(this);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}