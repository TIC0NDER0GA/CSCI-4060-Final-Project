package uga.edu.roomiebudget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity class for the settle costs activity which displays information about how much each user
 * has spent and the average cost per user.
 */
public class SettleCostsActivity extends AppCompatActivity {

    private HousingDataBaseManager hdb;
    private LinearLayoutManager llm;
    private CostsAdapter costsAdapter;
    private RecyclerView recyclerView;

    /**
     * The method to create the SettleCostsActivity and the correct layout.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_costs);
        hdb = new HousingDataBaseManager(this);
        recyclerView = findViewById(R.id.cost_recycler);
        llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        hdb.getRoomatesPurchasedCosts(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {
            @Override
            public void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data) {

            }

            /**
             * Method to set the cost adapter which helps display the correct information.
             * Then clears the purchased list.
             * @param data The data to be used to calculate the cost information.
             */
            @Override
            public void onCalculationsReceived(LinkedHashMap<String, Double> data) {
                costsAdapter = new CostsAdapter(data);
                recyclerView.setAdapter(costsAdapter);
                costsAdapter.notifyDataSetChanged();
                hdb.clearAllPurchasedData(hdb.getUser()[0]);
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

    /**
     * The method to inflate the nav bar.
     * @param menu The menu to be used for the nav bar.
     * @return boolean stating whether or not the nav bar was inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_bar, menu);
        MenuItem purchList = menu.findItem(R.id.purchListItem);
        purchList.setVisible(false);
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
}