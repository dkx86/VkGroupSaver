package vkgs;

import com.google.gson.Gson;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.WallPostFull;
import org.apache.log4j.Logger;
import vkgs.data.DataProcessor;
import vkgs.sns.ExtendedInfo;
import vkgs.sns.PhotoAlbum;
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
        String fromDate;
        if (args.length > 0)
            fromDate = args[0];
        boolean isDiff;
        if (args.length == 2)
            isDiff = Boolean.getBoolean(args[1]);

        logger.info("--------- Saver ONLINE --------- ");
        if (!checkOutputDirs())
            return;
        logger.info("Getting wall posts...");
        final Vk api = new Vk(logger);
        final GroupFull groupInfo = api.getGroupInfo(Settings.it().getVkGroupId());
        final ExtendedInfo postsInfo = api.getPostsExt();
        logger.info("Saving raw posts into json...");
        final Gson gson = new Gson();
        for (WallPostFull wallPostFull : postsInfo.getPostFullList()) {
            try (Writer writer = new FileWriter(Settings.it().getPostRawJsonDir() + "post_" + wallPostFull.getId() + ".json")) {
                gson.toJson(wallPostFull, writer);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info("Found " + postsInfo.getPostFullList().size() + " posts.");

        logger.info("Getting group albums...");
        final List<PhotoAlbum> photoAlbumList = api.getAlbums();
        logger.info("Found " + photoAlbumList.size() + " albums.");

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
        new DataProcessor(postsInfo, groupInfo, photoAlbumList, logger).start();

        logger.info("Completed");
    }

    private static boolean checkOutputDirs() {
        try {
            logger.info("Checking if 'POSTS' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getPostRawJsonDir()));
            logger.info(Settings.it().getPostRawJsonDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getUserRawJsonDir()));
            logger.info(Settings.it().getUserRawJsonDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getPostAudioDir()));
            logger.info(Settings.it().getPostAudioDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getPostImageDir()));
            logger.info(Settings.it().getPostImageDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getPostVideoDir()));
            logger.info(Settings.it().getPostVideoDir() + " -> OK.");
            Files.createDirectories(Paths.get(Settings.it().getPostDocDir()));
            logger.info(Settings.it().getPostDocDir() + " -> OK.");

            logger.info("Checking if 'PHOTOS' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getPhotosDir()));
            logger.info(Settings.it().getPhotosDir() + " -> OK.");

            logger.info("Checking if 'AUDIO' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getAudiosDir()));
            logger.info(Settings.it().getAudiosDir() + " -> OK.");

            logger.info("Checking if 'VIDEO' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getVideosDir()));
            logger.info(Settings.it().getVideosDir() + " -> OK.");

            logger.info("Checking if 'DISCUSSIONS' directory exists...");
            Files.createDirectories(Paths.get(Settings.it().getDiscussionsDir()));
            logger.info(Settings.it().getDiscussionsDir() + " -> OK.");

        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        return true;
    }


}

