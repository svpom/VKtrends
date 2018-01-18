package com.tutorialspoint;

public class Gif extends Attachment {
    public Gif(String reference){
        super(reference);
    }

    @Override
    public String toString() {
        return "<img src=\"" + this.reference +
                "\" alt=\"Gif не загрузилась.\">" + "<br>";
    }
}
