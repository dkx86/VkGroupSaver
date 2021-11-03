package vkgs;

import com.google.gson.Gson;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.apache.log4j.Logger;
import vkgs.data.DataProcessor;
import vkgs.sns.ExtendedInfo;
import vkgs.sns.MediaAlbum;
import vkgs.sns.Vk;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private final static Logger logger = Logger.getLogger("group_saver");

    public static void main(String[] args) throws ClientException, ApiException {

        logger.info("--------- Saver ONLINE --------- ");
        if (!checkOutputDirs())
            return;
        logger.info("Getting wall posts...");
        final Vk api = new Vk(logger);
        final GetByIdObjectLegacyResponse groupInfo = api.getGroupInfo(Settings.it().getVkGroupId());
        final ExtendedInfo postsInfo = api.getPostsExt();
        logger.info("Saving raw posts into json...");
        final Gson gson = new Gson();
        for (WallpostFull wallPostFull : postsInfo.getPostFullList()) {
            try (Writer writer = new FileWriter(Settings.it().getPostRawJsonDir() + "post_" + wallPostFull.getId() + ".json")) {
                gson.toJson(wallPostFull, writer);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info("Found " + postsInfo.getPostFullList().size() + " posts.");

        logger.info("Getting group albums...");
        final List<MediaAlbum<Photo>> photoAlbumList = api.getAlbums();
        logger.info("Found " + photoAlbumList.size() + " albums.");

        logger.info("Getting group videos...");
        final List<VideoFull> videoAddedList = api.getAddedVideos();
        final List<MediaAlbum<VideoFull>> videoAlbums = api.getVideoAlbums();
        logger.info("Found " + videoAddedList.size() + " added videos.");
        logger.info("Found " + videoAlbums.size() + " video albums.");

        logger.info("Saving raw profiles into json...");
        for (UserFull user : postsInfo.getProfiles()) {
            try (Writer writer = new FileWriter(Settings.it().getUserRawJsonDir() + "user_" + user.getId() + ".json")) {
                gson.toJson(user, writer);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info(String.format("Found %s profiles.", postsInfo.getProfiles().size()));

        logger.info("Start processing information");
        new DataProcessor(postsInfo, groupInfo, photoAlbumList, videoAddedList, videoAlbums, logger).start();

        logger.info("Completed");
    }

    private static boolean checkOutputDirs() {
        try {
            logger.info("Checking if 'POSTS' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getPostRawJsonDir()));
            logger.info(Settings.it().getPostRawJsonDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getUserRawJsonDir()));
            logger.info(Settings.it().getUserRawJsonDir() + " -> OK.");

            Files.createDirectories(Paths.get(Settings.it().getPostImageDir()));
            logger.info(Settings.it().getPostImageDir() + " -> OK.");

            Files.createDirectories(Paths.get(Settings.it().getPostDocDir()));
            logger.info(Settings.it().getPostDocDir() + " -> OK.");

            logger.info("Checking if 'PHOTOS' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getPhotosDir()));
            logger.info(Settings.it().getPhotosDir() + " -> OK.");

            logger.info("Checking if 'VIDEO' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getVideosDir()));
            logger.info(Settings.it().getVideosDir() + " -> OK.");

        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        return true;
    }


}

