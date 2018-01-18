package com.tutorialspoint;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    long id;
    //date - in unix time.
    long date;
    String text;
    long likesCount;
    /*TODO:Добавить attachements.*/
    List<Photo> photos = new ArrayList<>();
/*    List<Video> videos = new;
    List<Audio> audios = new;*/
    List<Gif> gifs = new ArrayList<>();

    public Post(long id, long date, String text, long likesCount){
        this.id = id;
        this.date = date;
        this.text = text;
        this.likesCount = likesCount;
    }

    @Override
    public String toString() {
        StringBuilder htmlPostRepresent = new StringBuilder();
        htmlPostRepresent.append("<p>").
                append(Date.from( Instant.ofEpochSecond( this.date ) )).
                append("(").
                append(this.likesCount).
                append(" likes)").
                append("<br>").
                append(this.text).
                append("<br>");

        //Проверяем пост на наличие у него прикрепленных файлов. Если есть - отображаем их тоже.
        if (this.photos.size() != 0) {
            for (Photo photo : photos) {
                htmlPostRepresent.append(photo.toString());
            }
        }
        //То же для аудио и видео.
        if (this.gifs.size() != 0) {
            for (Gif gif : gifs) {
                htmlPostRepresent.append(gif.toString());
            }
        }
        htmlPostRepresent.append("</p>");
        return htmlPostRepresent.toString();
    }

    public void addPhoto(String reference) {
        this.photos.add(new Photo(reference));
    }

    public void addGif(String reference) {
        this.gifs.add(new Gif(reference));
    }
}
