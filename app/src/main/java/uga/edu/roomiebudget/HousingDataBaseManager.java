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
import java.util.Map;

public class HousingDataBaseManager {
    private FirebaseDatabase fdb = FirebaseDatabase.getInstance("https://roomiebudget-default-rtdb.firebaseio.com");
    private DatabaseReference fRef;
    private DatabaseReference uRef;

    private final static String DATABASE_ENTRY = "/dorms";
    private final static String DATABASE_USERS = "/users";

    private Context context;

    private FirebaseAuth fAuth;

    private Intent intent;

    private final String USER_PREF = "preferences";
    private String[] auto;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private LinkedHashMap<String,String> item_list;
    private LinkedHashMap<String,Double> purchased_list;
    private MasterKey masterKey;
    private SharedPreferences privSharedPreferences;
    private SharedPreferences pubSharedPreferences;


    private LinkedHashMap<String, LinkedHashMap<String, Double>> lists_by_user;

    public  HousingDataBaseManager(Context parent) {
        context = parent;
        fAuth = FirebaseAuth.getInstance();
        preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }



    public void addUser(String email, String user, String fullName) {
        uRef = fdb.getReference(DATABASE_USERS);
        uRef = fdb.getReference(DATABASE_USERS + "/" + parseEmail(email));
        uRef.child("group").setValue(user);
        uRef.child("full_name").setValue(parseEmail(email));
        uRef.child("email").setValue(email);
    }


    public String parseEmail(String email) {
        email = email.substring(0, email.indexOf('@'));
        return email;
    }



    public void signinUser(String email, String password) {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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







    public void createGroup(String group) throws Exception{
        fRef = fdb.getReference(DATABASE_ENTRY);
        fRef.child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void addItem(String group, String item) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");
        fRef.child(item).setValue("");

    }

    public void purchasedItem(String group, String member, String item, Double price) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + member);
        fRef.child(item).setValue(price);
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");
        fRef.child(item).setValue(price);
    }


    public void getItems(String group, FireBaseDataCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
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


    public void getPurchased(String group, FireBaseDataCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void createUserWithGroup(String email, String group, String name, String password) {
        //  Log.e(TAG, "GROUP: " + group);
            fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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



    public void createUserWithoutGroup(String email, String group, String name, String password) {
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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


    public void getRoomatesPurchased(String group, FireBaseDataCallback callback) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> data = new LinkedHashMap<>();

        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            LinkedHashMap<String, Double> innerdata;
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




    public void getRoomatesPurchasedCosts(String group, FireBaseDataCallback callback) {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();


        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void encryptLogin(String email, String password) throws GeneralSecurityException, IOException {
        masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        privSharedPreferences = EncryptedSharedPreferences.create(context, "user_cred", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        privSharedPreferences.edit().putString("email", email).apply();
        privSharedPreferences.edit().putString("password",password).apply();
    }

    public void savePref(String group, String name, String email) {
        pubSharedPreferences = context.getSharedPreferences("user_pref",Context.MODE_PRIVATE);
        editor = pubSharedPreferences.edit();
        editor.putString("group", group).apply();
        editor.putString("name", name).apply();
        editor.putString("email", email).apply();
    }



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

    public String[] getUser() {
        String[] userdata = new String[3];
        pubSharedPreferences = context.getSharedPreferences("user_pref",Context.MODE_PRIVATE);
        userdata[0] = pubSharedPreferences.getString("group", null);
        userdata[1] = pubSharedPreferences.getString("name", null);
        userdata[2] = pubSharedPreferences.getString("email", null);
        return userdata;
    }

    public void storeUser(String user, FireBaseDataCallback callback) {
        uRef = fdb.getReference(DATABASE_USERS + "/" + user);
        String[] userStuff = new String[3];
        uRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void removeItem(String item, String group, DeleteCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list").child(item);
        Log.e(TAG, group);
        fRef.removeValue(new DatabaseReference.CompletionListener() {
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

    public void removePurchased(String user, String group, String item, DeleteCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_Purchased").child(item);
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


        /*
        uRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + user).child(item);
        uRef.removeValue(new DatabaseReference.CompletionListener() {
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
         */
    }

    public void removePurchasedUser(String user, String group, String item) {
        uRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + user).child(item);
        uRef.removeValue();

        /* uRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Notify the callback that the data has changed
                    if (callback != null) {
                        callback.purchasedDeleted();
                    }
                }
            }
        }); */
    }


    public void clearAllPurchasedData(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY).child(group);

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
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



    public class GroupException extends Exception {
        public GroupException(String msg) {
            super(msg);
        }
    }

    public interface FireBaseDataCallback {
        void onRoomatesPurchasedDataReceived(LinkedHashMap<String, LinkedHashMap<String, Double>> data);
        void onCalculationsReceived(LinkedHashMap<String,Double> data);
        void onItemsDataReceived(LinkedHashMap<String, String> data);

        void onPurchasedDataRecieved(LinkedHashMap<String, Double> data);

        void onLogin(String[] data);
    }

    public interface DeleteCallback {
        void itemDeleted();
        void purchasedCleared();

        void purchasedDeleted();

    }




}
