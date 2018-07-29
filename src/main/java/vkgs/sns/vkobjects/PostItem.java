package vkgs.sns.vkobjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


public class PostItem implements Serializable {
    @SerializedName("id")
    private Integer id;
    @SerializedName("from_id")
    private Integer fromId;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("date")
    private Long date;
    @SerializedName("post_type")
    private String postType;
    @SerializedName("text")
    private String text;
    @SerializedName("attachments")
    private List<Attachment> attachments;
    

    public Integer getId() {
        return id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public Long getDate() {
        return date;
    }

    public String getPostType() {
        return postType;
    }

    public String getText() {
        return text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }
}
