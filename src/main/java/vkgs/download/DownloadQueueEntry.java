package vkgs.download;


public final class DownloadQueueEntry {
    private String source;
    private String destination;

    public DownloadQueueEntry(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }
}
