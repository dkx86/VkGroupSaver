package vkgs.data;

import com.vk.api.sdk.objects.wall.WallpostAttachmentType;

import java.util.ArrayList;
import java.util.List;

final class AttachContainer {
    private WallpostAttachmentType type;
    private List<String> entries;

    AttachContainer(WallpostAttachmentType type) {
        this.type = type;
        this.entries = new ArrayList<>();
    }

    WallpostAttachmentType getType() {
        return type;
    }

    List<String> getEntries() {
        return entries;
    }

    void addEntry(String entry) {
        entries.add(entry);
    }
}

