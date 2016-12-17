package com.dogpo.kalobadmin.content;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.datetimepicker.date.DatePickerDialog;
import com.bumptech.glide.Glide;
import com.dogpo.kalobadmin.R;
import com.dogpo.kalobadmin.category.MyData;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuItem;
import com.shehabic.droppy.DroppyMenuPopup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.id;

public class ContentDetailActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private static final int PICK_VIDEO = 234;
    private static final int PICK_IMAGE = 420;
    EditText editTextTitle, editTextDescription;
    TextView textViewDate, textViewCategory, textViewSpeaker;
    CircleImageView circleImageView;
    VideoView videoView;
    boolean editflag = false;
    ArrayList<com.dogpo.kalobadmin.category.MyData> categories = new ArrayList<>();
    ArrayList<com.dogpo.kalobadmin.speaker.MyData> speakers = new ArrayList<>();
    String key, title, description, image_url, video_url, category, speaker, date;
    private DatabaseReference _speech_main;
    private DatabaseReference _child_speech_main, _category, _speaker;
    private DroppyMenuPopup.Builder categoryBuilder;
    String categoryId = "", speakerId = "";
    private DroppyMenuPopup.Builder speakerBuilder;
    private Uri selectedVideoLocation = null;
    private boolean changePicFlag = false, changeVideoFlag = false;
    private Context context;
    private String imageFilePath;
    boolean uploadDoneImageFlag = false, uploadedDoneVideoFlag = false;
    private String tempKey;
    private FullscreenVideoLayout videoLayout;
    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_detail);
        context = this;
        getPermission();
        try {
            Bundle bundle = getIntent().getExtras();
            key = bundle.getString("key");
            title = bundle.getString("title");
            description = bundle.getString("description");
            image_url = bundle.getString("image_url");
            video_url = bundle.getString("video_url");
            categoryId = bundle.getString("category_id");
            category = bundle.getString("category");
            speaker = bundle.getString("speaker");
            speakerId = bundle.getString("speaker_id");
            date = bundle.getString("date");
            editflag = true;
        } catch (Exception ex) {
            editflag = false;
        }
        initView();
        if (!editflag) {
            setupCategoryDropdown();
            setupSpeakerDropdown();
        }
    }

    private void setupSpeakerDropdown() {
        speakerBuilder = new DroppyMenuPopup.Builder(ContentDetailActivity.this, textViewSpeaker);

        speakerBuilder.setOnClick(new DroppyClickCallbackInterface() {
            @Override
            public void call(View v, int id) {
                textViewSpeaker.setText(speakers.get(id).name);
                speakerId = speakers.get(id).id;
                showMessage(textViewSpeaker.getText().toString() + "  " + speakerId);
            }
        });
    }

    private void setupCategoryDropdown() {
        categoryBuilder = new DroppyMenuPopup.Builder(ContentDetailActivity.this, textViewCategory);

        categoryBuilder.setOnClick(new DroppyClickCallbackInterface() {
            @Override
            public void call(View v, int id) {
                textViewCategory.setText(categories.get(id).name);
                categoryId = categories.get(id).id;
                showMessage(textViewCategory.getText().toString());
            }
        });
    }

    private void showMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        progressDialog = new MaterialDialog.Builder(context)
                .title(R.string.progress_dialog)
                .content(R.string.please_wait)
                .progress(true, 0).build();
        videoLayout = (FullscreenVideoLayout) findViewById(R.id.videoview);
        videoLayout.setActivity(this);

        editTextTitle = (EditText) findViewById(R.id.et_name);
        editTextDescription = (EditText) findViewById(R.id.et_description);
        textViewCategory = (TextView) findViewById(R.id.textViewcategory);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewSpeaker = (TextView) findViewById(R.id.textViewSpeaker);
        circleImageView = (CircleImageView) findViewById(R.id.imageView);
        circleImageView.setOnClickListener(this);
        videoView = (VideoView) findViewById(R.id.videoView);
        findViewById(R.id.buttonVideo).setOnClickListener(this);
        findViewById(R.id.buttonSubmit).setOnClickListener(this);
        findViewById(R.id.textViewDate).setOnClickListener(this);
        findViewById(R.id.buttonPlay).setOnClickListener(this);
        findViewById(R.id.buttonStop).setOnClickListener(this);
        findViewById(R.id.buttonDelete).setOnClickListener(this);
        if (editflag) {

            try {
                Uri videoUri = Uri.parse(video_url);
                videoLayout.setVideoURI(videoUri);
                videoLayout.setShouldAutoplay(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Glide.with(context).load(image_url).error(R.drawable.ic_edit).into(circleImageView);
            editTextTitle.setText(title);
            editTextDescription.setText(description);
            textViewCategory.setText(category);
            textViewSpeaker.setText(speaker);
            textViewDate.setText(date);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
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
        _speech_main = _root.getReference().child("speech_detail");
        if (editflag) {
            _child_speech_main = _speech_main.child(key);
        }
        try {
            _category = _root.getReference().child("category");
            _speaker = _root.getReference().child("speaker");
            _category.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    appendCategory(dataSnapshot);
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
            _speaker.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    appendSpeaker(dataSnapshot);
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
        } catch (Exception ex) {

        }
    }

    private void appendSpeaker(DataSnapshot dataSnapshot) {
        try {
            Log.i("kunsangfirebase", dataSnapshot.toString());
            String name, description, image_url, id;
            int priority;
            name = dataSnapshot.child("name").getValue(String.class);
            description = dataSnapshot.child("description").getValue(String.class);
            image_url = dataSnapshot.child("image_url").getValue(String.class);
            id = dataSnapshot.child("id").getValue(String.class);
            priority = dataSnapshot.child("priority").getValue(Integer.class);
            com.dogpo.kalobadmin.speaker.MyData myData = new com.dogpo.kalobadmin.speaker.MyData(id, name, description, image_url, priority);
            if (!speakers.contains(myData)) {
                speakers.add(myData);
                speakerBuilder.addMenuItem(new DroppyMenuItem(myData.name));
            }
            speakerBuilder.build();

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    private void appendCategory(DataSnapshot dataSnapshot) {
        try {
            Log.i("kunsangfirebase", dataSnapshot.toString());
            String name, description, image_url, id;
            name = dataSnapshot.child("name").getValue(String.class);
            description = dataSnapshot.child("description").getValue(String.class);
            image_url = dataSnapshot.child("image_url").getValue(String.class);
            id = dataSnapshot.child("id").getValue(String.class);
            MyData myData = new MyData(id, name, description, image_url);
            if (!categories.contains(myData)) {
                categories.add(myData);
                categoryBuilder.addMenuItem(new DroppyMenuItem(myData.name));

            }
            categoryBuilder.build();

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonVideo:
                Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
//comma-separated MIME types
                mediaChooser.setType("video/*");
                startActivityForResult(mediaChooser, PICK_VIDEO);
                break;
            case R.id.textViewDate:

                Calendar now = Calendar.getInstance();
                DatePickerDialog.newInstance(this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "datePicker");

                break;
            case R.id.buttonSubmit:
                if (editflag = false) {
                    if (imageFilePath == null || selectedVideoLocation == null) {
                        showMessage("pic needed");
                        return;
                    }
                    if (editTextTitle == null || category == null || editTextDescription == null || speaker == null) {
                        showMessage("field needed");
                        return;
                    }
                    progressDialog.show();
                    uploadImage(imageFilePath);
                    uploadVideo(selectedVideoLocation);
                } else {
                    progressDialog.show();

                    if (changePicFlag && changeVideoFlag) {
                        uploadImage(imageFilePath);
                        uploadVideo(selectedVideoLocation);
                        return;
                    }
                    if (changePicFlag) {
                        uploadImage(imageFilePath);
                        return;
                    }
                    if (changeVideoFlag) {
                        uploadVideo(selectedVideoLocation);
                        return;
                    }
                }


                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("title", editTextTitle.getText().toString());
                childUpdates.put("description", editTextDescription.getText().toString());
                childUpdates.put("category", textViewCategory.getText().toString());
                childUpdates.put("category_id", categoryId);
                childUpdates.put("speaker", textViewSpeaker.getText().toString());
                childUpdates.put("speaker_id", speakerId);
                childUpdates.put("date", textViewDate.getText().toString());


                _child_speech_main.updateChildren(childUpdates);
                _child_speech_main.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        showMessage("done updating...");
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.buttonPlay:
                if (selectedVideoLocation == null) {
                    showMessage("video not selected");
                    return;
                }
                playSong();
                break;
            case R.id.buttonStop:
                stopVideo();
                break;
            case R.id.imageView:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
            case R.id.buttonDelete:
                _child_speech_main.removeValue();
                break;
        }

    }

    private void uploadVideo(Uri selectedVideoLocation) {
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/*")
                .build();

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://kalob-2b82c.appspot.com");

// Create a reference to "mountains.jpg"
        StorageReference mountainsRef;
        StorageReference mountainImagesRef;
        final Map<String, Object> map = new HashMap<String, Object>();
        tempKey = _speaker.push().getKey();
        _speaker.updateChildren(map);
        UploadTask uploadTask;
        if (editflag) {
            uploadTask = storageRef.child("video/" + key).putFile(selectedVideoLocation, metadata);


        } else {
            uploadTask = storageRef.child("video/" + tempKey).putFile(selectedVideoLocation, metadata);


        }
// Upload the file and metadata
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
                video_url = downloadUrl.toString();
                uploadedDoneVideoFlag = true;
                if (uploadDoneImageFlag) {
                    DatabaseReference _child_speech_main = _speech_main.child(tempKey);
                    /*
                    *   childUpdates.put("title", editTextTitle.getText().toString());
                childUpdates.put("description", editTextDescription.getText().toString());
                childUpdates.put("category", textViewCategory.getText().toString());
                childUpdates.put("category_id", category_id);
                childUpdates.put("speaker", textViewSpeaker.getText().toString());
                childUpdates.put("speaker_id", speaker_id);
                childUpdates.put("date", textViewDate.getText().toString());


                _child_speech_main.updateChildren(childUpdates);
                    * */
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    map1.put("title", editTextTitle.getText().toString());
                    map1.put("description", editTextDescription.getText().toString());
                    map1.put("category_id", categoryId);
                    map1.put("category", textViewCategory.getText().toString());
                    map1.put("speaker_id", speakerId);
                    map1.put("speaker", textViewSpeaker.getText().toString());
                    map1.put("date", textViewDate.getText().toString());
                    map1.put("view", 0);
                    map1.put("like", 0);
                    map1.put("comment", 0);
                    map1.put("image_url", image_url);
                    map1.put("video_url", video_url);
                    map1.put("timestamp", ServerValue.TIMESTAMP);
                    _child_speech_main.updateChildren(map1);
                    showMessage("uploading... please wait");

                    _child_speech_main.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            progressDialog.dismiss();
                            showMessage("uploading done");
                            updateSpeakerVideoCount(speakerId);
                            updateCategoryVideoCount(categoryId);
                            finish();
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

    private void updateCategoryVideoCount(String categoryId) {
        final DatabaseReference _category_child=_category.child(categoryId);
        _category_child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = dataSnapshot.child("total_video").getValue(Integer.class);
                count++;
                _category_child.child("total_video").setValue(count);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateSpeakerVideoCount(String speakerId) {
        final DatabaseReference _speaker_child=_speaker.child(speakerId);
        _speaker_child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = dataSnapshot.child("total_video").getValue(Integer.class);
                count++;
                _speaker_child.child("total_video").setValue(count);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void stopVideo() {
        try {
            videoView.stopPlayback();
        } catch (Exception ex) {

        }

    }

    private void playSong() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(selectedVideoLocation);
        videoView.requestFocus();
        videoView.start();
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear = monthOfYear + 1;
        String date = dayOfMonth + "-" + monthOfYear + "-" + year;
        textViewDate.setText(date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                changeVideoFlag = true;
                selectedVideoLocation = data.getData();
                try {
                    Uri videoUri = selectedVideoLocation;
                    videoLayout.setVideoURI(videoUri);
                    videoLayout.setShouldAutoplay(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Do something with the data...
            }

        }
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    changePicFlag = true;
                    Uri uri = data.getData();
                    imageFilePath = getPath(uri);
                    Log.i("kresult", imageFilePath);
                    Glide.with(context).load(imageFilePath).into(circleImageView);
                }
                break;
        }

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
        File file = savebitmap(filePath);
        String newFilePath = file.getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://kalob-2b82c.appspot.com");

// Create a reference to "mountains.jpg"
        StorageReference mountainsRef;
        StorageReference mountainImagesRef;
        final Map<String, Object> map = new HashMap<String, Object>();
        tempKey = _speaker.push().getKey();
        _speaker.updateChildren(map);
        if (editflag) {
            mountainsRef = storageRef.child(key);
            mountainImagesRef = storageRef.child("images/" + key);

        } else {
            mountainsRef = storageRef.child(tempKey);
            mountainImagesRef = storageRef.child("images/" + tempKey);


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
                image_url = downloadUrl.toString();
                uploadDoneImageFlag = true;
                if (uploadedDoneVideoFlag) {
                    DatabaseReference _child_speech_main = _speech_main.child(tempKey);
                    /*
                    *   childUpdates.put("title", editTextTitle.getText().toString());
                childUpdates.put("description", editTextDescription.getText().toString());
                childUpdates.put("category", textViewCategory.getText().toString());
                childUpdates.put("category_id", category_id);
                childUpdates.put("speaker", textViewSpeaker.getText().toString());
                childUpdates.put("speaker_id", speaker_id);
                childUpdates.put("date", textViewDate.getText().toString());


                _child_speech_main.updateChildren(childUpdates);
                    * */
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    map1.put("title", editTextTitle.getText().toString());
                    map1.put("description", editTextDescription.getText().toString());
                    map1.put("category_id", categoryId);
                    map1.put("category", textViewCategory.getText().toString());
                    map1.put("speaker_id", speakerId);
                    map1.put("speaker", textViewSpeaker.getText().toString());
                    map1.put("date", textViewDate.getText().toString());
                    map1.put("view", 0);
                    map1.put("like", 0);
                    map1.put("comment", 0);
                    map1.put("image_url", image_url);
                    map1.put("video_url", video_url);
                    map1.put("timestamp", ServerValue.TIMESTAMP);

                    _child_speech_main.updateChildren(map1);
                    showMessage("uploading... please wait");
                    _child_speech_main.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            progressDialog.dismiss();
                            showMessage("uploading done");
                            updateSpeakerVideoCount(speakerId);
                            updateCategoryVideoCount(categoryId);
                            finish();
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


}
