package com.github.hiteshlilhare.jcpss;

/**
 *
 * @author Hitesh
 */
public class AppDetail {
    private String title;
    private String version;
    private String description;
    private String releaseDate;
    private boolean signVerified;
    private float rating;
    private String appFilePath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isSignVerified() {
        return signVerified;
    }

    public void setSignVerified(boolean signVerified) {
        this.signVerified = signVerified;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAppFilePath() {
        return appFilePath;
    }

    public void setAppFilePath(String appFilePath) {
        this.appFilePath = appFilePath;
    }
    
}
