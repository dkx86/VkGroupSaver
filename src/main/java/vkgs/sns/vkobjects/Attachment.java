package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.wall.Graffiti;

import java.io.Serializable;


public final class Attachment implements Serializable {
    @SerializedName("type")
    private String type;

    @SerializedName("photo")
    private Photo photo;

    @SerializedName("audio")
    private Audio audio;

    @SerializedName("video")
    private Video video;

    @SerializedName("link")
    private Link link;

    @SerializedName("graffiti")
    private Graffiti graffiti;


    public String getType() {
        return type;
    }

    public Photo getPhoto() {
        return photo;
    }

    public Audio getAudio() {
        return audio;
    }

    public Video getVideo() {
        return video;
    }

    public Link getLink() {
        return link;
    }
}
