package vkgs.sns;

import com.vk.api.sdk.objects.photos.Photo;

import java.util.List;

public final class PhotoAlbum {
    private final String title;
    private final String description;
    private final List<Photo> photos;

    PhotoAlbum(String title, String description, List<Photo> photos) {
        this.title = title;
        this.description = description;
        this.photos = photos;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}
