package com.dogpo.kalobadmin.content;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dogpo.kalobadmin.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gowtham Chandrasekar on 31-07-2015.
 */
public class MyHolder extends RecyclerView.ViewHolder {


    CircleImageView circleImageView;
    TextView textViewName,textViewDescription;
    ImageView imageViewEdit;
    View view;


    public MyHolder(View itemView) {
        super(itemView);
        //implementing onClickListener
        view = itemView;
        circleImageView= (CircleImageView) view.findViewById(R.id.imageView);
        textViewDescription= (TextView) view.findViewById(R.id.textViewDscription);
        textViewName= (TextView) view.findViewById(R.id.textViewName);
        imageViewEdit= (ImageView) view.findViewById(R.id.imageViewEdit);

    }
}