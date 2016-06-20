package com.ink.models;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedModel {
    private String mTitle;
    private String mContent;


    public FeedModel(String mTitle, String mContent) {
        this.mTitle = mTitle;
        this.mContent = mContent;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }
}
