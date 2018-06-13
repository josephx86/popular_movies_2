package io.github.josephx86.popularmovies.data.videos;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.josephx86.popularmovies.R;

public class VideosAdapter extends RecyclerView.Adapter<VideoViewHolder> {
    private List<Video> videos = new ArrayList<>();
    private View.OnClickListener clickListener;

    public VideosAdapter(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_video_item, parent, false);
        if (clickListener != null) {
            view.setOnClickListener(clickListener);
        }
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        if (position < videos.size()) {
            Video video = videos.get(position);
            if (video != null) {
                holder.setVideo(video);
            }
        }
    }

    public void addVideos(List<Video> newVideos) {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        videos.addAll(newVideos);
        notifyDataSetChanged();
    }

    public Video getFirstVideo() {
        Video firstVideo = null;
        if (videos != null) {
            if (videos.size() >= 1) {
                firstVideo = videos.get(0);
            }
        }
        return firstVideo;
    }

    public ArrayList<Video> getVideos() {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        return (ArrayList<Video>) videos;
    }

    @Override
    public int getItemCount() {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        return videos.size();
    }
}