package com.tutorialspoint;

import java.time.LocalDate;

public class Post {
    //date - in unix time.
    long date;
    String text;
    /*TODO:Добавить attachements.*/

    public Post(long date, String text){
        this.date = date;
        this.text = text;
    }

    public String toString() {
        return "<p>" + this.date + "<br>" + this.text + "</p>";
    }
}
