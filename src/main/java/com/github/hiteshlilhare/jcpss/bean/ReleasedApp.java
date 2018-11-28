package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;

/**
 *
 * @author Hitesh
 */
public class ReleasedApp {

    public enum TableField {
        AppName, Version, DeveloperId, ReleaseDate, CloneURL, SourceCloneURL, Rating, TestedOn, Status, Remarks
    }

    //Corresponds to RepoName in repo_details table.
    private String appName;
    //Corresponds to TagName in github_releases table.
    private String version;
    //Corresponds to RepoUserId in repo_details table.
    private String developerId;
    //Corresponds to PublishedAt in github_releases table. 
    private String releaseDate;
    //clone url of the App Store Repository.
    private String cloneURL;
    //clone url for source repository in github.
    private String sourceCloneURL;
    //Corresponds to PublishedAt in github_releases table. 
    private float rating;
    //List of cards on which App is tested. 
    private final ArrayList<String> testedOn = new ArrayList<>();
    //Corresponds to Status in github_releases table and used for updating status of 
    //release in github_releases table.
    private GitHubRelease.Status status = GitHubRelease.Status.Unknown;
    //Corresponds to remearks in github_releases table and used for updating remarks for
    //releases in github_releases table.
    private String remarks;

    @Override
    public String toString() {
        return "App Name: " + appName + System.lineSeparator() + " App Version: " + version
                + System.lineSeparator() + "Developer ID: " + developerId + System.lineSeparator()
                + "Relesae Date: " + releaseDate + System.lineSeparator() + "Clone URL: " + cloneURL
                + System.lineSeparator() + "Source Clone URL: " + sourceCloneURL + System.lineSeparator()
                + "Tested On: " + testedOn + System.lineSeparator() + "Rating: " + rating + System.lineSeparator()
                + "Status: " + status + System.lineSeparator() + "Remarks: " + remarks;
    }

