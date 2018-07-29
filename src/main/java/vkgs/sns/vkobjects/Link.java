package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Link implements Serializable {

    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;
    @SerializedName("caption")
    private String caption;
    @SerializedName("description")
    private String description;

}
