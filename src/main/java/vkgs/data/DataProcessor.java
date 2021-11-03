package vkgs.data;

import com.vk.api.sdk.objects.audio.Audio;
import com.vk.api.sdk.objects.base.Link;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.*;
import org.apache.log4j.Logger;
import vkgs.Settings;
import vkgs.download.DownloadQueueEntry;
import vkgs.download.DownloadThread;
import vkgs.sns.ExtendedInfo;
import vkgs.sns.MediaAlbum;

import java.io.IOException;
import java.net.URI;
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
    private final List<WallpostFull> postFullList;
    private final Logger logger;
    private final Map<Integer, UserFull> id2userMap = new HashMap<>();
    private final GetByIdObjectLegacyResponse groupInfo;
    private final List<MediaAlbum<Photo>> photoAlbumList;
    private final List<VideoFull> videoAddedList;
    private final List<MediaAlbum<VideoFull>> videoAlbums;

    public DataProcessor(ExtendedInfo extendedInfo,
                         GetByIdObjectLegacyResponse groupInfo,
                         List<MediaAlbum<Photo>> photoAlbumList,
                         List<VideoFull> videoAddedList,
                         List<MediaAlbum<VideoFull>> videoAlbums,
                         Logger logger) {
        this.postFullList = extendedInfo.getPostFullList();
        this.logger = logger;
        extendedInfo.getProfiles().forEach(u -> id2userMap.put(u.getId(), u));
        this.groupInfo = groupInfo;
        this.photoAlbumList = photoAlbumList;
        this.videoAddedList = videoAddedList;
        this.videoAlbums = videoAlbums;
    }

    private static URI getPhotoSource(Photo photo) {
        if(photo.getSizes() == null)
            return null;
        PhotoSizes sizeHQ = photo.getSizes().get(0);
        for (PhotoSizes size : photo.getSizes()) {
            if(size.getWidth() > sizeHQ.getWidth())
                sizeHQ = size;
        }

        return sizeHQ.getUrl();
    }

    private static URI getGraffitiSource(Graffiti graffiti) {
        if (graffiti.getPhoto586() != null)
            return graffiti.getPhoto586();

        return graffiti.getPhoto200();
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
        for (MediaAlbum<Photo> photoAlbum : photoAlbumList) {
            logger.info("Star downloading album: " + photoAlbum.getTitle() + " Photos count: " + photoAlbum.getItems().size());
            final List<DownloadQueueEntry> downloadQueueEntryList = getPhotoAlbumsDownloadQueue(photoAlbum.getTitle(), photoAlbum.getItems());
            startHeavyDownloads(downloadQueueEntryList);
            doWait();
        }

        logger.info("Star saving video links. List size: " + videoAddedList.size());
        saveVideoLinks(videoAddedList, "added");
        videoAlbums.forEach(a->saveVideoLinks(a.getItems(), a.getTitle().replace(" ", "_")));
        doWait();

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
        for (WallpostFull post : postFullList) {
            Map<WallpostAttachmentType, AttachContainer> attachContainerMap = collectAttachments(post, downloadQueueEntryList);
            saveTextPost(post, id2userMap.get(post.getFromId()), attachContainerMap);
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

    private Map<WallpostAttachmentType, AttachContainer> collectAttachments(WallpostFull post, List<DownloadQueueEntry> downloadQueueEntryList) {
        final Map<WallpostAttachmentType, AttachContainer> result = new HashMap<>();
        if (post.getAttachments() == null)
            return result;
        for (WallpostAttachment item : post.getAttachments()) {
            String filename = "";
            String filepath = "";
            URI source = null;
            final WallpostAttachmentType itemType = item.getType();
            switch (itemType) {
                case AUDIO:
                    Audio audio = item.getAudio();
                    source = audio.getUrl();
                    filename = audio.getArtist() + " - " + audio.getTitle();
                    logger.debug("Audio from post #" + post.getId());
                    break;
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
                    filename = video.getTitle() + " URL: " + video.getPlayer();
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

            if (itemType != WallpostAttachmentType.LINK && itemType != WallpostAttachmentType.VIDEO && itemType != WallpostAttachmentType.AUDIO && source != null)
                downloadQueueEntryList.add(new DownloadQueueEntry(source, filepath));
        }

        return result;
    }

    private void saveTextPost(WallpostFull post, UserFull author, Map<WallpostAttachmentType, AttachContainer> attachContainerMap) {
        final Path fileName = Paths.get(Settings.it().getPostsDir() + "post_" + post.getId() + ".txt");
        StringBuilder data = new StringBuilder();
        data.append("ID: ").append(post.getId()).append("\n\n");
        if (author != null)
            data.append(author.getFirstName()).append(' ').append(author.getLastName()).append(" id:[").append(author.getId()).append("]");
        else
            data.append("Group: ").append(groupInfo.getName()).append(' ').append(" id:[").append(groupInfo.getId()).append("] ( https://vk.com/").append(groupInfo.getScreenName()).append(')');
        data.append(" @ ").append(getDateString(post.getDate())).append("\n\n");
        data.append("----------------------------------------------------------------\n\n");
        data.append(post.getText()).append("\n\n");
        final PostCopyright postCopyright = post.getCopyright();
        if(postCopyright != null) {
            data.append("Copyright: ").append(postCopyright.getLink()).append("\n\n");
        }

        attachContainerMap.forEach((type, container) -> {
            data.append(container.getType().toString().toUpperCase()).append(":\n");
            container.getEntries().forEach(e -> data.append(e).append('\n'));
        });

        try {
            Files.write(fileName, data.toString().getBytes());
        } catch (IOException e) {
            logger.error("Cannot save file: " + fileName);
            logger.error(e);
        }
        logger.info("Post #" + post.getId() + " was saved to the file " + fileName.toAbsolutePath());
    }

    private void saveVideoLinks(List<VideoFull> videoList, String prefix) {
        final Path fileName = Paths.get(Settings.it().getVideosDir() + prefix + "_video_links.txt");
        final Path fileNameRaw = Paths.get(Settings.it().getVideosDir() + prefix+ "_video_links_raw.txt");

        StringBuilder data = new StringBuilder();
        StringBuilder dataRaw = new StringBuilder();

        // сохраняем добавленные видео
        for (VideoFull video : videoList) {
            data.append("ID: ").append(video.getId());
            data.append(" TITLE: ").append(video.getTitle());
            data.append(" DESC: ").append(video.getDescription());
            data.append(" URL: ").append(video.getPlayer());
            data.append("\n");

            dataRaw.append(video.getPlayer()).append("\n");
        }

        try {
            Files.write(fileName, data.toString().getBytes());
            Files.write(fileNameRaw, dataRaw.toString().getBytes());
        } catch (IOException e) {
            logger.error(e);
        }
    }



    private void doWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

}
