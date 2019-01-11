package vkgs.data;

import com.vk.api.sdk.objects.base.Link;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.video.VideoFiles;
import com.vk.api.sdk.objects.wall.Graffiti;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import org.apache.log4j.Logger;
import vkgs.Settings;
import vkgs.download.DownloadQueueEntry;
import vkgs.download.DownloadThread;
import vkgs.sns.ExtendedInfo;
import vkgs.sns.PhotoAlbum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public final class DataProcessor {
    private final List<WallPostFull> postFullList;
    private final Logger logger;
    private final Map<Integer, UserFull> id2userMap = new HashMap<>();
    private final GroupFull groupInfo;
    private final List<PhotoAlbum> photoAlbumList;

    public DataProcessor(ExtendedInfo extendedInfo, GroupFull groupInfo, List<PhotoAlbum> photoAlbumList, Logger logger) {
        this.postFullList = extendedInfo.getPostFullList();
        this.logger = logger;
        extendedInfo.getProfiles().forEach(u -> id2userMap.put(u.getId(), u));
        this.groupInfo = groupInfo;
        this.photoAlbumList = photoAlbumList;
    }

    private static String getPhotoSource(Photo photo) {
        if (photo.getPhoto2560() != null)
            return photo.getPhoto2560();

        if (photo.getPhoto1280() != null)
            return photo.getPhoto1280();

        if (photo.getPhoto807() != null)
            return photo.getPhoto807();

        if (photo.getPhoto604() != null)
            return photo.getPhoto604();

        if (photo.getPhoto130() != null)
            return photo.getPhoto130();

        return photo.getPhoto75();
    }

    private static String getGraffitiSource(Graffiti graffiti) {
        if (graffiti.getPhoto586() != null)
            return graffiti.getPhoto586();

        return graffiti.getPhoto200();
    }

    private static String getVideoSource(Video video) {
        final VideoFiles files = video.getFiles();
        if (files == null)
            return null;
        if (files.getMp1080() != null)
            return files.getMp1080();

        if (files.getMp720() != null)
            return files.getMp720();

        if (files.getMp480() != null)
            return files.getMp480();

        if (files.getMp360() != null)
            return files.getMp360();

        return files.getMp240();
    }

    public static String getDateString(Integer time) {
        final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.ofHours(3));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy", Locale.ENGLISH);
        return dateTime.format(formatter);
    }

    public void start() {
        final List<DownloadQueueEntry> downloadPostsQueueEntryList = getPostsDownloadQueue();
        logger.info("Star downloading attachments. Queue size is " + downloadPostsQueueEntryList.size());
        startHeavyDownloads(downloadPostsQueueEntryList);


        logger.info("Star downloading photo albums. List size: " + photoAlbumList.size());
        for (PhotoAlbum photoAlbum : photoAlbumList) {
            logger.info("Star downloading album: " + photoAlbum.getTitle() + " Photos count: " + photoAlbum.getPhotos().size());
            final List<DownloadQueueEntry> downloadQueueEntryList = getPhotoAlbumsDownloadQueue(photoAlbum.getTitle(), photoAlbum.getPhotos());
            startHeavyDownloads(downloadQueueEntryList);
            doWait();
        }
        logger.info("All downloads completed");
    }

    private List<DownloadQueueEntry> getPhotoAlbumsDownloadQueue(String albumTitle, List<Photo> photos) {
        final List<DownloadQueueEntry> downloadQueueEntryList = new ArrayList<>();

        for (Photo photo : photos) {
            final String filename = "img_" + photo.getId() + ".jpg";
            final String dirPath = Settings.it().getPhotosDir() + albumTitle + "/";
            try {
                Files.createDirectories(Paths.get(dirPath));
            } catch (IOException e) {
                logger.error("Cannot create dir '" + dirPath + "' for album '" + albumTitle + "'.", e);
                return Collections.emptyList();
            }

            downloadQueueEntryList.add(new DownloadQueueEntry(getPhotoSource(photo), dirPath + filename));
        }

        return downloadQueueEntryList;
    }


    private List<DownloadQueueEntry> getPostsDownloadQueue() {
        final List<DownloadQueueEntry> downloadQueueEntryList = new ArrayList<>();
        for (WallPostFull post : postFullList) {
            Map<WallpostAttachmentType, AttachContainer> attachContainerMap = collectAttachments(post, downloadQueueEntryList);
            saveTextPost(post.getId(), id2userMap.get(post.getFromId()), post.getDate(), post.getText(), attachContainerMap);
        }
        return downloadQueueEntryList;
    }

    private void startHeavyDownloads(List<DownloadQueueEntry> downloadQueueEntryList) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        downloadQueueEntryList.forEach(e -> {
            //logger.info("Thread for " + e.toString());
            executorService.execute(new DownloadThread(e));
        });

        executorService.shutdown();

        try {
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    private Map<WallpostAttachmentType, AttachContainer> collectAttachments(WallPostFull post, List<DownloadQueueEntry> downloadQueueEntryList) {
        final Map<WallpostAttachmentType, AttachContainer> result = new HashMap<>();
        if (post.getAttachments() == null)
            return result;
        for (WallpostAttachment item : post.getAttachments()) {
            String filename = "";
            String filepath = "";
            String source = "";
            final WallpostAttachmentType itemType = item.getType();
            switch (itemType) {
                case AUDIO:
                    /*
                    AudioFull audio = item.getAudio();
                    source = audio.getUrl();
                    filename = audio.getArtist() + " - " + audio.getTitle();
                    filepath = Settings.it().getPostAudioDir() + post.getId() + "_" + filename;
                    */
                    logger.debug("Audio from post #" + post.getId());
                    continue; //TODO
                case PHOTO:
                    final Photo photo = item.getPhoto();
                    source = getPhotoSource(photo);
                    filename = "img_" + post.getId() + '_' + photo.getId() + ".jpg";
                    filepath = Settings.it().getPostImageDir() + filename;
                    break;
                case PHOTOS_LIST:
                    final List<String> photosList = item.getPhotosList();
                    logger.debug("Photo list from post #" + post.getId());
                    photosList.forEach(logger::debug);
                    continue;
                case ALBUM:
                    logger.debug("Photo album from post #" + post.getId());
                    continue; //TODO
                case VIDEO:
                    final Video video = item.getVideo();
                    source = getVideoSource(video);
                    filename = video.getTitle();
                    if (source == null) {
                        filename += " [NO FILE - EXTERNAL VIDEO]";
                        break;
                    }
                    filename += ".mp4";
                    filepath = Settings.it().getPostVideoDir() + filename;
                    break;
                case LINK:
                    Link link = item.getLink();
                    filename = link.getUrl() + " 「" + link.getCaption() + ": " + link.getDescription() + "」";
                    break;
                case DOC:
                    logger.debug("Doc from post #" + post.getId());
                    source = item.getDoc().getUrl();
                    filename = item.getDoc().getTitle();
                    filepath = Settings.it().getPostDocDir() + filename;
                    break;
                case GRAFFITI:
                    logger.debug("Graffiti from post #" + post.getId());
                    final Graffiti graffiti = item.getGraffiti();
                    source = getGraffitiSource(graffiti);
                    filename = "graffiti_" + post.getId() + '_' + graffiti.getId() + ".png";
                    filepath = Settings.it().getPostImageDir() + filename;
                    break;
            }
            AttachContainer container = result.get(itemType);
            if (container == null) {
                container = new AttachContainer(itemType);
                result.put(itemType, container);
            }
            container.addEntry(filename);

            if (itemType != WallpostAttachmentType.LINK && source != null)
                downloadQueueEntryList.add(new DownloadQueueEntry(source, filepath));
        }

        return result;
    }

    private void saveTextPost(Integer id, UserFull author, Integer date, String text, Map<WallpostAttachmentType, AttachContainer> attachContainerMap) {
        final Path fileName = Paths.get(Settings.it().getPostsDir() + "post_" + id + ".txt");
        StringBuilder data = new StringBuilder();
        data.append("ID: ").append(id).append('\n');
        if (author != null)
            data.append(author.getFirstName()).append(' ').append(author.getLastName()).append(" id:[").append(author.getId()).append("]");
        else
            data.append("Group: ").append(groupInfo.getName()).append(' ').append(" id:[").append(groupInfo.getId()).append("] ( https://vk.com/").append(groupInfo.getScreenName()).append(')');
        data.append(" @ ").append(getDateString(date));
        data.append("--------------------------------\n");
        data.append(text).append('\n');

        attachContainerMap.forEach((type, container) -> {
            data.append(container.getType().toString().toUpperCase()).append(" --------------------------------\n");
            container.getEntries().forEach(e -> data.append(e).append('\n'));
        });

        try {
            Files.write(fileName, data.toString().getBytes());
        } catch (IOException e) {
            logger.error("Cannot save file: " + fileName);
            logger.error(e);
        }
        logger.info("Post #" + id + " was saved to the file " + fileName.toAbsolutePath());
    }

    private void doWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

}
