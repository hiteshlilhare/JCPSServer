/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Hitesh
 */
public class TagCommitDetails {

    private String authorName;
    private String authorEmail;
    private String message;
    private boolean verified;
    private String verificationReason;
    private String signature;
    private String dateOfCommit;

    @Override
    public String toString() {
        return "Author Name: " + authorName + System.lineSeparator() + "Author Email: " + authorEmail
                + System.lineSeparator() + "Message: " + message + System.lineSeparator() + "Verified: "
                + verified + System.lineSeparator() + "Verification Reason: " + verificationReason
                + System.lineSeparator() + "Data of Commit: " + dateOfCommit + System.lineSeparator()
                + "Signature: " + signature;
    }

    public static void getTagCommitDetails(JsonObject commitJSONObject,
            TagCommitDetails tagCommitDetails) throws FieldNotPresentException {

        JsonElement commitJsonElement = commitJSONObject.get("commit");
        if (commitJsonElement == null || commitJsonElement.isJsonNull()) {
            throw new FieldNotPresentException("commit element/value is not present in tag's commit json");
        }
        JsonObject commitDetailJSONObject = commitJsonElement.getAsJsonObject();

        //Get Author Details.
        JsonElement tmp = commitDetailJSONObject.get("author");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("author element/value is not present in tag's commit json");
        }
        JsonObject tmpJSONObject = tmp.getAsJsonObject();

        tmp = tmpJSONObject.get("name");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("name element/value is not present in tag's"
                    + " commit author json element");
        }
        tagCommitDetails.setAuthorName(tmp.getAsString());

        tmp = tmpJSONObject.get("email");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("email element/value is not present in tag's"
                    + " commit author json element");
        }
        tagCommitDetails.setAuthorEmail(tmp.getAsString());

        tmp = tmpJSONObject.get("date");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("date element/value is not present in tag's"
                    + " commit author json element");
        }
        tagCommitDetails.setDateOfCommit(tmp.getAsString());

        // Get commit message.
        tmp = commitDetailJSONObject.get("message");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("message element/value is not present in tag's"
                    + " commit json element");
        }
        tagCommitDetails.setMessage(tmp.getAsString());

        //Verification details.
        tmp = commitDetailJSONObject.get("verification");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("verification element/value is not present in tag's commit json");
        }
        tmpJSONObject = tmp.getAsJsonObject();

        tmp = tmpJSONObject.get("verified");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("verified element/value is not present in tag's"
                    + " commit verification json element");
        }
        tagCommitDetails.setVerified(tmp.getAsBoolean());

        tmp = tmpJSONObject.get("reason");
        if (tmp == null || tmp.isJsonNull()) {
            throw new FieldNotPresentException("reason element/value is not present in tag's"
                    + " commit verification json element");
        }
        tagCommitDetails.setVerificationReason(tmp.getAsString());

        if (tagCommitDetails.isVerified()) {
            tmp = tmpJSONObject.get("signature");
            if (tmp == null || tmp.isJsonNull()) {
                throw new FieldNotPresentException("signature element/value is not present in tag's"
                        + " commit verification json element");
            }
            tagCommitDetails.setSignature(tmp.getAsString());
        }

    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(String verificationReason) {
        this.verificationReason = verificationReason;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDateOfCommit() {
        return dateOfCommit;
    }

    public void setDateOfCommit(String dateOfCommit) {
        this.dateOfCommit = dateOfCommit;
    }

}
