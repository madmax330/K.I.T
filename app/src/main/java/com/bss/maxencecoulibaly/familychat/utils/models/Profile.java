package com.bss.maxencecoulibaly.familychat.utils.models;

/**
 * Created by maxencecoulibaly on 2/25/18.
 */

public class Profile {

    private String id;
    private String name;
    private String occupation;
    private String phone;
    private String email;
    private String city;
    private String country;
    private String mother;
    private String father;
    private String photoUrl;
    private String cover_photoUrl;
    private Long dateOfBirth;
    private String spouse;
    private String userId;

    public Profile() {
    }

    public Profile(String name, String occupation, String phone, String email, String city,
                   String country, String mother, String father, String photoUrl,
                   String cover_photoUrl, Long dateOfBirth, String spouse, String userId) {
        this.name = name;
        this.occupation = occupation;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.country = country;
        this.mother = mother;
        this.father = father;
        this.photoUrl = photoUrl;
        this.cover_photoUrl = cover_photoUrl;
        this.dateOfBirth = dateOfBirth;
        this.spouse = spouse;
        this.userId = userId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMother() {
        return this.mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    public String getFather() {
        return this.father;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCover_photoUrl() {
        return this.cover_photoUrl;
    }

    public void setCover_photoUrl(String cover_photoUrl) {
        this.cover_photoUrl = cover_photoUrl;
    }

    public Long getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(Long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSpouse() {
        return this.spouse;
    }

    public void setSpouse(String spouse) {
        this.spouse = spouse;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
