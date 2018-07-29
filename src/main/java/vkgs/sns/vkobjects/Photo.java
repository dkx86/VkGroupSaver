package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public final class Photo implements Serializable {

    @SerializedName("id")
    private Integer id;
    @SerializedName("album_id")
    private Integer albumId;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("photo_75")
    private String photo75;
    @SerializedName("photo_130")
    private String photo130;
    @SerializedName("photo_604")
    private String photo604;
    @SerializedName("photo_807")
    private String photo807;
    @SerializedName("photo_1280")
    private String photo1280;
    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;
    @SerializedName("text")
    private String text;
    @SerializedName("date")
    private Long date;
    @SerializedName("post_id")
    private Integer postId;
    @SerializedName("access_key")
    private String accessKey;


    public Integer getId() {
        return id;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getPhoto75() {
        return photo75;
    }

    public String getPhoto130() {
        return photo130;
    }

    public String getPhoto604() {
        return photo604;
    }

    public String getPhoto807() {
        return photo807;
    }

    public String getPhoto1280() {
        return photo1280;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }

    public Long getDate() {
        return date;
    }

    public Integer getPostId() {
        return postId;
    }

    public String getAccessKey() {
        return accessKey;
    }
}
