package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.exception.UnexpectedInputLengthException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Bean corresponding to database table repo_details. Learning : JsonElement ==
 * null if element not present in the JSON String JsonElement.isJsonNull == true
 * is value corresponding to json element is not present in the json string
 * {arr:[]} : JsonElement.isJsonNull will be true; {name:"xyz"} : JsonElement =
 * JsonElement.get("lastname"): JsonElement will be null
 *
 * @author Hitesh
 */
public class RepoDetail {

    /**
     * Table Field enum.
     */
    public enum TableField {
        RepoUserID, RepoName, RepoURL, SignKeyFgrPrint, KeyServerURL, EmailID,
        Verified, Request, ClientID, AnchorTag, WebhookEncSecret, WebhookSecret, Remarks
    }

    /**
     * Requested operation.
     */
    public enum Request {
        Verify, Register, Delete, SendEmail, Unknown, Done;

        public static Request getRequest(String request) {
            if (Verify.toString().equalsIgnoreCase(request)) {
                return Verify;
            } else if (Delete.toString().equalsIgnoreCase(request)) {
                return Delete;
            } else if (SendEmail.toString().equalsIgnoreCase(request)) {
                return SendEmail;
            } else if (Done.toString().equalsIgnoreCase(request)) {
                return Done;
            } else {
                return Unknown;
            }
        }
    }

    /**
     * Verification status of Repository Detail.
     */
    public enum Status {
        Initial, Verified, ClientVerified, Registered, PreRegistered, Rejected, Deleted, Unknown;

        public static Status getStatus(String status) {
            if (Initial.toString().equalsIgnoreCase(status)) {
                return Initial;
            } else if (Verified.toString().equalsIgnoreCase(status)) {
                return Verified;
            } else if (ClientVerified.toString().equalsIgnoreCase(status)) {
                return ClientVerified;
            } else if (Rejected.toString().equalsIgnoreCase(status)) {
                return Rejected;
            } else if (Deleted.toString().equalsIgnoreCase(status)) {
                return Deleted;
            } else if (Registered.toString().equalsIgnoreCase(status)) {
                return Registered;
            } else if (PreRegistered.toString().equalsIgnoreCase(status)) {
                return PreRegistered;
            } else {
                return Unknown;
            }
        }
    };

    /**
     * Repository User ID.
     */
    private String repoUserID;
    /**
     * Repository Name.
     */
    private String repoName;
    /**
     * Repository URL.
     */
    private String repoURL;
    /**
     * Public key finger print.
     */
    private String signKeyFgrPrint;
    /**
     * Key server for public key.
     */
    private String keyServeURL;
    /**
     * User email address.
     */
    private String email;
    /**
     * Whether repository verified.
     */
    private Status verified;
    /**
     * Requested operation.
     */
    private Request request;
    /**
     * Client ID used for Client Verification.
     */
    private String clientID;
    /**
     * Release tag from where to repository got registered.
     */
    private String anchorTag;
    /**
     * Encrypted webhook secret.
     */
    private String webhookEncSecretFileName;
    /**
     * Remarks (Will be used for explanation of particular operation done).
     */
    private String remarks;

    @Override
    public String toString() {
        return "Repo User ID: " + repoUserID + System.lineSeparator() + "Repo Name: " + repoName + System.lineSeparator()
                + "Repo URL: " + repoURL + System.lineSeparator()
                + "Sign Key Fingerprint: " + signKeyFgrPrint + System.lineSeparator() + "Key Server URL: " + keyServeURL
                + System.lineSeparator() + "E-Mail: " + email + System.lineSeparator() + "Verified: " + verified
                + System.lineSeparator() + "Request: " + request + System.lineSeparator()
                + "Client ID: " + clientID + System.lineSeparator() + "Anchor Tag: "
                + anchorTag + System.lineSeparator() + "Webhook Encrypted Secret File:"
                + webhookEncSecretFileName + System.lineSeparator() + "Remarks: " + remarks;
    }

