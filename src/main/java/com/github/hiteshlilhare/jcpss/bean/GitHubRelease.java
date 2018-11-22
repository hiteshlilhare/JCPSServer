package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Bean w.r.t. GitHub Release Json response.
 *
 * @author Hitesh
 */
public class GitHubRelease {

    /**
     * github_releases table fileds.
     */
    public enum TableField {
        Title, TagName, Description, CloneURL, ReleaseURL, PublishedAt, CreatedAt, TarballURL, ZipballURL,
        TagSignatureVerified, AuthorLogin, Status, Rating, Remarks
    }
    
    public enum Status {
        PreVerified, Verified, Unknown, Invalid, Rejected;

        public static Status getStatus(String status) {
            if (PreVerified.toString().equalsIgnoreCase(status)) {
                return PreVerified;
            } else if (Verified.toString().equalsIgnoreCase(status)) {
                return Verified;
            } else if (Invalid.toString().equalsIgnoreCase(status)) {
                return Invalid;
            } else if (Rejected.toString().equalsIgnoreCase(status)) {
                return Rejected;
            } else {
                return Unknown;
            }
        }
    }

    //Corresponding JSON field "repository" : "clone_url"
    private String cloneUrl;
    //Corresponding JSON field "releaseUrl"
    private String releaseUrl;
    //Corresponding JSON field "name", it is title of release.
    private String name;
    //Corresponding JSON field "tag_name"
    private String tagname;
    //Corresponding JSON field "published_at"
    private String publishedAt;
    //Corresponding JSON field "created_at" it is date to commit
    private String createdAt;
    //Corresponding JSON field "tarball_url"
    private String tarballURL;
    //Corresponding JSON field "zipball_url"
    private String zipballURL;
    //Corresponding JSON field "body", it is description of release
    private String description;
    //Corresponding JSON field "prerelease"
    private String prerelease;
    //represents whether annonated tag signature tagSignatureVerified or not.
    private boolean tagSignatureVerified;
    //Corrsponding JSON field author-->login
    private String authorLogin;
    //represents status of release.
    private Status status;
    //Rating (User Defined field do not add into createGitHubReleaseBean())
    private float rating;
    //remarks 
    private String remarks;

    public GitHubRelease() {
        tagSignatureVerified = false;
        status = Status.Unknown;
    }

    public static GitHubRelease createGitHubReleaseBean(JsonObject releaseJSONObject) throws FieldNotPresentException {
        GitHubRelease release = new GitHubRelease();
        //set prerelease
        JsonElement tmp = releaseJSONObject.get("prerelease");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setPrerelease(tmp.isJsonNull() ? null : tmp.getAsString());
        //set clone url 
        //use "html_url" element of "release"
        tmp = releaseJSONObject.get("html_url");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        if (!tmp.isJsonNull()) {
            String htmlUrl = tmp.getAsString();
            release.setCloneUrl(htmlUrl.substring(0, htmlUrl.indexOf("/releases")) + ".git");
        } else {
            release.setCloneUrl(null);
        }
        //set releaseUrl
        tmp = releaseJSONObject.get("url");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setReleaseUrl(tmp.isJsonNull() ? null : tmp.getAsString());
        //set name
        tmp = releaseJSONObject.get("name");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setName(tmp.isJsonNull() ? null : tmp.getAsString());
        //set tag name
        tmp = releaseJSONObject.get("tag_name");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setTagname(tmp.isJsonNull() ? null : tmp.getAsString());
        //set published at
        tmp = releaseJSONObject.get("published_at");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setPublishedAt(tmp.isJsonNull() ? null : tmp.getAsString());

        //LocalDate date = OffsetDateTime.parse(tmp.getAsString()).toLocalDate();
        //set created at
        tmp = releaseJSONObject.get("created_at");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setCreatedAt(tmp.isJsonNull() ? null : tmp.getAsString());
        //set tarball releaseUrl
        tmp = releaseJSONObject.get("tarball_url");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setTarballURL(tmp.isJsonNull() ? null : tmp.getAsString());
        //set zipball releaseUrl
        tmp = releaseJSONObject.get("zipball_url");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setZipballURL(tmp.isJsonNull() ? null : tmp.getAsString());
        //set description
        tmp = releaseJSONObject.get("body");
        if (tmp == null) {
            throw new FieldNotPresentException("html_url field is not present in github release json");
        }
        release.setDescription(tmp.isJsonNull() ? null : tmp.getAsString());
        //set author login.
        tmp = releaseJSONObject.get("author");
        if (tmp == null) {
            throw new FieldNotPresentException("author field is not present in github release json");
        }
        JsonObject authorJsonObject = tmp.getAsJsonObject();
        tmp = authorJsonObject.get("login");
        if (tmp == null) {
            throw new FieldNotPresentException("login field is not present in github release json's author json");
        }
        release.setAuthorLogin(tmp.isJsonNull() ? null : tmp.getAsString());
        //Don't add user defined fields
        return release;
    }

