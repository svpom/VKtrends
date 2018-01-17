package com.tutorialspoint;

public class Photo {
    String reference;

    public Photo(String reference){
        this.reference = reference;
    }

    public String getReference(){
        return this.reference;
    }

    public void setReference(String reference){
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "<img src=\"" + this.reference +
                "\" alt=\"Фото не загрузилось.\">" + "<br>";
    }
}
