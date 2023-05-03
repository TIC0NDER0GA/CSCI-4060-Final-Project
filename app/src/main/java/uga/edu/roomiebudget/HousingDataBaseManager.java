package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class to manage the database and deal with accessing data corresponding to the groups, users
 * and list items/purchases.
 */
public class HousingDataBaseManager {
    private FirebaseDatabase fdb = FirebaseDatabase.getInstance("https://roomiebudget-default-rtdb.firebaseio.com"); // the entry point of the App's Firebase Database
    private DatabaseReference fRef; // Database reference for use to deal with group info
    private DatabaseReference uRef; // Database reference for use to deal with grab user info

    private final static String DATABASE_ENTRY = "/dorms"; // path to groups part of database
    private final static String DATABASE_USERS = "/users"; // path to users part of database

    private Context context; // the parent activity context for objects and methods that depend on it

    private FirebaseAuth fAuth; // the firebase authentication object

    private Intent intent; // used to change activities from the utility class as needed

    private final String USER_PREF = "preferences"; // name for the preferences file

    private SharedPreferences preferences; // a global app wide var that can retrieve the users information when needed
    private SharedPreferences.Editor editor; // Edits the file that stores user preferences
    private LinkedHashMap<String,String> item_list; // stores a groups shopping list
    private LinkedHashMap<String,Double> purchased_list; // stores a groups purchased data
    private MasterKey masterKey; // the master key for getting sensitive data
    private SharedPreferences privSharedPreferences; // the user's login that are available only when provided with a master key
    private SharedPreferences pubSharedPreferences; // the user's preferences that are available only for the app to see


