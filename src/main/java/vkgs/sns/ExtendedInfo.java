package vkgs.sns;

import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.WallpostFull;

import java.util.ArrayList;
import java.util.List;

public class ExtendedInfo {

    private final List<WallpostFull> postFullList;
    private final List<UserFull> profiles;

    public ExtendedInfo() {
        this.postFullList = new ArrayList<>();
        this.profiles = new ArrayList<>();
    }

    public List<WallpostFull> getPostFullList() {
        return postFullList;
    }

    public List<UserFull> getProfiles() {
        return profiles;
    }

    public void addPosts(List<WallpostFull> items){
        postFullList.addAll(items);
    }

    public void addProfiles(List<UserFull> items){
        profiles.addAll(items);
    }

    public Integer size() {
        return postFullList.size();
    }
}
