/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.metadata.bean;

import java.util.ArrayList;

/**
 *
 * @author Hitesh
 */
public class CardAppMetaData {

    private String name;
    private String appName;
    private String website;
    private final ArrayList<Author> authors = new ArrayList<>();
    private String company;
    private String license;
    private String capFile;
    private String cartIconFile;
    private String descIconFile;
    private String version;
    private String signedBy;
    private String releaseDate;
    private String description;
    private String appstore_repository;
    private String appsource_repository;
    private final ArrayList<String> features = new ArrayList<>();
    private final ArrayList<String> repositories = new ArrayList<>();
    private final ArrayList<String> discussions = new ArrayList<>();
    private final ArrayList<User> users = new ArrayList<>();

    @Override
    public String toString() {
        return "AppName:" + name + "\nweb-site:" + website + "\ncompany:"
                + company + "\nlicense:" + license + "\nAuthors:" + authors
                + "\ncap-fie:" + capFile + "\ncart-icon:" + cartIconFile
                + "\ndesc-icon:" + descIconFile + "\nversion:" + version
                + "\nSigned By:" + signedBy + "\nRelease Date:" + releaseDate
                + "\nDescription:" + description + "\nfeatupes:" + features
                + "\nRepositories:" + repositories + "\nDiscussion:"
                + discussions + "\nUsers:" + users + "\nappstore-repository:"
                + appstore_repository + "\nappsource-repository:"
                + appsource_repository + "\napp-name:" + appName;
    }

    public String getID() {
        return company + "/" + appName + "/" + version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getCapFile() {
        return capFile;
    }

    public void setCapFile(String capFile) {
        this.capFile = capFile;
    }

    public String getCartIconFile() {
        return cartIconFile;
    }

    public void setCartIconFile(String cartIconFile) {
        this.cartIconFile = cartIconFile;
    }

    public String getDescIconFile() {
        return descIconFile;
    }

    public void setDescIconFile(String descIconFile) {
        this.descIconFile = descIconFile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addAuthor(Author author) {
        authors.add(author);
    }

    public ArrayList<Author> getAuthors() {
        return authors;
    }

    public void addFeature(String feature) {
        features.add(feature);
    }

    public ArrayList<String> getFeatures() {
        return features;
    }

    public void addRepository(String repository) {
        repositories.add(repository);
    }

    public ArrayList<String> getRepositories() {
        return repositories;
    }

    public String getAppstoreRepository() {
        return appstore_repository;
    }

    public void setAppstoreRepository(String appstore_repository) {
        this.appstore_repository = appstore_repository;
    }

    public String getAppsourceRepository() {
        return appsource_repository;
    }

    public void setAppsourceRepository(String appsource_repository) {
        this.appsource_repository = appsource_repository;
    }

    public void addDiscussion(String discussion) {
        discussions.add(discussion);
    }

    public ArrayList<String> getDiscussions() {
        return discussions;
    }

    public void addUser(User user) {
        users.add(user);
    }
}
