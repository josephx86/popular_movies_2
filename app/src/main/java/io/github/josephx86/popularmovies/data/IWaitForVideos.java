package io.github.josephx86.popularmovies.data;

import java.util.List;
import io.github.josephx86.popularmovies.data.videos.Video;

public interface IWaitForVideos {
    void processReceivedVideos(List<Video> videos);
}
