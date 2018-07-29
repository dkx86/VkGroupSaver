package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public final class Audio implements Serializable {

    @SerializedName("id")
    private Integer id;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("artist")
    private String artist;
    @SerializedName("title")
    private String title;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("date")
    private Long date;
    @SerializedName("url")
    private String url;
    @SerializedName("albumId")
    private Integer album_id;
    @SerializedName("genre_id")
    private Integer genreId;
    @SerializedName("is_hq")
    private Boolean isHQ;


    public Integer getId() {
        return id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public Integer getDuration() {
        return duration;
    }

    public Long getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public Integer getAlbum_id() {
        return album_id;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public Boolean getHQ() {
        return isHQ;
    }
}
