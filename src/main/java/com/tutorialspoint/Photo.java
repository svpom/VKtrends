package com.tutorialspoint;

public class Photo extends Attachment{
    public Photo(String reference){
        super(reference);
    }

    @Override
    public String toString() {
        return "<img src=\"" + this.reference +
                "\" alt=\"Фото не загрузилось.\">" + "<br>";
    }
}