    /**
     * Constructor for a HousingDataBaseManager object
     * @param parent The context of the parent activity.
     */
    public  HousingDataBaseManager(Context parent) {
        context = parent;
        fAuth = FirebaseAuth.getInstance();
        preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Method to add a user to the database.
     * @param email The user's email.
     * @param user The user's group.
     * @param fullName The user's name.
     */
    public void addUser(String email, String user, String fullName) {
        uRef = fdb.getReference(DATABASE_USERS);
        uRef = fdb.getReference(DATABASE_USERS + "/" + parseEmail(email.toLowerCase(Locale.ENGLISH)));
        uRef.child("group").setValue(user);
        uRef.child("full_name").setValue(parseEmail(email));
        uRef.child("email").setValue(email);
    }

    /**
     * Method to parse the users email to retrieve their username.
     * @param email The user's email.
     * @return The username retrieved from the parsed email.
     */
    public String parseEmail(String email) {
        email = email.substring(0, email.indexOf('@'));
        return email;
    }

    /**
     * Method to sign a user in to the app.
     * @param email The user's email.
     * @param password The user's password.
     */
    public void signinUser(String email, String password) {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                /**
                 * Method to encrypt the login info, catch any exceptions, and then start the
                 * the shopping list activity.
                 * @param task
                 */
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try {
                            encryptLogin(email, password);
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        intent = new Intent(context, ShoppingListActivity.class);
                        Log.d(TAG, parseEmail(email));
                        intent.putExtra("user", parseEmail(email));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Invalid Login", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /**
     * Method to create a new group in the database.
     * @param group The group name to be used.
     * @throws Exception GroupException thrown if the group already exists.
     */
    public void createGroup(String group) throws Exception{
        fRef = fdb.getReference(DATABASE_ENTRY);
        fRef.child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {

            /**
             * Method to check if adding the group is successful or if the group already exists.
             * @param task
             */
            @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
                                Log.d(TAG, "Added email to group");
                                fRef.child("Item_list").setValue("");
                                Log.d(TAG, "Added Item_list to group");
                                fRef.child("Recently_purchased").setValue("");
                                Log.d(TAG, "Added Recently_purchased to group");
                            } else {
                                try {
                                    throw new GroupException("Group Already Exists");
                                } catch (GroupException e) {
                                    Toast.makeText(context, "Group already exists", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
    }

    /**
     * Method to add an item to the shopping list database.
     * @param group Group the item is added to.
     * @param item The name of the item to be added.
     */
    public void addItem(String group, String item) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");
        fRef.child(item).setValue("");
    }

    /**
     * Method to add an item to the purchased list in the database
     * @param group The group the item is within.
     * @param member The user purchasing the item.
     * @param item The item to be added to the purchased list.
     * @param price The price of the item to be saved with the name of the item.
     */
    public void purchasedItem(String group, String member, String item, Double price) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + member);
        fRef.child(item).setValue(price);
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");
        fRef.child(item).setValue(price);
    }

    /**
     * Method to retrieve the shopping list of items.
     * @param group The group to get the list from.
     * @param callback The callback to the database.
     */
    public void getItems(String group, FireBaseDataCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");
        fRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Method to call each time the data is changed.
             * @param snapshot The current data at the location.
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                item_list = new LinkedHashMap<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    String value = (String) childSnapshot.getValue();
                    item_list.put(key, value);
                }
                callback.onItemsDataReceived(item_list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Method to get the list of purchased items.
     * This was a test method before assigning each
     * user their own list.
     * @param group The group to get the list from.
     * @param callback The callback to the database.
     */
    public void getPurchased(String group, FireBaseDataCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Method called each time the data is changed.
             * @param snapshot The current data at the location.
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                purchased_list = new LinkedHashMap<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    String value = (String) String.valueOf(childSnapshot.getValue());
                    Double price = Double.valueOf(value);
                    purchased_list.put(key, price);
                }
                callback.onPurchasedDataRecieved(purchased_list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Method to add a new user to the database which is being added to an existing group.
     * @param email The user's email.
     * @param group The user's group to join.
     * @param name The user's name.
     * @param password The user's password.
     */
    public void createUserWithGroup(String email, String group, String name, String password) {
        //  Log.e(TAG, "GROUP: " + group);
            fAuth.createUserWithEmailAndPassword(email.toLowerCase(Locale.ENGLISH), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        /**
                         * Method to check if the group exists.
                         * After the user signs up correctly, the user is sent back to the home page.
                         * @param task Async task for registering
                         */
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    createGroup(group);
                                    savePref(group, name, email);
                                    addUser(email, group, name);
                                    Log.d(TAG, "USER with email: " + email + "and password: " + password + "created.");
                                    intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "USER creation fail.");
                                Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    /**
     * Method to add a new user to the database who is creating/joining a new group.
     * @param email User's email.
     * @param group The new group the user is creating.
     * @param name User's name.
     * @param password User's password.
     */
    public void createUserWithoutGroup(String email, String group, String name, String password) {
        fAuth.createUserWithEmailAndPassword(email.toLowerCase(Locale.ENGLISH), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    /**
                     * Method to add the user and check to make sure it was done correctly.
                     * After the user signs up correctly, the user is sent back to the home page.
                     * @param task Async task for registering
                     */
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                savePref(group, name, email);
                                addUser(email, group, name);
                                Log.d(TAG, "USER with email: " + email + "and password: " + password + "created.");
                                intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "USER creation fail.");
                            Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    /**
     * Method to retrieve the items from the purchased list for a specific group.
     * @param group The group to get the purchased list from.
     * @param callback The callback to the database.
     */
    public void getRoomatesPurchased(String group, FireBaseDataCallback callback) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> data = new LinkedHashMap<>();
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            LinkedHashMap<String, Double> innerdata;

            /**
             * Method to be used whenever data is changed in the database.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if ( userId.equals("Item_list") || userId.equals("Recently_purchased")) {
                        continue;
                    }
                    LinkedHashMap<String, Double> innerData = new LinkedHashMap<>();

                    for (DataSnapshot itemSnapshot : userSnapshot.getChildren()) {
                        String itemName = itemSnapshot.getKey();
                        Object value = itemSnapshot.getValue();

                        if (value instanceof Double) {
                            innerData.put(itemName, (Double) value);
                        } else if (value instanceof Long) {
                            String valLong = String.valueOf(value);
                            Double doubleValue = Double.parseDouble(valLong);
                            innerData.put(itemName, doubleValue);
                        }
                    }
                    data.put(userId, innerData);
                }
                callback.onRoomatesPurchasedDataReceived(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Method to retrieve the costs for the items on the purchased list for a specific group.
     * @param group The group to get the costs from.
     * @param callback The callback to the database.
     */
    public void getRoomatesPurchasedCosts(String group, FireBaseDataCallback callback) {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Method to be used whenever data is changed in the database.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalSpent = 0;
                double totalUsers = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId.equals("Item_list") || userId.equals("Recently_purchased")) {
                        continue;
                    }
                    double userTotal = 0;
                    for (DataSnapshot itemSnapshot : userSnapshot.getChildren()) {
                        Object value = itemSnapshot.getValue();
                        if (value instanceof Double) {
                            userTotal += (Double) value;
                            totalSpent += (Double) value;
                        } else if (value instanceof Long) {
                            String valLong = String.valueOf(value);
                            Double doubleValue = Double.parseDouble(valLong);
                            userTotal += doubleValue;
                            totalSpent += doubleValue;
                        }
                    }
                    data.put(userId, userTotal);
                    totalUsers++;
                }
                double average = totalSpent / totalUsers;
                data.put("Average cost per roomate", average);
                data.put("Total spent by group", totalSpent);
                callback.onCalculationsReceived(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle cancellation
            }
        });
    }

    /**
     * Method to encrypt a user's login information.
     * @param email User's email.
     * @param password User's password.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private void encryptLogin(String email, String password) throws GeneralSecurityException, IOException {
        masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        privSharedPreferences = EncryptedSharedPreferences.create(context, "user_cred", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        privSharedPreferences.edit().putString("email", email).apply();
        privSharedPreferences.edit().putString("password",password).apply();
    }

    /**
     * Method to save the user's information.
     * @param group The group the user is in.
     * @param name User's name.
     * @param email User's email.
     */
    public void savePref(String group, String name, String email) {
        pubSharedPreferences = context.getSharedPreferences("user_pref",Context.MODE_PRIVATE);
        editor = pubSharedPreferences.edit();
        editor.putString("group", group).apply();
        editor.putString("name", name).apply();
        editor.putString("email", email).apply();
    }

    /**
     * Method to get the user's login information.
     * @return Array containing the user's email & password.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public String[] getCred() throws GeneralSecurityException, IOException {
        String[] userdata = new String[2];
        masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        privSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "user_cred",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        userdata[0] = privSharedPreferences.getString("email", null);
        userdata[1] = privSharedPreferences.getString("password", null);
        return userdata;
    }

    /**
     * Method to retrieve a user from the database.
     * @return Array containing the group the user is in, user's name, and user's email.
     */
    public String[] getUser() {
        String[] userdata = new String[3];
        pubSharedPreferences = context.getSharedPreferences("user_pref",Context.MODE_PRIVATE);
        userdata[0] = pubSharedPreferences.getString("group", null);
        userdata[1] = pubSharedPreferences.getString("name", null);
        userdata[2] = pubSharedPreferences.getString("email", null);
        return userdata;
    }

    /**
     * Method to store a user's information in the database.
     * @param user The user's name.
     * @param callback The callback to the database.
     */
    public void storeUser(String user, FireBaseDataCallback callback) {
        uRef = fdb.getReference(DATABASE_USERS + "/" + user);
        String[] userStuff = new String[3];
        uRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Method to be used whenever data is changed in the database.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds: snapshot.getChildren()) {
                    userStuff[i] = (String) ds.getValue();
                    Log.d(TAG, userStuff[i]);
                    Log.d(TAG, "BRUH");
                    i++;
                }
                callback.onLogin(userStuff);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Method to clear the apps data when a user logs out.
     * @param context The current context of the app.
     */
    public static void clearAppData(Context context) {
        try {
            // Get the package manager and activity manager
            PackageManager pm = context.getPackageManager();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.clearApplicationUserData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to delete an item from the shopping list.
     * @param item Item to be deleted.
     * @param group Group to get the list from.
     * @param callback The callback to the database.
     */
    public void removeItem(String item, String group, DeleteCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list").child(item);
        Log.e(TAG, group);
        fRef.removeValue(new DatabaseReference.CompletionListener() {

            /**
             * Method to check if the item is deleted from the database.
             * @param error A description of any errors that occurred or null on success
             * @param ref A reference to the specified Firebase Database location
             */
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Notify the callback that the data has changed
                    if (callback != null) {
                        callback.itemDeleted();
                        Log.e(TAG, callback.toString());
                    }

                }
            }
        });
    }

    /**
     * Method to delete an item from the purchased list.
     * @param group The group to get the list from.
     * @param item The item to be deleted.
     * @param callback The callback to the database.
     */
    public void removePurchased( String group, String item, DeleteCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased").child(item);
        fRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Notify the callback that the data has changed
                    if (callback != null) {
                        callback.purchasedDeleted();
                    }
                }
            }
        });
    }

    /**
     * Method to remove an item from a user's list of purchased items.
     * @param user The user that purchased the item.
     * @param group The group the user is in.
     * @param item The item to be removed.
     */
    public void removePurchasedUser(String user, String group, String item) {
        uRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + user).child(item);
        uRef.removeValue();
    }

    /**
     * Method to clear the purchased items list and the purchased item lists for each user.
     * @param group The group to delete the purchased items from.
     */
    public void clearAllPurchasedData(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY).child(group);

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Method used whenever data in the database is changed.
             * @param snapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // iterate over the child nodes of the group node
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String childKey = childSnapshot.getKey();
                    // skip the Item_list node
                    if (childKey.equals("Item_list")) {
                        continue;
                    }
                    // delete the children of the node
                    DatabaseReference nodeRef = childSnapshot.getRef();
                    nodeRef.removeValue(new DatabaseReference.CompletionListener() {

                        /**
                         * Method to handle error if the data is not changed correctly.
                         * @param error A description of any errors that occurred or null on success
                         * @param ref A reference to the specified Firebase Database location
                         */
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                // handle error
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });
    }

    /**
     * A child of the Exception Class made to specifically
     * warn the user of errors when trying to make a group.
     */
    public class GroupException extends Exception {
        /**
         * Creates a custom exception message for making groups
         * @param msg the error message
         */
        public GroupException(String msg) {
            super(msg);
        }
    }


    /**
     * An interface whose purpose is to manage the untimely effects of asynchronous calls.
     * It makes sure the data is availble before the code tries to run a varible that is null.
     */
    public interface FireBaseDataCallback {
        /**
         * When implemented as a part of a getRoomatesPurchased() method call
         * provides data to the implementing method when it is ready.
         * @param data the seperate lists of purchases by each roomate
         */
        void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data);

        /**
         * When implemented as a part of a getRoomatesPurchasedCosts() method call
         * provides data to the implementing method when it is ready.
         * @param data the average cost, the total each roomate spent, and the overall total spent by the group.
         */
        void onCalculationsReceived(LinkedHashMap<String,Double> data);

        /**
         * When implemented as a part of a getItems() method call
         * provides data to the implementing method when it is ready.
         * @param data all the items added to the shopping list.
         */
        void onItemsDataReceived(LinkedHashMap<String, String> data);

        /**
         * When implemented as a part of a getPurchased() method call
         * provides data to the implementing method when it is ready.
         * @param data all the items purchased by the group
         */
        void onPurchasedDataRecieved(LinkedHashMap<String, Double> data);

        void onLogin(String[] data);
    }

    /**
     * An interface that handled the asynchronous calls for
     * deletion only, so the program knows when the data is gone
     * to update the appropriate UI elements.
     */
    public interface DeleteCallback {

        /**
         * When implemented as a part of a removeItem() method call
         * lets the implementing method know for the UI to be updated.
         */
        void itemDeleted();

        /**
         * A test method that was in charge of
         * letting the calling method know when the
         * Purchased list was empty.
         */
        void purchasedCleared();

        /**
         * When implemented as a part of a removePurchased() method call
         * lets the implementing method know for the UI to be updated.
         */
        void purchasedDeleted();

    }




}
