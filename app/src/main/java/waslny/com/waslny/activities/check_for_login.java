package waslny.com.waslny.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import waslny.com.waslny.R;
import waslny.com.waslny.activities.customer_login_activity;
import waslny.com.waslny.activities.driver_login_activity;

public class check_for_login extends AppCompatActivity implements View.OnClickListener
{

    private Button driver;
    private Button user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_for_login);

        driver=(Button)findViewById(R.id.driver_button);
        user=(Button)findViewById(R.id.user_button);

        //action of driver button and
        //user button
        driver.setOnClickListener(this);
        user.setOnClickListener(this);

    }

    @Override
    public void onClick(View view)
    {
        if(view == driver)
        {
            //make intent to driver login activity
            Intent login=new Intent(this,driver_login_activity.class);
            //start the new activity
            startActivity(login);
        }

        if(view == user)
        {
            //make intent to customer login activity and start it
            startActivity(new Intent(this,customer_login_activity.class));
        }
    }
}
