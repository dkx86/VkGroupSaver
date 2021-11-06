package vkgs.sns;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.responses.GetAlbumsResponse;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.video.VideoAlbumFull;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.responses.GetExtendedResponse;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
import com.vk.api.sdk.queries.photos.PhotosGetAlbumsQuery;
import com.vk.api.sdk.queries.photos.PhotosGetQuery;
import com.vk.api.sdk.queries.video.VideoGetAlbumsQuery;
import com.vk.api.sdk.queries.video.VideoGetQuery;
import com.vk.api.sdk.queries.wall.WallGetQueryWithExtended;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import vkgs.Settings;

import java.util.ArrayList;
import java.util.List;


public class Vk {
    private static final int LIMIT = 100;
    private final VkApiClient vk;
    private final UserActor userActor;
    private final Logger logger;

    public Vk(Logger logger) {
        this.logger = logger;
        vk = new VkApiClient(HttpTransportClient.getInstance());
        userActor = new UserActor(Settings.it().getVkUserId(), Settings.it().getVkToken());
    }

    public GetByIdObjectLegacyResponse getGroupInfo(int id) throws ClientException, ApiException {
        GroupsGetByIdQueryWithObjectLegacy query = vk.groups().getByIdObjectLegacy(userActor);
        query.groupId(String.valueOf(id));
        final List<GetByIdObjectLegacyResponse> fullList = query.execute();
        return fullList.get(0);
    }

    public ExtendedInfo getPostsExt() throws ClientException, ApiException {
        final ExtendedInfo result = new ExtendedInfo();
        int offset = 0;
        while (true) {
            final WallGetQueryWithExtended wallGetQuery = vk.wall().getExtended(userActor);
            wallGetQuery.ownerId(-Settings.it().getVkGroupId());
            wallGetQuery.count(LIMIT);
            wallGetQuery.offset(offset);
            wallGetQuery.unsafeParam("fields", "first_name, last_name");

            final GetExtendedResponse response = wallGetQuery.execute();
            if (response.getItems().isEmpty())
                break;
            result.addPosts(response.getItems());
            result.addProfiles(response.getProfiles());
            offset += LIMIT;
            doWait();
        }

        logger.info("Collected " + result.size() + " posts.");
        return result;
    }

    public List<MediaAlbum<Photo>> getAlbums() throws ClientException, ApiException {
        List<MediaAlbum<Photo>> photoAlbumList = new ArrayList<>();
        final PhotosGetAlbumsQuery albums = vk.photos().getAlbums(userActor);
        albums.ownerId(-Settings.it().getVkGroupId());
        GetAlbumsResponse albumsResponse = albums.execute();

        for (PhotoAlbumFull albumFull : albumsResponse.getItems()) {
            final String title = albumFull.getTitle();
            String description = albumFull.getDescription();

            List<Photo> photos = getPhotosForAlbum(albumFull);
            photoAlbumList.add(new MediaAlbum<>(title, description, photos));
            doWait();
        }

        return photoAlbumList;
    }

    public List<VideoFull> getAddedVideos() throws ClientException, ApiException {
        final VideoGetQuery videos = vk.videos().get(userActor);
        videos.ownerId(-Settings.it().getVkGroupId());
        com.vk.api.sdk.objects.video.responses.GetResponse videosResponse = videos.execute();
        return videosResponse.getItems();
    }

    public List<MediaAlbum<VideoFull>> getVideoAlbums() throws ClientException, ApiException {
        final List<MediaAlbum<VideoFull>> albums = new ArrayList<>();
        final VideoGetAlbumsQuery videos = vk.videos().getAlbums(userActor);
        videos.ownerId(-Settings.it().getVkGroupId());
        com.vk.api.sdk.objects.video.responses.GetAlbumsResponse videosResponse = videos.execute();
        for (VideoAlbumFull albumFull : videosResponse.getItems()) {
            List<VideoFull> items = getVideosForAlbum(albumFull);
            albums.add(new MediaAlbum<>(albumFull.getTitle(), StringUtils.EMPTY, items));
        }

        return albums;
    }

    private List<Photo> getPhotosForAlbum(PhotoAlbumFull albumFull) throws ApiException, ClientException {
        final List<Photo> result = new ArrayList<>();
        int offset = 0;
        while (true) {
            PhotosGetQuery photosGetQuery = vk.photos().get(userActor);
            photosGetQuery.albumId(albumFull.getId().toString());
            photosGetQuery.ownerId(-Settings.it().getVkGroupId());
            photosGetQuery.count(LIMIT);
            photosGetQuery.offset(offset);
            GetResponse photoResponse = photosGetQuery.execute();
            final List<Photo> items = photoResponse.getItems();
            if (items == null || items.isEmpty()) break;
            result.addAll(items);
            offset += LIMIT;
            doWait();
        }
        return result;
    }


    private List<VideoFull> getVideosForAlbum(VideoAlbumFull albumFull) throws ApiException, ClientException {
        final List<VideoFull> result = new ArrayList<>();
        int offset = 0;
        while (true) {
            VideoGetQuery videosGetQuery = vk.videos().get(userActor);
            videosGetQuery.albumId(albumFull.getId());
            videosGetQuery.ownerId(-Settings.it().getVkGroupId());
            videosGetQuery.count(LIMIT);
            videosGetQuery.offset(offset);
            com.vk.api.sdk.objects.video.responses.GetResponse videoResponse = videosGetQuery.execute();
            final List<VideoFull> items = videoResponse.getItems();
            if (items == null || items.isEmpty()) break;
            result.addAll(items);
            offset += LIMIT;
            doWait();
        }
        return result;
    }

    private void doWait() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            logger.error("doWait failed", e);
        }
    }


}
