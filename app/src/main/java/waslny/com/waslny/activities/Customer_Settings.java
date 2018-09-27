package waslny.com.waslny.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import waslny.com.waslny.R;

public class Customer_Settings extends AppCompatActivity implements View.OnClickListener
{

    private CircleImageView customer_image;
    private EditText customer_name;
    private EditText customer_phone;
    private Button confirm_bt;

    private StorageReference image_storage;
    private DatabaseReference database;

    private final static int gallery_pick=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer__settings);


        customer_image=(CircleImageView)findViewById(R.id.customer_image_view);
        customer_name=(EditText)findViewById(R.id.customer_setings_name);
        customer_phone=(EditText)findViewById(R.id.customer_setings_phone);
        confirm_bt=(Button)findViewById(R.id.customer_settings_confirm);


        image_storage=FirebaseStorage.getInstance().getReference();
        String current_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        database=FirebaseDatabase.getInstance().getReference().child("Users")
                .child("customers").child(current_id);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name,phone,image;

                name=dataSnapshot.child("name").getValue().toString();
                phone=dataSnapshot.child("phone").getValue().toString();
                image=dataSnapshot.child("image").getValue().toString();

                customer_name.setText(name);
                customer_phone.setText(phone);

                if(!image.equals("default"))
                {
                    Picasso.with(Customer_Settings.this).load(image).placeholder(R.drawable.waslny_default_image).into(customer_image);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        customer_image.setOnClickListener(this);
        confirm_bt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == customer_image)
        {
            Intent gallery_intent=new Intent();
            gallery_intent.setType("image/*");
            gallery_intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(gallery_intent,"Select image :D"),gallery_pick);

        }
        if(view == confirm_bt)
        {
            updateData();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode == gallery_pick && resultCode == RESULT_OK)
        {
            Uri imageUri =data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                File image_file=new File(resultUri.getPath());
                final String current_id= FirebaseAuth.getInstance().getCurrentUser().getUid();



                StorageReference filepath=image_storage.child("profile_images").child(current_id+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            final String download_url=task.getResult().getDownloadUrl().toString();
                             Map map=new HashMap();
                             map.put("image",download_url);

                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child("customers").child(current_id);

                            databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getApplicationContext(),"image updated",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"sorry try again later...",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }







    }

    private void updateData()
    {
        final String name=customer_name.getText().toString();
        final String phone=customer_phone.getText().toString();


        Map map=new HashMap();
        map.put("name",name);
        map.put("phone",phone);

        database.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"data updated",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"sorry try again later",Toast.LENGTH_SHORT).show();

            }
        });

    }
}
