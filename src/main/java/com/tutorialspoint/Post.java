package com.tutorialspoint;

import java.time.LocalDate;

public class Post {
    long id;
    //date - in unix time.
    long date;
    String text;
    long likesCount;
    /*TODO:Добавить attachements.*/

    public Post(long id, long date, String text, long likesCount){
        this.id = id;
        this.date = date;
        this.text = text;
        this.likesCount = likesCount;
    }

    public String toString() {
        return "<p>" + this.date + "(" + this.likesCount + " likes)" + "<br>" + this.text + "</p>";
    }
}
