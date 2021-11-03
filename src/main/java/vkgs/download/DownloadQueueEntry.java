package vkgs.download;


import java.net.URI;

public final class DownloadQueueEntry {
    private URI source;
    private String destination;

    public DownloadQueueEntry(URI source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public URI getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "DownloadQueueEntry{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
