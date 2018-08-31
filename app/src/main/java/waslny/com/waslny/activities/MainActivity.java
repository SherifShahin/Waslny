package waslny.com.waslny.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import waslny.com.waslny.R;
import waslny.com.waslny.Maps.customer_map;
import waslny.com.waslny.Maps.driver_map;

public class MainActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();

        sharedPreferences=getSharedPreferences("user_type", Context.MODE_PRIVATE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(firebaseAuth.getCurrentUser() == null)
        {
            finish();
            startActivity(new Intent(this,check_for_login.class));
        }

        else
        {
            String s=sharedPreferences.getString("type","");

            if(s.equals("driver"))
            {
                finish();
                startActivity(new Intent(this,driver_map.class));
            }
            else {
                finish();
                startActivity(new Intent(this, customer_map.class));
            }
        }


    }
}
