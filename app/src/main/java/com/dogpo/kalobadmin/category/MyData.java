package com.dogpo.kalobadmin.category;

/**
 * Created by Gowtham Chandrasekar on 31-07-2015.
 */
public class MyData {
    String description,name,id,imageurl;
    MyData(String id,String name,String description, String imageurl){
        this.imageurl=imageurl;
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