    @Override
    public String toString() {
        return "Presrelease:" + prerelease + System.lineSeparator() + "clone url:"
                + cloneUrl + System.lineSeparator() + "url:"
                + releaseUrl + System.lineSeparator() + "name:" + name + System.lineSeparator()
                + "tag name:" + tagname + System.lineSeparator() + "published at:" + publishedAt
                + System.lineSeparator() + "created at:" + createdAt + System.lineSeparator()
                + "tarball url:" + tarballURL + System.lineSeparator() + "zipball url:"
                + zipballURL + System.lineSeparator() + "Description:" + description
                + System.lineSeparator() + "Author Login:" + authorLogin + System.lineSeparator()
                + "Tag Signature Verified:" + tagSignatureVerified + System.lineSeparator() + "Status:"
                + status.toString() + System.lineSeparator() + "Remarks:" + remarks;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public String getReleaseUrl() {
        return releaseUrl;
    }

    public void setReleaseUrl(String url) {
        this.releaseUrl = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagname() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname = tagname;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTarballURL() {
        return tarballURL;
    }

    public void setTarballURL(String tarballURL) {
        this.tarballURL = tarballURL;
    }

    public String getZipballURL() {
        return zipballURL;
    }

    public void setZipballURL(String zipballURL) {
        this.zipballURL = zipballURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerelease() {
        return prerelease;
    }

    public void setPrerelease(String prerelease) {
        this.prerelease = prerelease;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public void setAuthorLogin(String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public boolean isTagSignatureVerified() {
        return tagSignatureVerified;
    }

    public void setTagSignatureVerified(boolean tagSignatureVerified) {
        this.tagSignatureVerified = tagSignatureVerified;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
    
    /**
     * Returns create table SQL statement for creation of database table.
     *
     * @return
     */
    public static String getCreateTableStatement() {
        return "create table if not exists github_releases ( "
                + TableField.Title + " varchar(200) NOT NULL, "
                + TableField.TagName + " varchar(200) NOT NULL, "
                + TableField.Description + " varchar(600), "
                + TableField.CloneURL + " varchar(200) NOT NULL, "
                + TableField.ReleaseURL + " varchar(200) NOT NULL, "
                + TableField.PublishedAt + " varchar(100) NOT NULL, "
                + TableField.CreatedAt + " varchar(100) NOT NULL, "
                + TableField.TarballURL + " varchar(200) NOT NULL, "
                + TableField.ZipballURL + " varchar(200) NOT NULL, "
                + TableField.TagSignatureVerified + " varchar(100), "
                + TableField.AuthorLogin + " varchar(200) NOT NULL, "
                + TableField.Status + " varchar(100), "
                + TableField.Rating + " DECIMAL(10,2) NOT NULL DEFAULT '0.00' , "
                + TableField.Remarks + " varchar(1000), "
                + "CONSTRAINT pk_github_releases PRIMARY KEY (" + TableField.CloneURL +", "+ TableField.TagName +"),"
                + " FOREIGN KEY (" + TableField.CloneURL + ") REFERENCES repo_details("
                + RepoDetail.TableField.RepoURL + ") ON DELETE CASCADE "
                + ")";
    }

    /**
     * Returns insert SQL statement wrt github_releases table.
     *
     * @return
     */
    public String getInsertStatement() {
        return "insert into github_releases ( "
                + TableField.Title + ", "
                + TableField.TagName + ", "
                + TableField.Description + ", "
                + TableField.CloneURL + ", "
                + TableField.ReleaseURL + ", "
                + TableField.PublishedAt + ", "
                + TableField.CreatedAt + ", "
                + TableField.TarballURL + ", "
                + TableField.ZipballURL + ", "
                + TableField.TagSignatureVerified + ", "
                + TableField.AuthorLogin + ", "
                + TableField.Status + ", "
                + TableField.Remarks + ") values ( "
                + (name == null ? " NULL " : "'" + name + "'") + " , "
                + (tagname == null ? " NULL " : "'" + tagname + "'") + " , "
                + (description == null ? " NULL " : "'" + description + "'") + " , "
                + (cloneUrl == null ? " NULL " : "'" + cloneUrl + "'") + " , "
                + (releaseUrl == null ? " NULL " : "'" + releaseUrl + "'") + " , "
                + (publishedAt == null ? " NULL " : "'" + publishedAt + "'") + " , "
                + (createdAt == null ? " NULL " : "'" + createdAt + "'") + " , "
                + (tarballURL == null ? " NULL " : "'" + tarballURL + "'") + " , "
                + (zipballURL == null ? " NULL " : "'" + zipballURL + "'") + " , "
                + "'" + tagSignatureVerified + "' , "
                + (authorLogin == null ? " NULL " : "'" + authorLogin + "'") + " , "
                + (status == null ? " NULL " : "'" + status.toString() + "'") + " , "
                + (remarks == null ? " NULL " : "'" + remarks + "'") + " ) ";
    }

    /**
     * Returns select SQL statement wrt github_releases table.
     *
     * @return
     */
    public String getSelectStatement() {
        return "select * from github_releases where "
                + TableField.TagName.toString() + " = "
                + (tagname == null ? "NULL" : "'" + tagname + "' and ")
                + TableField.CloneURL.toString() + " = "
                + (cloneUrl==null ? "NULL" : "'" + cloneUrl + "'");
    }

    public static String getSelectStatement(String fieldValue, TableField fieldName) {
        return "select * from github_releases where "
                + fieldName.toString() + " = '" + fieldValue + "'";
    }

    /**
     * Returns SQL delete statement wrt github_releases table.
     *
     * @return
     */
    public String getDeleteStatement() {
        return "delete from github_releases where "
                + TableField.TagName + " = "
                + (tagname == null ? "NULL" : "'" + tagname + "' and ")
                + TableField.CloneURL.toString() + " = "
                + (cloneUrl==null ? "NULL" : "'" + cloneUrl + "'");
    }
    
    /**
     * Returns update statement for updating status & remarks field of github_releases
     * table.
     *
     * @return
     */
    public String getUpdateStatusAndRemarksStatement() {
        return "update github_releases set " + TableField.Status.toString()
                + "= '" + status + "' , " + TableField.Remarks.toString()
                +"= '"+ remarks+"' where " + TableField.TagName + " = '"
                + tagname + "' and " + TableField.CloneURL + " = '"
                + cloneUrl + "'";
    }

}
