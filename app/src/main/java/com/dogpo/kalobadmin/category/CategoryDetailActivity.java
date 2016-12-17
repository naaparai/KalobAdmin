package com.dogpo.kalobadmin.category;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dogpo.kalobadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE = 102;
    String id, image_url = "", name = "", description = "";
    EditText editTextName, editTextDescription;
    CircleImageView circleImageView;
    Context context;
    DatabaseReference _category, _child_category;
    private boolean changePicFlag = false;
    private String filePath;
    boolean editflag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        context = this;
        getPermission();
        try {

            Bundle bundle = getIntent().getExtras();
            id = bundle.getString("id");
            name = bundle.getString("name");
            description = bundle.getString("description");
            image_url = bundle.getString("image_url");
            editflag = true;

        } catch (Exception ex) {
            editflag = false;
            ex.printStackTrace();
        }
        Log.i("kunsangbool",""+editflag);
        setupToolBar(name);
        initView();
        setupFirebase();

    }

    private void getPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String[] permission = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            ActivityCompat.requestPermissions(this,
                    permission, 1);


        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void setupFirebase() {
        FirebaseDatabase _root = FirebaseDatabase.getInstance();
        _category = _root.getReference().child("category");
        if (editflag) {
            _child_category = _category.child(id);
        }
    }

    private void initView() {
        circleImageView = (CircleImageView) findViewById(R.id.imageView);
        editTextDescription = (EditText) findViewById(R.id.et_description);
        editTextName = (EditText) findViewById(R.id.et_name);
        editTextName.setText(name);
        editTextDescription.setText(description);
        Glide.with(context).load(image_url).error(R.drawable.ic_edit).into(circleImageView);
        findViewById(R.id.buttonDelete).setOnClickListener(this);
        findViewById(R.id.buttonSubmit).setOnClickListener(this);
        circleImageView.setOnClickListener(this);
    }

    private void setupToolBar(String app_name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(app_name);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                if (editflag = false && filePath == null) {
                    showMessage("pic needed");
                    if (editTextName.length() <= 0 && editTextDescription.length() <= 0) {
                        showMessage("field needed");
                        return;
                    }
                    return;
                }
                if (changePicFlag) {
                    uploadImage(filePath);
                    return;
                }
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("name", editTextName.getText().toString());
                childUpdates.put("description", editTextDescription.getText().toString());

                _child_category.updateChildren(childUpdates);
                break;

        }
    }

    private void showMessage(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    private File savebitmap(String filePath) {
        File file = new File(filePath);
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
        OutputStream outStream = null;
        try {
            // make a new bitmap from your file
            outStream = new FileOutputStream(file);
            if (extension.equalsIgnoreCase("png")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
            } else {
                return null;
            }
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;


    }

    private void uploadImage(String filePath) {
        final String myString = editTextName.getText().toString().replaceAll(" ", "_").toLowerCase();
        File file = savebitmap(filePath);
        String newFilePath = file.getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://kalob-2b82c.appspot.com");

// Create a reference to "mountains.jpg"
        StorageReference mountainsRef;
        StorageReference mountainImagesRef;
        if (editflag) {
            mountainsRef = storageRef.child(id);
            mountainImagesRef = storageRef.child("images/" + id);

        } else {
            mountainsRef = storageRef.child(myString);
            mountainImagesRef = storageRef.child("images/" + myString);


        }
// Create a reference to 'images/mountains.jpg'

// While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false
        circleImageView.setDrawingCacheEnabled(true);
        circleImageView.buildDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                if (editflag) {
                    Toast.makeText(context, "uploading done", Toast.LENGTH_SHORT).show();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("name", editTextName.getText().toString());
                    childUpdates.put("description", editTextDescription.getText().toString());
                    childUpdates.put("image_url", downloadUrl.toString());
                    _child_category.updateChildren(childUpdates);
                    showMessage("uploading... please wait");
                    _child_category.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            finish();
                            showMessage("uploading done");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(myString, "");
                    _category.updateChildren(map);
                    _category.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            DatabaseReference _child_category = _category.child(myString);
                            Map<String, Object> map1 = new HashMap<String, Object>();
                            map1.put("name", editTextName.getText().toString());
                            map1.put("description", editTextDescription.getText().toString());
                            map1.put("image_url", downloadUrl.toString());
                            map1.put("total_video", 0);
                            map1.put("id", myString);
                            _child_category.updateChildren(map1);
                            showMessage("uploading... please wait");
                            _child_category.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    finish();
                                    showMessage("uploading done");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    changePicFlag = true;
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
