package com.dogpo.kalobadmin.content;

import android.Manifest;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.dogpo.kalobadmin.R;
import com.dogpo.kalobadmin.category.*;
import com.dogpo.kalobadmin.category.MyData;
import com.dogpo.kalobadmin.speaker.*;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuItem;
import com.shehabic.droppy.DroppyMenuPopup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.id;
import static android.R.attr.y;

public class ContentDetailActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    EditText editTextTitle, editTextDescription;
    TextView textViewDate, textViewCategory, textViewSpeaker;
    CircleImageView circleImageView;
    VideoView videoView;
    boolean editflag = false;
    ArrayList<com.dogpo.kalobadmin.category.MyData> categories = new ArrayList<>();
    ArrayList<com.dogpo.kalobadmin.speaker.MyData> speakers = new ArrayList<>();
    String key = "", title = "", description = "", image_url = "", video_url = "", category_id = "", category = "", speaker = "", speaker_id = "", date = "";
    private DatabaseReference _speech_main;
    private DatabaseReference _child_speech_main, _category, _speaker;
    private DroppyMenuPopup.Builder categoryBuilder;
    String categoryId = "", speakerId;
    private DroppyMenuPopup.Builder speakerBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_detail);
        getPermission();
        initView();
        try {
            Bundle bundle = getIntent().getExtras();
            key = bundle.getString("key");
            title = bundle.getString("title");
            description = bundle.getString("description");
            image_url = bundle.getString("image_url");
            video_url = bundle.getString("video_url");
            category_id = bundle.getString("category_id");
            category = bundle.getString("category");
            speaker = bundle.getString("speaker");
            speaker_id = bundle.getString("speaker_id");
            date = bundle.getString("date");
            editflag = true;
        } catch (Exception ex) {
            editflag = false;
        }
        setupCategoryDropdown();
        setupSpeakerDropdown();

    }

    private void setupSpeakerDropdown() {
        speakerBuilder = new DroppyMenuPopup.Builder(ContentDetailActivity.this, textViewSpeaker);

        speakerBuilder.setOnClick(new DroppyClickCallbackInterface() {
            @Override
            public void call(View v, int id) {
                textViewSpeaker.setText(speakers.get(id).name);
                speakerId = speakers.get(id).id;
                showMessage(textViewSpeaker.getText().toString());
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
        editTextTitle = (EditText) findViewById(R.id.et_name);
        editTextDescription = (EditText) findViewById(R.id.et_description);
        textViewCategory = (TextView) findViewById(R.id.textViewcategory);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewSpeaker = (TextView) findViewById(R.id.textViewSpeaker);
        circleImageView = (CircleImageView) findViewById(R.id.imageView);
        videoView = (VideoView) findViewById(R.id.videoView);
        findViewById(R.id.buttonVideo).setOnClickListener(this);
        findViewById(R.id.buttonSubmit).setOnClickListener(this);
        findViewById(R.id.textViewDate).setOnClickListener(this);
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
                break;
            case R.id.textViewDate:

                Calendar now = Calendar.getInstance();
                DatePickerDialog.newInstance(this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "datePicker");

                break;
            case R.id.buttonSubmit:
                break;
        }

    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear = monthOfYear + 1;
        String date = dayOfMonth + "-" + monthOfYear + "-" + year;
        textViewDate.setText(date);
    }
}
