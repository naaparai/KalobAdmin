package com.dogpo.kalobadmin.content;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dogpo.kalobadmin.R;
import com.dogpo.kalobadmin.speaker.SpeakerDetailActivity;

import java.util.List;

/**
 * Created by Gowtham Chandrasekar on 31-07-2015.
 */
public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SIMPLE_TYPE = 0;
    private static final int IMAGE_TYPE = 1;
    private final LayoutInflater inflater;
    private List<MyData> itemList;
    private Context context;

    public MyAdapter(Context context, List<MyData> itemList) {
        this.itemList = itemList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /*  @Override
      public int getItemViewType(int position) {
          if (itemList.get(position).image.isEmpty()) {
              return SIMPLE_TYPE;
          } else {
              return IMAGE_TYPE;
          }
      }
  */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.row_speech, parent, false);
        MyHolder viewHolder = new MyHolder(view);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyHolder myHolder= (MyHolder) holder;
        myHolder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,ContentDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("key",itemList.get(position).key);
                bundle.putString("title",itemList.get(position).title);
                bundle.putString("description",itemList.get(position).description);
                bundle.putString("image_url",itemList.get(position).image_url);
                bundle.putString("video_url",itemList.get(position).videourl);
                bundle.putString("category_id",itemList.get(position).cat_id);
                bundle.putString("category",itemList.get(position).category);
                bundle.putString("speaker",itemList.get(position).speaker);
                bundle.putString("speaker_id",itemList.get(position).speaker_id);
                bundle.putString("date",itemList.get(position).date);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        myHolder.textViewName.setText(itemList.get(position).title);
        myHolder.textViewDescription.setText(itemList.get(position).description);
        Glide.with(context).load(itemList.get(position).image_url).error(R.drawable.ic_edit).into(myHolder.circleImageView);

    }

    private void showMessage(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount() {
        return itemList.size();
//        return itemList.size();
    }
}
