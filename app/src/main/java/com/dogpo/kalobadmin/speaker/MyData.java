package com.dogpo.kalobadmin.speaker;

/**
 * Created by Gowtham Chandrasekar on 31-07-2015.
 */
public class MyData {
    public String description,name,id,imageurl;
    public int priority;
    public MyData(String id, String name, String description, String imageurl, int priority){
        this.imageurl=imageurl;
        this.priority=priority;
        this.description=description;
        this.name=name;
        this.id=id;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyData)) {
            return false;
        }

        MyData that = (MyData) obj;
        return this.id.equals(that.id);
    }
}
