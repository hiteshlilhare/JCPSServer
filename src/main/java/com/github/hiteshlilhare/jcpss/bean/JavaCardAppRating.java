package com.github.hiteshlilhare.jcpss.bean;

/**
 *
 * @author Hitesh
 */
public class JavaCardAppRating {

    /**
     * Table Field enum.
     */
    public enum TableField {
        UserID, RepoID, ReleaseTag, Rating, Remarks
    }

    private String userID;
    //Repository URL
    private String repoID;
    private String releaseTag;
    private int rating;
    private String remarks;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRepoID() {
        return repoID;
    }

    public void setRepoID(String repoID) {
        this.repoID = repoID;
    }

    public String getReleaseTag() {
        return releaseTag;
    }

    public void setReleaseTag(String releaseTag) {
        this.releaseTag = releaseTag;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns create table SQL statement for creation of database table.
     *
     * @return
     */
    public static String getCreateTableStatement() {
        return "create table if not exists app_rating ( "
                + TableField.UserID + " varchar(200) NOT NULL, "
                + TableField.RepoID + " varchar(400) NOT NULL, "
                + TableField.ReleaseTag + " varchar(200) NOT NULL, "
                + TableField.Rating + " DECIMAL(10,2) NOT NULL DEFAULT '0.00' , "
                + TableField.Remarks + " varchar(1000), "
                + "CONSTRAINT pk_app_rating PRIMARY KEY (" + TableField.UserID + "(50) , " + TableField.RepoID + "(100) , " + TableField.ReleaseTag + "(50))"
                + " , CONSTRAINT fk_app_rating FOREIGN KEY ( " + TableField.RepoID + ", " + TableField.ReleaseTag + " ) REFERENCES github_releases( "
                + GitHubRelease.TableField.CloneURL + ", " + GitHubRelease.TableField.TagName + " )  ON DELETE NO ACTION "
                + ")";
    }

    /**
     *
     * @return
     */
    public String getInsertStatement() {
        return "insert into app_rating ( "
                + TableField.UserID + ", "
                + TableField.RepoID + ", "
                + TableField.ReleaseTag + ", "
                + TableField.Rating + ", "
                + TableField.Remarks + ") values ( "
                + (userID == null ? " NULL " : "'" + userID + "'") + " , "
                + (repoID == null ? " NULL " : "'" + repoID + "'") + " , "
                + (releaseTag == null ? " NULL " : "'" + releaseTag + "'") + " , "
                + rating + " , "
                + (remarks == null ? " NULL " : "'" + remarks + "'") + " ) ";
    }

}