    /**
     * Constructor for initializing verified status to false.
     */
    //Note: Do not initialize variable in constructor as this bean will be used for 
    //querying database too. 
//    public RepoDetail() {
//        verified = Status.Initial;
//        request = Request.Verify;
//    }
    /**
     * Creates RepoDetail object from json string.
     *
     * @param json
     * @return
     * @throws
     * com.github.hiteshlilhare.jcpss.exception.UnexpectedInputLengthException
     */
    public static RepoDetail createRepoDetailBean(String json) 
            throws UnexpectedInputLengthException {
        RepoDetail repoDetail = new RepoDetail();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();

        JsonElement jsonElement = jsonObject.get("github-user-id");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Repository user id is of unexpected length");
            } else {
                repoDetail.setRepoUserID(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setRepoUserID(null);
        }

        jsonElement = jsonObject.get("github-repo-name");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Repository name is of unexpected length");
            } else {
                repoDetail.setRepoName(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setRepoName(null);
        }

        jsonElement = jsonObject.get("github-repo-url");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Repository url is of unexpected length");
            } else {
                repoDetail.setRepoURL(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setRepoURL(null);
        }

        jsonElement = jsonObject.get("sign-key-fingerprint");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Signature key fingerpring is of unexpected length");
            } else {
                repoDetail.setSignKeyFingerPrint(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setSignKeyFingerPrint(null);
        }

        jsonElement = jsonObject.get("key-server-url");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Key server url is of unexpected length");
            } else {
                repoDetail.setKeyServeURL(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setKeyServeURL(null);
        }

        jsonElement = jsonObject.get("email");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull()
                    && jsonElement.getAsString() != null
                    && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("E-mail id is of unexpected length");
            } else {
                repoDetail.setEmail(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setEmail(null);
        }

        jsonElement = jsonObject.get("verified");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull()
                    && jsonElement.getAsString() != null
                    && jsonElement.getAsString().length() > 50) {
                throw new UnexpectedInputLengthException("Verified status is of unexpected length");
            } else {
                repoDetail.setVerified(jsonElement.isJsonNull() ? Status.Unknown
                        : Status.getStatus(jsonElement.getAsString()));
            }
        } else {
            repoDetail.setVerified(Status.Unknown);
        }

        jsonElement = jsonObject.get("request");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 50) {
                throw new UnexpectedInputLengthException("Service request is of unexpected length");
            } else {
                repoDetail.setRequest(jsonElement.isJsonNull() ? Request.Unknown
                        : Request.getRequest(jsonElement.getAsString()));
            }
        } else {
            repoDetail.setRequest(Request.Unknown);
        }

        jsonElement = jsonObject.get("clientid");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Client ID is of unexpected length");
            } else {
                repoDetail.setClientID(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setClientID(null);
        }

        jsonElement = jsonObject.get("anchortag");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 200) {
                throw new UnexpectedInputLengthException("Anchor tag is of unexpected length");
            } else {
                repoDetail.setAnchorTag(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setAnchorTag(null);
        }

        jsonElement = jsonObject.get("webhook-enc-secret");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 300) {
                throw new UnexpectedInputLengthException("Webhook encrypted secret is of unexpected length");
            } else {
                repoDetail.setWebhookEncSecretFileName(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setWebhookEncSecretFileName(null);
        }

        jsonElement = jsonObject.get("remarks");
        if (jsonElement != null) {
            if (!jsonElement.isJsonNull() && jsonElement.getAsString() != null && jsonElement.getAsString().length() > 500) {
                throw new UnexpectedInputLengthException("Remarks is of unexpected length");
            } else {
                repoDetail.setRemarks(jsonElement.isJsonNull() ? null : jsonElement.getAsString());
            }
        } else {
            repoDetail.setRemarks(null);
        }

        return repoDetail;
    }

    /**
     * Returns repo user id.
     *
     * @return
     */
    public String getRepoUserID() {
        return repoUserID;
    }

    /**
     * Set repo user id.
     *
     * @param repoUserID
     */
    public void setRepoUserID(String repoUserID) {
        this.repoUserID = repoUserID;
    }

    /**
     * Returns Repository name.
     *
     * @return
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * Sets Repository name.
     *
     * @param repoName
     */
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    /**
     * returns repo url.
     *
     * @return
     */
    public String getRepoURL() {
        return repoURL;
    }

    /**
     * Sets repo url.
     *
     * @param repoURL
     */
    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    /**
     * Returns signature key (public key) fingerprint.
     *
     * @return
     */
    public String getSignKeyFgrPrint() {
        return signKeyFgrPrint;
    }

    /**
     * Sets signature key fingerprint.
     *
     * @param signKeyFgrPrint
     */
    public void setSignKeyFingerPrint(String signKeyFgrPrint) {
        if (signKeyFgrPrint != null) {
            signKeyFgrPrint = signKeyFgrPrint.replaceAll("\\s+", "");
        }
        this.signKeyFgrPrint = signKeyFgrPrint;
    }

    /**
     * Returns signature key server url.
     *
     * @return
     */
    public String getKeyServeURL() {
        return keyServeURL;
    }

    /**
     * Sets key server url.
     *
     * @param keyServeURL
     */
    public void setKeyServeURL(String keyServeURL) {
        this.keyServeURL = keyServeURL;
    }

    /**
     * Returns user email address.
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets use email id.
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns repository verified status.
     *
     * @return
     */
    public Status getVerified() {
        return verified;
    }

    /**
     * Sets repository verified status.
     *
     * @param verified
     */
    public void setVerified(Status verified) {
        this.verified = verified;
    }

    /**
     * Returns requested operation on registered repository.
     *
     * @return
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Sets requested operation on registered repository.
     *
     * @param request
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Returns Client ID for client.
     *
     * @return
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Sets the client id for user.
     *
     * @param clientID
     */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getAnchorTag() {
        return anchorTag;
    }

    public void setAnchorTag(String anchorTag) {
        this.anchorTag = anchorTag;
    }

    public String getWebhookEncSecretFileName() {
        return webhookEncSecretFileName;
    }

    public void setWebhookEncSecretFileName(String webhookEncSecretFileName) {
        this.webhookEncSecretFileName = webhookEncSecretFileName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns create table SQL statement wrt repo_details.
     *
     * @return
     */
    public static String getCreateTableStatement() {
        return "create table if not exists repo_details "
                + "(RepoUserID varchar(200) NOT NULL,RepoName varchar(200) NOT NULL,RepoURL varchar(200) NOT NULL,SignKeyFgrPrint varchar(200) NOT NULL,"
                + "KeyServerURL varchar(200) NOT NULL,EmailID varchar(200) NOT NULL,Verified varchar(50)  NOT NULL,Request varchar(50)  NOT NULL,"
                + "ClientID varchar(200),AnchorTag varchar(200), WebhookEncSecret LONGBLOB NULL, WebhookSecret varchar(300) NULL,Remarks varchar(500),"
                + " PRIMARY KEY (RepoURL))";
    }

    /**
     * Returns insert statement w.r.t. corresponding table in database. Note:
     * Error due to null/empty values are supposed to be handled by
     * corresponding database method.
     *
     * @return
     */
    public String getSQLInsertStatement() {
        return "insert into repo_details ( RepoUserID,RepoName,RepoURL,SignKeyFgrPrint,KeyServerURL,"
                + "EmailID,Verified, Request, ClientID, AnchorTag, Remarks) values ("
                + (repoUserID == null ? "NULL" : "'" + repoUserID + "'")
                + " , " + (repoName == null ? "NULL" : "'" + repoName + "'")
                + " , " + (repoURL == null ? "NULL" : "'" + repoURL + "'")
                + " , " + (signKeyFgrPrint == null ? "NULL" : "'" + signKeyFgrPrint + "'")
                + " , " + (keyServeURL == null ? "NULL" : "'" + keyServeURL + "'")
                + " , " + (email == null ? "NULL" : "'" + email + "'")
                + " , '" + (verified == null ? Status.Unknown.toString() : verified.toString())
                + "' , '" + (request == null ? Request.Unknown : request.toString())
                + "' , " + (clientID == null ? "NULL" : "'" + clientID + "'")
                + " , " + (anchorTag == null ? "NULL" : "'" + anchorTag + "'")
                + " , " + (remarks == null ? "NULL" : "'" + remarks + "'") + " )";
    }

    /**
     * Returns update statement for updating Request field of repo_details
     * table.
     *
     * @return
     */
    public String getUpdateRequestFieldStatement() {
        return "update repo_details set Request = '" + request.toString() + "' where RepoURL = '"
                + repoURL + "'";
    }

    /**
     * Returns update statement for updating ClientID field of repo_details
     * table.
     *
     * @return
     */
    public String getUpdateClientIDFieldStatement() {
        return "update repo_details set ClientID = '" + clientID + "' where RepoURL = '"
                + repoURL + "'";
    }

    /**
     * Returns update statement fro updating AnchorTag field of repo_details
     * table.
     *
     * @return
     */
    public String getUpdateAnchorTagFieldStatement() {
        return "update repo_details set " + TableField.AnchorTag.toString()
                + "= '" + anchorTag + "' where " + TableField.RepoURL + " = '"
                + repoURL + "'";
    }

    /**
     * Returns select statement w.r.t. corresponding table in database. Note:
     * Error due to null/empty values are supposed to be handled by
     * corresponding database method.
     *
     * @return
     */
    public String getSelectStatement() {
        return "select * from repo_details where RepoURL='" + (repoURL == null ? "" : repoURL) + "'";
    }

    /**
     * Returns select statement w.r.t. client id. Note: Error due to null/empty
     * values are supposed to be handled by corresponding database method.
     *
     * @return
     */
    public String getSelectStatementWRTClientID() {
        return "select * from repo_details where ClientID='" + (clientID == null ? "" : clientID) + "'";
    }

    /**
     * Returns select statement w.r.t. repo url. Note: Error due to null/empty
     * values are supposed to be handled by corresponding database method.
     *
     * @return
     */
    public String getSelectStatementWRTRepoURL() {
        return "select * from repo_details where " + TableField.RepoURL
                + "= '" + (repoURL == null ? "" : repoURL) + "'";
    }

    /**
     * Returns select statement w.r.t. given table field and value. Note: Error
     * due to null/empty values are supposed to be handled by corresponding
     * database method.
     *
     * @param fieldValue
     * @param field
     * @return
     */
    public static String getSelectStatement(String fieldValue, TableField field) {
        return "select * from repo_details where " + field.toString()
                + "= '" + fieldValue + "'";
    }

    public static String getSelectStatement(String fieldValue1, TableField field1,
            String fieldValue2, TableField field2) {
        return "select * from repo_details where " + field1.toString()
                + "= '" + fieldValue1 + "' and " + field2.toString()
                + "= '" + fieldValue2 + "'";
    }

    /**
     * Returns update statement w.r.t. given table field and value
     *
     * @param fieldValue
     * @param field
     * @param whereValue
     * @param whereField
     * @return
     */
    public static String getUpadteStatement(String fieldValue, TableField field,
            String whereValue, TableField whereField) {
        return "update repo_details set " + field.toString() + " = '" + fieldValue + "' where "
                + whereField.toString() + " = '" + whereValue + "'";
    }

    /**
     * Returns update statement for updating Verified field of repo_details
     * table.
     *
     * @return
     */
    public String getUpdateVerifyFieldWRTClientIDStatement() {
        return "update repo_details set Verified = '" + verified.toString() + "' where ClientID = '"
                + clientID + "'";
    }

    /**
     * Returns update statement for updating Remarks field of repo_details
     * table.
     *
     * @return
     */
    public String getUpdateRemarksFieldWRTClientIDStatement() {
        return "update repo_details set " + RepoDetail.TableField.Remarks + " = '"
                + remarks + "' where " + RepoDetail.TableField.ClientID + " = '"
                + clientID + "'";
    }

    /**
     * Returns update statement for updating Request field of repo_details table
     * using client id as primary key
     *
     * @return
     */
    public String getUpdateRequestFieldWRTClientIDStatement() {
        return "update repo_details set Request = '" + request.toString() + "' where ClientID = '"
                + clientID + "'";
    }

    /**
     *
     * @return
     */
    public static String getUpdateWebhookEncSecretBlobStatement() {
        return "UPDATE repo_details SET WebhookEncSecret = ? WHERE RepoURL = ? ";
    }

    /**
     *
     * @return
     */
    public static String getSelectWebhookEncSecretBlobStatement() {
        return "SELECT WebhookEncSecret FROM repo_details WHERE RepoURL = ?";
    }

    /**
     * Returns SQL delete statement w.r.t. repo URL.
     *
     * @return
     */
    public String getDeleteStatement() {
        return "delete from repo_details where " + RepoDetail.TableField.RepoURL
                + " = '" + repoURL + "'";
    }

}
