package com.beta.autobookkeeping.activity.familyTodo.Entity;

import android.graphics.drawable.Drawable;

public class TodoItem {
    private Integer id;
    private String title;
    private String postTime;
    private String handleTime;
    private Drawable posterPortrait;
    private Drawable handlerPortrait;

    public TodoItem(Integer id,String title, String postTime,String handleTime, Drawable posterPortrait, Drawable handlerPortrait) {
        this.id = id;
        this.title = title;
        this.postTime = postTime;
        this.handleTime = handleTime;
        this.posterPortrait = posterPortrait;
        this.handlerPortrait = handlerPortrait;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public Drawable getPosterPortrait() {
        return posterPortrait;
    }

    public void setPosterPortrait(Drawable posterPortrait) {
        this.posterPortrait = posterPortrait;
    }

    public Drawable getHandlerPortrait() {
        return handlerPortrait;
    }

    public void setHandlerPortrait(Drawable handlerPortrait) {
        this.handlerPortrait = handlerPortrait;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
