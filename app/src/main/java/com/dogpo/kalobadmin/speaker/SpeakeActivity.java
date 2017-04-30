package com.dogpo.kalobadmin.speaker;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.dogpo.kalobadmin.R;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SpeakeActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    private Context context;
    ArrayList<MyData> myDatas = new ArrayList<>();
    private MyAdapter adapter;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        context = this;
        setupToolBar("Speaker");
        getPermission();
        initView();
        setupRecyclerView();
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

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("speaker");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendList(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendList(dataSnapshot);

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

    private void appendList(DataSnapshot dataSnapshot) {
        try {
            Log.i("kunsangfirebase", dataSnapshot.toString());
            String name, description, image_url, id;
            int priority;
            name = dataSnapshot.child("name").getValue(String.class);
            description = dataSnapshot.child("description").getValue(String.class);
            image_url = dataSnapshot.child("image_url").getValue(String.class);
            id = dataSnapshot.child("id").getValue(String.class);
            priority = dataSnapshot.child("priority").getValue(Integer.class);
            MyData myData = new MyData(id, name, description, image_url, priority);
            if (!myDatas.contains(myData)) {
                myDatas.add(myData);
            } else {
                int index = myDatas.indexOf(myData);
                myDatas.set(index, myData);
            }
            adapter.notifyDataSetChanged();

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        adapter = new MyAdapter(context, myDatas);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }



    private void initView() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SpeakerDetailActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setupToolBar(String app_name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(app_name);

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

    @Override
    protected void onRestart() {
        super.onRestart();
        setupFirebase();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setupFirebase();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }



    @Override
    public boolean onQueryTextChange(String s) {
        upDateList(s);

        return false;
    }

    private void upDateList(String s) {
        myDatas.clear();
        adapter.notifyDataSetChanged();
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mFirebaseDatabaseReference.child("speaker").orderByChild("name").startAt(s).limitToFirst(1);
        query.addValueEventListener(valueEventListener);


    }
    ValueEventListener valueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot)
        {
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
            {
                appendList(postSnapshot);

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError)
        {

        }
    };

}