    /**
     * Creates ReleasedApp bean object from json.
     *
     * @param json
     * @return
     * @throws FieldNotPresentException
     */
    public static ReleasedApp createReleasedAppBean(String json)
            throws FieldNotPresentException {
        ReleasedApp releasedApp = new ReleasedApp();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        //Set release app name.
        JsonElement jsonElement = jsonObject.get("release-app-name");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-app-name field is not present in releases json");
        } else {
            releasedApp.setAppName(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Set release app version.
        jsonElement = jsonObject.get("release-app-version");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-app-version field is not present in releases json");
        } else {
            releasedApp.setVersion(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Set developer id.
        jsonElement = jsonObject.get("developer-id");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("developer-id field is not present in releases json");
        } else {
            releasedApp.setDeveloperId(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Set Clone URL
        jsonElement = jsonObject.get("release-app-cloneurl");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-app-cloneurl field is not present in releases json");
        } else {
            releasedApp.setCloneURL(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Set release date
        jsonElement = jsonObject.get("release-date");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-date field is not present in releases json");
        } else {
            releasedApp.setReleaseDate(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Set release status
        jsonElement = jsonObject.get("release-status");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-status field is not present in releases json");
        } else {
            releasedApp.setStatus(jsonElement.isJsonNull()
                    ? GitHubRelease.Status.Unknown.toString()
                    : jsonElement.getAsString());
        }
        //Will display github_releases table remarks.
        jsonElement = jsonObject.get("release-remarks");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-remarks field is not present in releases json");
        } else {
            releasedApp.setRemarks(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
        }
        //Will be same as github_releases table rating
        jsonElement = jsonObject.get("release-app-rating");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-app-rating field is not present in releases json");
        } else {
            releasedApp.setRating(jsonElement.isJsonNull() ? 0.0f : jsonElement.getAsFloat());
        }
        //Set release tested on 
        jsonElement = jsonObject.get("release-tested-on");
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new FieldNotPresentException("release-tested-on field is not present in releases json");
        } else {
            if (!jsonElement.isJsonNull()) {
                Gson googleJson = new Gson();
                ArrayList<String> javaArrayListFromGSON
                        = googleJson.fromJson(jsonElement.getAsJsonArray(),
                                ArrayList.class);
                for (String cardName : javaArrayListFromGSON) {
                    releasedApp.addJavaCardTestedOn(cardName);
                }
            }
        }
        return releasedApp;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(String developerId) {
        this.developerId = developerId;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCloneURL() {
        return cloneURL;
    }

    public void setCloneURL(String cloneURL) {
        this.cloneURL = cloneURL;
    }

    public String getSourceCloneURL() {
        return sourceCloneURL;
    }

    public void setSourceCloneURL(String sourceCloneURL) {
        this.sourceCloneURL = sourceCloneURL;
    }

    /**
     * Returns rating.
     *
     * @return
     */
    public float getRating() {
        return rating;
    }

    /**
     * Sets rating.
     *
     * @param rating
     */
    public void setRating(float rating) {
        this.rating = rating;
    }

    /**
     * Returns list of java cards on which app is tested.
     *
     * @return
     */
    public ArrayList<String> getTestedOn() {
        ArrayList<String> list = new ArrayList<>();
        for (String cardName : testedOn) {
            list.add(cardName);
        }
        return list;
    }

    public void setTestedOn(String jsonArray) {
        testedOn.clear();
        Gson googleJson = new Gson();
        ArrayList<String> javaArrayListFromGSON = googleJson.fromJson(jsonArray, ArrayList.class);
        for (String javacardName : javaArrayListFromGSON) {
            testedOn.add(javacardName);
        }
    }

    /**
     * Add Java card name on which app is tested.
     *
     * @param cardName
     */
    public void addJavaCardTestedOn(String cardName) {
        testedOn.add(cardName);
    }

    /**
     * Returns status.
     *
     * @return
     */
    public String getStatus() {
        return status.toString();
    }

    /**
     * Sets status.
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = GitHubRelease.Status.getStatus(status);
    }

    /**
     * Returns remarks.
     *
     * @return
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Set remarks.
     *
     * @param remarks
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns create table SQL statement for creation of database table.
     *
     * @return
     */
    public static String getCreateTableStatement() {
        return "create table if not exists released_app ( "
                + TableField.AppName + " varchar(200) NOT NULL, "
                + TableField.Version + " varchar(200) NOT NULL, "
                + TableField.DeveloperId + " varchar(200) NOT NULL, "
                + TableField.ReleaseDate + " varchar(100) NOT NULL, "
                + TableField.CloneURL + " varchar(200), "
                + TableField.SourceCloneURL + " varchar(200) NOT NULL, "
                + TableField.TestedOn + " varchar(1000), "
                + TableField.Rating + " DECIMAL(10,2) NOT NULL DEFAULT '0.00' , "
                + TableField.Status + " varchar(100) NOT NULL, "
                + TableField.Remarks + " varchar(1000), "
                + "CONSTRAINT pk_released_app PRIMARY KEY ("
                + TableField.SourceCloneURL + ", " + TableField.Version + "),"
                + " FOREIGN KEY (" + TableField.SourceCloneURL + ", " + TableField.Version
                + ") REFERENCES github_releases("
                + GitHubRelease.TableField.CloneURL + ", " + GitHubRelease.TableField.TagName
                + ") ON DELETE NO ACTION )";
    }

    /**
     * Returns JavaCrad names as JSON array.
     *
     * @return
     */
    public String getTestedOnJsonArray() {
        Gson gsonBuilder = new GsonBuilder().create();
        return gsonBuilder.toJson(testedOn);
    }

    /**
     * Returns insert SQL statement wrt github_releases table.
     *
     * @return
     */
    public String getInsertStatement() {
        return "insert into released_app ( "
                + TableField.AppName + ", "
                + TableField.Version + ", "
                + TableField.DeveloperId + ", "
                + TableField.ReleaseDate + ", "
                + TableField.CloneURL + ", "
                + TableField.SourceCloneURL + ", "
                + TableField.TestedOn + ", "
                + TableField.Rating + ", "
                + TableField.Status + ", "
                + TableField.Remarks + ") values ( "
                + (appName == null ? " NULL " : "'" + appName + "'") + " , "
                + (version == null ? " NULL " : "'" + version + "'") + " , "
                + (developerId == null ? " NULL " : "'" + developerId + "'") + " , "
                + (releaseDate == null ? " NULL " : "'" + releaseDate + "'") + " , "
                + (cloneURL == null ? " NULL " : "'" + cloneURL + "'") + " , "
                + (sourceCloneURL == null ? " NULL " : "'" + sourceCloneURL + "'") + " , "
                + "'" + getTestedOnJsonArray() + "' , "
                + rating + " , "
                + (status == null ? " NULL " : "'" + status.toString() + "'") + " , "
                + (remarks == null ? " NULL " : "'" + remarks + "'") + " ) ";
    }

    /**
     * Returns select SQL statement wrt released_app table.
     *
     * @return
     */
    public String getSelectStatement() {
        return "select * from released_app where "
                + ReleasedApp.TableField.Version.toString() + " = "
                + (version == null ? "NULL" : "'" + version + "' and ")
                + ReleasedApp.TableField.SourceCloneURL.toString() + " = "
                + (sourceCloneURL == null ? "NULL" : "'" + sourceCloneURL + "'");
    }

    /**
     * Returns update SQL statement wrt released_app table.
     *
     * @return
     */
    public String getUpdateRemarksAndTestedOnFieldStatement() {
        return "update released_app set " + TableField.Remarks.toString()
                + "= '" + remarks + "' , " + TableField.TestedOn.toString()
                + "= '" + getTestedOnJsonArray() + "' where "
                + ReleasedApp.TableField.Version.toString() + " = "
                + (version == null ? "NULL" : "'" + version + "' and ")
                + ReleasedApp.TableField.SourceCloneURL.toString() + " = "
                + (sourceCloneURL == null ? "NULL" : "'" + sourceCloneURL + "'");
    }

    /**
     * Returns select statement wrt given field name and value.
     * @param fieldValue
     * @param fieldName
     * @return 
     */
    public static String getSelectStatement(String fieldValue, ReleasedApp.TableField fieldName) {
        return "select * from released_app where "
                + fieldName.toString() + " = '" + fieldValue + "'";
    }
    
    /**
     * Returns SQL statement for getting all records.
     * @return
     */
    public static String getSelectAllStatement() {
        return "select * from released_app";
    }

}
