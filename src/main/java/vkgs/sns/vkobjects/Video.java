package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Video implements Serializable {

    @SerializedName("id")
    private Long id;
    @SerializedName("owner_id")
    private Long ownerId;
    @SerializedName("title")
    private String title;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("description")
    private String description;
    @SerializedName("date")
    private Long date;
    @SerializedName("comments")
    private Integer comments;
    @SerializedName("views")
    private Integer views;
    @SerializedName("photo_130")
    private String photo130;
    @SerializedName("photo_320")
    private String photo320;
    @SerializedName("photo_640")
    private String photo640;
    @SerializedName("access_key")
    private String accessKey;
    @SerializedName("platform")
    private String platform; //YouTube
    @SerializedName("can_add")
    private Integer canAdd;


    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public Long getDate() {
        return date;
    }

    public Integer getComments() {
        return comments;
    }

    public Integer getViews() {
        return views;
    }

    public String getPhoto130() {
        return photo130;
    }

    public String getPhoto320() {
        return photo320;
    }

    public String getPhoto640() {
        return photo640;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getPlatform() {
        return platform;
    }

    public Integer getCanAdd() {
        return canAdd;
    }
}
