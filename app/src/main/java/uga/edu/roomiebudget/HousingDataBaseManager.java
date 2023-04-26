package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.HashMap;
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

    public void makeGroup(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY);
        fRef.child(group).setValue("");
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.child("Item_list").setValue("");
        fRef.child("Recently_purchased").setValue("");

    }

    public void addUser(String email, String user, String fullName, String password) {
        uRef = fdb.getReference(DATABASE_USERS);
        uRef = fdb.getReference(DATABASE_USERS + "/" + parseEmail(email));
        uRef.child("username").setValue(user);
        uRef.child("full_name").setValue(fullName);
        uRef.child("password").setValue(password);
    }

    public void addMember(String group, String username) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.child(username).setValue("");
    }

    public String parseEmail(String email) {
        email = email.substring(0, email.indexOf('@'));
        return email;
    }

    public void addGroupToUser(String user, String group) {
        uRef = fdb.getReference(DATABASE_USERS + "/" + user);
        uRef.child("dorm").setValue(group);
    }


    public void signinUser(String email, String password) {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        intent = new Intent(context, ShoppingListActivity.class);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Invalid Login", Toast.LENGTH_SHORT);
                    }

                }
            });
    }



    public void addUserToGroup(String email, String group) throws Exception {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);

        fRef.child(parseEmail(email)).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                } else {
                    try {
                        throw new GroupException("Group Already Exists");
                    } catch (GroupException e) {
                        Toast.makeText(context, "User Already in group", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }




    public void createGroup(String email, String group) throws Exception{
        fRef = fdb.getReference(DATABASE_ENTRY);
        fRef.child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
                                fRef.child(parseEmail(email)).setValue("");
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

    public void addItem(String group, String item, String id) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");
        fRef.child(item).setValue(parseEmail(id));
    }

    public void purchasedItem(String group, String member, String item, Double price) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + member);
        fRef.child(item).setValue(price);
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");
        fRef.child(item).setValue(price);
    }


    public LinkedHashMap<String,String> getItems(String group, FireBaseDataCallback callback) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinkedHashMap<String, String> value = (LinkedHashMap<String,String>) snapshot.getValue();
                    for (Map.Entry<String, String> b: value.entrySet()) {
                        Log.d(TAG, b.getKey());
                        Log.d(TAG, b.getValue());
                    }
                    item_list = value;


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return item_list;
    }


    public LinkedHashMap<String ,Double> getPurchased(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinkedHashMap<String, Double> value = (LinkedHashMap<String,Double>) snapshot.getValue();
                for (LinkedHashMap.Entry<String, Double> b: value.entrySet()) {
                    Log.d(TAG, b.getKey());
                    Log.d(TAG, String.valueOf(b.getValue()));
                }
                purchased_list = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return purchased_list;
    }



    public void createUserWithGroup(String email, String group, String name, String password) {
        //  Log.e(TAG, "GROUP: " + group);
            fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    createGroup(email, group);
                                    addUser(email, group, name, password);
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
                                addUserToGroup(email, group);
                                addUser(email, group, name, password);
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


    public LinkedHashMap<String,LinkedHashMap<String, Double>> getRoomatesPurchased(String group) {
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

                // Log.e(TAG, data.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return lists_by_user;
    }


    private void encryptLogin(String email, String password) throws GeneralSecurityException, IOException {
        masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        privSharedPreferences = EncryptedSharedPreferences.create(context, "user_cred", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        privSharedPreferences.edit().putString("email", email);
        privSharedPreferences.edit().putString("password",password);
    }

    private void savePref(String group, String name, String email) {
        pubSharedPreferences = context.getSharedPreferences("user_pref",Context.MODE_PRIVATE);
        editor = pubSharedPreferences.edit();
        editor.putString("group", group);
        editor.putString("name", name);
        editor.putString("email", parseEmail(email));
        editor.apply();
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

    public class GroupException extends Exception {
        public GroupException(String msg) {
            super(msg);
        }
    }

    public interface FireBaseDataCallback {
        void onDataRecieved(LinkedHashMap<String, LinkedHashMap<String, Double>> data);
        // void onDataRecieved()
    }

}
