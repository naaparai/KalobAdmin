package com.dogpo.kalobadmin;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.dogpo.kalobadmin.category.CategoryActivity;
import com.dogpo.kalobadmin.content.ContentActivity;
import com.dogpo.kalobadmin.speaker.SpeakeActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Intent intent;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        setupToolBar("Dashboard");
        initView();

    }

    private void showMessage(String query) {
        Toast.makeText(context, query, Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        findViewById(R.id.buttonSpeaker).setOnClickListener(this);
        findViewById(R.id.buttonCategory).setOnClickListener(this);
        findViewById(R.id.buttonContent).setOnClickListener(this);
    }

    private void setupToolBar(String app_name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonCategory:
                intent=new Intent(context, CategoryActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonContent:
                intent=new Intent(context, ContentActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonSpeaker:
                intent=new Intent(context, SpeakeActivity.class);
                startActivity(intent);
                break;
        }
    }


}
