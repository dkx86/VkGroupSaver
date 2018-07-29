package vkgs;

import org.junit.Assert;
import org.junit.Test;

public final class ConfigTests {

    @Test
    public void parse() {
        Settings settings = Settings.it();
        Assert.assertNotNull(settings);
        Assert.assertEquals("D:/group_archive/", settings.getDataDir());
        Assert.assertEquals("D:/group_archive/posts/video/", settings.getPostVideoDir());
        Assert.assertEquals("D:/group_archive/README", settings.getReadmeFile());
    }
}
