package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
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
    private Map<String,String> item_list = new HashMap<>();
    private Map<String,Double> purchased_list = new HashMap<>();

    private Map<String, Map<String, Double>> lists_by_user = new HashMap<>();

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


    public Map<String,String> getItems(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Item_list");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    HashMap<String, String> value = (HashMap<String,String>) snapshot.getValue();
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


    public Map<String ,Double> getPurchased(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group + "/" + "Recently_purchased");

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Double> value = (HashMap<String,Double>) snapshot.getValue();
                for (Map.Entry<String, Double> b: value.entrySet()) {
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

    public class GroupException extends Exception {
        public GroupException(String msg) {
            super(msg);
        }
    }

}
