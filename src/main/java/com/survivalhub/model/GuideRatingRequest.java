package com.survivalhub.model;

public class GuideRatingRequest {

    private String userName;

    private int rating;

    public GuideRatingRequest() {
    }

    public GuideRatingRequest(String userName, int rating) {
        this.userName = userName;
        this.rating = rating;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
