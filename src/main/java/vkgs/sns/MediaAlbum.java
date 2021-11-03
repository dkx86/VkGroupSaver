package vkgs.sns;

import java.util.List;

public final class MediaAlbum<T> {
    private final String title;
    private final String description;
    private final List<T> items;

    MediaAlbum(String title, String description, List<T> items) {
        this.title = title;
        this.description = description;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<T> getItems() {
        return items;
    }
}
