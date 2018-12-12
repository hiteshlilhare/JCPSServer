package com.github.hiteshlilhare.jcpss.metadata.bean;

/**
 *
 * @author Hitesh
 */
public class User {

    private String email;
    private int rating;
    private String comment;

    public String getEmail() {
        return email;
    }

    public void setEmail(String userID) {
        this.email = userID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "User :[emial:" + email + ",rating:" + rating + ",comment:" + comment + "]";
    }

}
