package waslny.com.waslny.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import waslny.com.waslny.R;
import waslny.com.waslny.Maps.customer_map;

public class customer_login_activity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText email;
    private EditText password;
    private Button login;
    private TextView register_intent;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_login_activity);

        email=(EditText)findViewById(R.id.login_customer_email);
        password=(EditText)findViewById(R.id.login_customer_password);
        login=(Button)findViewById(R.id.login_customer_button);
        register_intent=(TextView) findViewById(R.id.customer_register_intent);

        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences("user_type", Context.MODE_PRIVATE);

        login.setOnClickListener(this);
        register_intent.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == login)
            login_customer();
        if(view == register_intent)
        {
            Toast.makeText(this,"register",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,customer_register_activity.class));
        }

    }

    private void login_customer()
    {
        Toast.makeText(getApplicationContext(),"log in ",Toast.LENGTH_LONG).show();
        String customer_email,customer_password;

        customer_email=email.getText().toString().trim();
        customer_password=password.getText().toString().trim();

        if(TextUtils.isEmpty(customer_email))
        {
            Toast.makeText(getApplicationContext(),"pls enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(customer_password))
        {
            Toast.makeText(getApplicationContext(),"pls enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(customer_email,customer_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("type", "customer");

                    editor.commit();

                    startActivity(new Intent(customer_login_activity.this, customer_map.class));
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(),"shit!!",Toast.LENGTH_LONG).show();
            }
        });


    }
}

