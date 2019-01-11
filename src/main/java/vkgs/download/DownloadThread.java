package vkgs.download;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URL;


public final class DownloadThread extends Thread {
    private final static Logger logger = Logger.getLogger("downloader");
    private final DownloadQueueEntry entry;
    private FinishListener finishListener;

    public DownloadThread(DownloadQueueEntry entry) {
        this.entry = entry;
    }

    void setFinishListener(FinishListener listener) {
        this.finishListener = listener;
    }

    @Override
    public void run() {
        final boolean result = downloadFile();
        if (finishListener != null) {
            finishListener.onFinish(result, entry.getSource(), entry.getDestination());
        }
    }


    private boolean downloadFile() {
        InputStream is = null;
        OutputStream os = null;

        try {
            URL url = new URI(entry.getSource()).toURL();
            File dstFile = new File(entry.getDestination());
            if (dstFile.exists()) {
                logger.warn("SKIPPED. " + entry.getDestination() + " is already exists. ");
                return true;
            }

            is = url.openStream();
            os = new FileOutputStream(dstFile);
            byte[] b = new byte[512];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            logger.error(e + "Source: " + entry.getSource());
            return false;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    interface FinishListener {
        void onFinish(Boolean success, String source, String dest);
    }

}
