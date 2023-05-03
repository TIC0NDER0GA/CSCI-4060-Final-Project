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
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The activity class for the purchased list.
 */
public class PurchasedListActivity extends AppCompatActivity {

    private HousingDataBaseManager hdb;
    private PurchaseByUserAdapter purchased_list_adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Button settleCosts;
    private TextView groupListTitle;

    /**
     * Creates the purchased list activity to display the groups purchased list to the user.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
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
        groupListTitle = findViewById(R.id.listTitle2);
        groupListTitle.setText(hdb.getUser()[0] + "'s Purchased List");
        hdb.getRoomatesPurchased(hdb.getUser()[0], new HousingDataBaseManager.FireBaseDataCallback() {

            /**
             * Implements the interface for a callback
             * letting the adapter know the data is ready.
             * @param data the seperate lists of purchases by each roomate
             */
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
                HousingDataBaseManager.clearAppData(this);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Button listener class used to settle the costs.
     */
    private class ButtonClickListener implements View.OnClickListener {

        /**
         * On click method to start the settle costs activity.
         * @param view
         */
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), SettleCostsActivity.class);
            startActivity(intent);
        }
    }

}