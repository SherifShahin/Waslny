package waslny.com.waslny.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import waslny.com.waslny.R;

public class customer_register_activity extends AppCompatActivity implements View.OnClickListener
{
    private EditText full_name;
    private  EditText email;
    private EditText password;
    private EditText age;
    private Button register_bt;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private TextView signin_intent;

    private boolean s=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_register_activity);

        full_name=(EditText)findViewById(R.id.register_customer_name);
        email=(EditText)findViewById(R.id.register_customer_email);
        password=(EditText)findViewById(R.id.register_customer_password);
        age=(EditText)findViewById(R.id.register_customer_age);
        register_bt=(Button)findViewById(R.id.customer_register_bt);
        signin_intent=(TextView)findViewById(R.id.customer_Signin_intent);

        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        register_bt.setOnClickListener(this);
        signin_intent.setOnClickListener(this);

    }

    @Override
    public void onClick(View view)
    {
        if(view == register_bt)
            register_user();

        if(view == signin_intent)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),customer_login_activity.class));
        }


    }

    private void register_user()
    {

        final String name, customer_email, customer_password,customer_age;
        name = full_name.getText().toString();
        customer_email = email.getText().toString().trim();
        customer_password = password.getText().toString().trim();
        customer_age = age.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "pls enter your full name", Toast.LENGTH_SHORT).show();

            return;
        }


        if (TextUtils.isEmpty(customer_email))
        {
            Toast.makeText(getApplicationContext(), "pls enter your email", Toast.LENGTH_SHORT).show();

            return;
        }

        if (TextUtils.isEmpty(customer_password)) {
            Toast.makeText(getApplicationContext(), "pls enter your Password", Toast.LENGTH_SHORT).show();

            return;
        }


        if (customer_password.length() < 8) {
            Toast.makeText(getApplicationContext(), "you password is too short", Toast.LENGTH_SHORT).show();

            return;
        }


        if (TextUtils.isEmpty(customer_age)) {
            Toast.makeText(getApplicationContext(), "pls enter your age", Toast.LENGTH_SHORT).show();

            return;
        }


        firebaseAuth.fetchProvidersForEmail(customer_email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                boolean check = task.getResult().getProviders().isEmpty();

                // if the email  existing in database
                if (!check)
                {
                    Toast.makeText(customer_register_activity.this, "sorry this email is already used", Toast.LENGTH_SHORT).show();
                    full_name.setText("");
                    email.setText("");
                    password.setText("");

                    return;
                }

                // the email not exist
                else {
                    s = false;
                }

            }
        });


        if (!s)
        {

            //create new account
            firebaseAuth.createUserWithEmailAndPassword(customer_email, customer_password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //when create auth finished put data
                    // in database
                    if (task.isSuccessful())
                    {
                        String uid = firebaseAuth.getCurrentUser().getUid().toString();

                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customers").child(uid);

                        HashMap<String, String> map = new HashMap<>();
                        map.put("name", name);
                        map.put("email", customer_email);
                        map.put("age",customer_age);
                        map.put("image","default");

                        //set the map in database
                        databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(), "account created", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                    else
                    {
                        Toast.makeText(getApplicationContext(), "shit!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
}
