package com.dogpo.kalobadmin.category;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dogpo.kalobadmin.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE = 102;
    String id,image_url,name,description;
    EditText editTextName,editTextDescription;
    CircleImageView circleImageView;
    Context context;
    DatabaseReference _category,_child_category;
    private boolean changePicFlag=false;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        context=this;
        try{
            Bundle bundle=getIntent().getExtras();
            id=bundle.getString("id");
            name=bundle.getString("name");
            description=bundle.getString("description");
             image_url=bundle.getString("image_url");
            setupToolBar(name);
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(this, "wrong", Toast.LENGTH_SHORT).show();
        }
        initView();
        setupFirebase();

    }

    private void setupFirebase() {
        FirebaseDatabase _root=FirebaseDatabase.getInstance();
        _category=_root.getReference().child("category");
        _child_category=_category.child(id);

    }

    private void initView() {
        circleImageView.setOnClickListener(this);
        editTextDescription= (EditText) findViewById(R.id.et_description);
        editTextName= (EditText) findViewById(R.id.et_name);
        editTextName.setText(name);
        editTextDescription.setText(description);
        Glide.with(context).load(image_url).error(R.drawable.ic_edit).into(circleImageView);
        findViewById(R.id.buttonDelete).setOnClickListener(this);
        findViewById(R.id.buttonSubmit).setOnClickListener(this);
    }

    private void setupToolBar(String app_name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(app_name);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonDelete:
            _child_category.removeValue();
                break;
            case R.id.imageView:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE); 
                break;
            case R.id.buttonSubmit:
                if(changePicFlag){

                }
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("name", editTextName.getText().toString());
                childUpdates.put("description" , editTextDescription.getText().toString());

                _child_category.updateChildren(childUpdates);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    changePicFlag=true;
                    Uri uri = data.getData();
                    filePath = getPath(uri);
                    Log.i("kresult", filePath);
                    Glide.with(context).load(filePath).into(circleImageView);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @SuppressLint("NewApi")
    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor;
        if (Build.VERSION.SDK_INT > 19) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{id}, null);
        } else {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {

        }
        return path;
    }

}
