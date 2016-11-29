package com.dogpo.kalobadmin.content;

/**
 * Created by Gowtham Chandrasekar on 31-07-2015.
 */
public class MyData {

   String cat_id,category,speaker_id,speaker,date,description,image_url,title,videourl,key;
    long timestamp,view,comment,like;
    MyData(String cat_id,String category,String speaker_id, String speaker,long comment,String date,String description,String image_url,long like,String title,String videourl,long view,long timestamp,String key){
        this.cat_id=cat_id;
        this.category=category;
        this.speaker=speaker;
        this.speaker_id=speaker_id;
        this.comment=comment;
        this.date=date;
        this.description=description;
        this.image_url=image_url;
        this.like=like;
        this.title=title;
        this.videourl=videourl;
        this.view=view;
        this.timestamp=timestamp;
        this.key=key;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyData)) {
            return false;
        }

        MyData that = (MyData) obj;
        return this.key.equals(that.key);
    }
}
