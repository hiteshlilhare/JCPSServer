/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * This bean represents the public key at Github.
 *
 * @author Hitesh
 */
public class DeveloperGPGPublicKey {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DeveloperGPGPublicKey.class);
    /**
     * Key Id.
     */
    private String keyID;
    /**
     * Raw key.
     */
    private String rawKey;
    /**
     * To keep pair like (email,verified).
     */
    private final HashMap<String, Boolean> emails = new HashMap<>();
    /**
     * To keep sub keys.
     */
    private final ArrayList<DeveloperGPGPublicKey> subkeys = new ArrayList<>();
    /**
     * Key Capabilities.
     */
    private boolean canSign;
    private boolean canEncryptComms;
    private boolean canEncryptStorage;
    private boolean canCertify;
    /**
     * creation date & time.
     */
    private String createdAt;
    /**
     * expiry date & time.
     */
    private String expiresAt;

    @Override
    public String toString() {
        return "Key ID: " + keyID + System.lineSeparator() + "Can Sign: " + canSign + System.lineSeparator()
                + "Can Encrypt Comms: " + canEncryptComms + System.lineSeparator() + "Can Encrypt Storage: "
                + canEncryptStorage + System.lineSeparator() + "Can Certify: " + canCertify + System.lineSeparator()
                + " Created At: " + createdAt + System.lineSeparator() + "Expires At: " + expiresAt + System.lineSeparator()
                + "emails: " + emails + System.lineSeparator() + "subkeys: " + subkeys;
    }

    /**
     * Creates DeveloperGPGPublicKey object from JSONObject.
     *
     * @param gpgKeyJsonObject
     * @return
     */
    public static DeveloperGPGPublicKey createDeveloperGPGPublicKeyBean(
            JsonObject gpgKeyJsonObject) throws FieldNotPresentException {
        DeveloperGPGPublicKey developerGPGPublicKey = new DeveloperGPGPublicKey();

        com.google.gson.JsonElement tmp = gpgKeyJsonObject.get("key_id");
        if (tmp == null) {
            throw new FieldNotPresentException("key_id field is not present in GPG key json");
        }
        developerGPGPublicKey.setKeyID(tmp.isJsonNull() ? null : tmp.getAsString());

        tmp = gpgKeyJsonObject.get("raw_key");
        if (tmp == null) {
            throw new FieldNotPresentException("raw_key field is not present in GPG key json");
        }
        developerGPGPublicKey.setRawKey(tmp.isJsonNull() ? null : tmp.getAsString());

        tmp = gpgKeyJsonObject.get("can_sign");
        if (tmp == null) {
            throw new FieldNotPresentException("can_sign field is not present in GPG key json");
        }
        developerGPGPublicKey.setCanKeySign(tmp.isJsonNull() ? false : tmp.getAsBoolean());

        tmp = gpgKeyJsonObject.get("can_encrypt_comms");
        if (tmp == null) {
            throw new FieldNotPresentException("can_encrypt_comms field is not present in GPG key json");
        }
        developerGPGPublicKey.setCanKeyEncryptComms(tmp.isJsonNull() ? false : tmp.getAsBoolean());

        tmp = gpgKeyJsonObject.get("can_encrypt_storage");
        if (tmp == null) {
            throw new FieldNotPresentException("can_encrypt_storage field is not present in GPG key json");
        }
        developerGPGPublicKey.setCanKeyEncryptStorage(tmp.isJsonNull() ? false : tmp.getAsBoolean());

        tmp = gpgKeyJsonObject.get("can_certify");
        if (tmp == null) {
            throw new FieldNotPresentException("can_certify field is not present in GPG key json");
        }
        developerGPGPublicKey.setCanKeyCertify(tmp.isJsonNull() ? false : tmp.getAsBoolean());

        tmp = gpgKeyJsonObject.get("created_at");
        if (tmp == null) {
            throw new FieldNotPresentException("created_at field is not present in GPG key json");
        }
        developerGPGPublicKey.setCreatedAt(tmp.isJsonNull() ? null : tmp.getAsString());

        tmp = gpgKeyJsonObject.get("expires_at");
        if (tmp == null) {
            throw new FieldNotPresentException("expires_at field is not present in GPG key json");
        }
        developerGPGPublicKey.setExpiresAt(tmp.isJsonNull() ? null : tmp.getAsString());

        JsonArray emailsJsonArray = gpgKeyJsonObject.get("emails").getAsJsonArray();

        for (JsonElement emailJsonElement : emailsJsonArray) {
            if (!emailJsonElement.isJsonNull()) {
                JsonObject emailJsonObj = emailJsonElement.getAsJsonObject();
                developerGPGPublicKey.addEmailId(
                        emailJsonObj.get("email").getAsString(),
                        emailJsonObj.get("verified").getAsBoolean());
            }
        }

        //Added for handing subkeys.
        if (gpgKeyJsonObject.get("subkeys") == null) {
            throw new FieldNotPresentException("subkeys field is not present in GPG key json");
        } else {
            if (!gpgKeyJsonObject.get("subkeys").isJsonNull()) {
                JsonArray subkeysJsonArray = gpgKeyJsonObject.get("subkeys").getAsJsonArray();
                for (JsonElement subkeyJsonElement : subkeysJsonArray) {
                    if (!subkeyJsonElement.isJsonNull()) {
                        JsonObject subkeyJsonObj = subkeyJsonElement.getAsJsonObject();
                        DeveloperGPGPublicKey dgpgpk = createDeveloperGPGPublicKeyBean(subkeyJsonObj);
                        //System.out.println("subkeyJsonObj : " + dgpgpk);
                        developerGPGPublicKey.addSubkey(dgpgpk);
                    }
                }
            }
        }
        return developerGPGPublicKey;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getRawKey() {
        return rawKey;
    }

    public void setRawKey(String rawKey) {
        this.rawKey = rawKey;
    }

    public boolean isEmailIdExists(String emailId) {
        return emails.containsKey(emailId);
    }

    public boolean isEmailIdVerified(String emailId) {
        return emails.get(emailId);
    }

    public void addEmailId(String emailId, boolean verified) {
        emails.put(emailId, verified);
    }

    public Set<String> getAllEmailIds() {
        return emails.keySet();
    }

    public void addSubkey(DeveloperGPGPublicKey subkey) {
        subkeys.add(subkey);
    }

    /**
     * Returns all subkeys as ArrayList.
     *
     * @return
     */
    public ArrayList<DeveloperGPGPublicKey> getAllSubkeys() {
        ArrayList<DeveloperGPGPublicKey> localsubkeys = new ArrayList<>();
        for (DeveloperGPGPublicKey subkey : this.subkeys) {
            localsubkeys.add(subkey);
        }
        return localsubkeys;
    }

    /**
     * Returns subkey that can be used for encryption.
     *
     * @return
     */
    public DeveloperGPGPublicKey getSubkeyforEncryption() {
        for (DeveloperGPGPublicKey subkey : this.subkeys) {
            if (subkey.canKeyEncryptComms()
                    && subkey.canKeyEncryptStorage()) {
                return subkey;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public boolean canKeySign() {
        return canSign;
    }

    public void setCanKeySign(boolean canSign) {
        this.canSign = canSign;
    }

    public boolean isExpired() {
        try {
            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            Date date = localFormat.parse(expiresAt);
            LocalDateTime expiryLocalDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return expiryLocalDateTime.isBefore(LocalDateTime.now());
        } catch (ParseException ex) {
            logger.error("isExpired:fail:ParseException", ex);
            return true;
        }
    }

//    public static void main(String[] args) {
//        DeveloperGPGPublicKey developerGPGPublicKey = new DeveloperGPGPublicKey();
//        developerGPGPublicKey.setExpiresAt("2018-11-06T17:17:04.000Z");
//        System.out.println(developerGPGPublicKey.isExpired());
//    }
    public boolean canKeyEncryptComms() {
        return canEncryptComms;
    }

    public void setCanKeyEncryptComms(boolean canEncryptComms) {
        this.canEncryptComms = canEncryptComms;
    }

    public boolean canKeyEncryptStorage() {
        return canEncryptStorage;
    }

    public void setCanKeyEncryptStorage(boolean canEncryptStorage) {
        this.canEncryptStorage = canEncryptStorage;
    }

    public boolean canKeyCertify() {
        return canCertify;
    }

    public void setCanKeyCertify(boolean canCertify) {
        this.canCertify = canCertify;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
