package vkgs.download;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URL;


public final class DownloadThread extends Thread {
    private final static Logger logger = Logger.getLogger("downloader");
    private  final DownloadQueueEntry entry;
    private int repeatCounter;
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

        while (true) {
            try {
                URL url = new URI(entry.getSource()).toURL();
                File dstFile = new File(entry.getDestination());
                if(dstFile.exists()){
                    logger.info("SKIPPED. " + entry.getDestination() + " is already exists. ");
                    return true;
                }

                is = url.openStream();
                os = new FileOutputStream(dstFile);
                byte[] b = new byte[2048];
                int length;

                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                return true;
            } catch (Exception e) {
                logger.error(e);
                return false;
            } finally {
                try {
                    if (is != null) is.close();
                    if (os != null) os.close();
                } catch (IOException e) {
                    logger.error(e);
                    return false;
                }
            }
        }
    }

    interface FinishListener {
        void onFinish(Boolean success, String source, String dest);
    }

}
