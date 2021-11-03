package vkgs;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Settings {

    private static final String FILE_NAME = "config.dat";
    private final static Logger logger = Logger.getLogger("group_saver");
    private static Settings instance;

    private Integer vk_api_id;
    private String vk_client_secret;
    private String vk_redirect_uri;
    private String vk_token;
    private Integer vk_group_id;
    private Integer vk_user_id;

    private String data_dir;
    private String posts_dir;
    private String post_image_dir;
    private String post_docs_dir;
    private String post_raw_json_dir;
    private String user_raw_json_dir;
    private String photos_dir;
    private String videos_dir;
    private String discussions_dir;
    private String links_file;
    private String readme_file;

    private Settings() {
        Path path = Paths.get(FILE_NAME);
        final List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e);
            return;
        }
        parse(lines);
    }

    public static Settings it() {
        if (instance == null)
            instance = new Settings();
        return instance;
    }

    private void parse(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("#") || line.length() == 0)
                continue;
            initField(getName(line), getValue(line));
        }
    }

    private void initField(String name, String val) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.getName().equals(name))
                continue;
            try {
                field.setAccessible(true);
                if(field.getType().equals(Integer.class))
                    field.set(this, Integer.valueOf(val));
                else
                    field.set(this, val);
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                logger.error(e);
            }
        }
    }

    private String getValue(String line) {
        int start = line.indexOf("=");
        return line.substring(start + 1).trim();
    }

    private String getName(String line) {
        int end = line.indexOf("=");
        return line.substring(0, end).trim();
    }

    public String getDataDir() {
        return data_dir;
    }

    public String getPostsDir() {
        return getDataDir() + posts_dir;
    }

    public String getPostImageDir() {
        return getPostsDir() + post_image_dir;
    }

    public String getPostDocDir(){
        return getPostsDir() +  post_docs_dir;
    }

    public String getPostRawJsonDir() {
        return getPostsDir() + post_raw_json_dir;
    }

    public String getUserRawJsonDir() {
        return getPostsDir() + user_raw_json_dir;
    }

    public String getPhotosDir() {
        return getDataDir() + photos_dir;
    }

    public String getVideosDir() {
        return getDataDir() + videos_dir;
    }

    public String getLinksFile() {
        return getDataDir() + links_file;
    }

    public String getReadmeFile() {
        return getDataDir() + readme_file;
    }


    public Integer getVkApiId() {
        return vk_api_id;
    }

    public String getVkClientSecret() {
        return vk_client_secret;
    }

    public String getVkRedirectUri() {
        return vk_redirect_uri;
    }

    public String getVkToken() {
        return vk_token;
    }

    public Integer getVkGroupId() {
        return vk_group_id;
    }

    public Integer getVkUserId() {
        return vk_user_id;
    }
}
