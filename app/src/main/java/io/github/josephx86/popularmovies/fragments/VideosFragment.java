package io.github.josephx86.popularmovies.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.DetailsPagerFragment;
import io.github.josephx86.popularmovies.data.Utils;
import io.github.josephx86.popularmovies.data.videos.Video;
import io.github.josephx86.popularmovies.data.videos.VideosAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideosFragment extends DetailsPagerFragment implements View.OnClickListener {
    @BindView(R.id.videos_rv)
    RecyclerView videosRecyclerView;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    @BindView(R.id.no_videos_tv)
    TextView noVideosTextView;

    @BindView(R.id.snackbar_parent)
    CoordinatorLayout snackbarParent;

    private VideosAdapter adapter = new VideosAdapter(this);

    private int scrollPosition = 0;

    public VideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        ButterKnife.bind(this, view);
        setupRecyclerView(view.getContext());
        updateUI();
        return view;
    }

    private void updateUI() {
        // Update the UI accordingly
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if ((videosRecyclerView != null) && (noVideosTextView != null)) {
            if ((adapter != null) && (adapter.getItemCount() == 0)) {
                videosRecyclerView.setVisibility(View.GONE);
                noVideosTextView.setVisibility(View.VISIBLE);
            } else {
                videosRecyclerView.setVisibility(View.VISIBLE);
                noVideosTextView.setVisibility(View.GONE);
            }
        }
    }

    public Video getFirstVideo() {
        return adapter.getFirstVideo();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();

        // Restore scroll position
        if ((videosRecyclerView != null) && (adapter != null) && (adapter.getItemCount() > 0) && (scrollPosition > 0)) {
            videosRecyclerView.smoothScrollToPosition(scrollPosition);
        }
    }

    private void setupRecyclerView(Context context) {
        if (videosRecyclerView != null) {
            int columnCount = Utils.calculateVideoGridLayoutColumns(context);
            GridLayoutManager layoutManager = new GridLayoutManager(context, columnCount);
            videosRecyclerView.setLayoutManager(layoutManager);
            videosRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save videos to bundle
        if (adapter != null) {
            String key = getString(R.string.parcelable_video_array_key);
            outState.putParcelableArrayList(key, adapter.getVideos());
        }

        // Save scroll position
        GridLayoutManager manager = (GridLayoutManager) videosRecyclerView.getLayoutManager();
        if (manager != null) {
            int position = manager.findFirstVisibleItemPosition();
            outState.putInt(getString(R.string.videos_recyclerview_scroll_position), position);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore videos
            String key = getString(R.string.parcelable_video_array_key);
            if (savedInstanceState.containsKey(key)) {
                if (adapter != null) {
                    ArrayList<Video> videos = savedInstanceState.getParcelableArrayList(key);
                    if (videos != null) {
                        adapter.addVideos(videos);
                    }
                }
            }

            // Restore scroll position
            key = getString(R.string.videos_recyclerview_scroll_position);
            if (savedInstanceState.containsKey(key)) {
                scrollPosition = savedInstanceState.getInt(key);
            }
        }
    }

    private void playVideo(String videoId, Context context) {
        String endpoint = String.format(Locale.US, context.getString(R.string.youtube_url), videoId);
        Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(endpoint));
        videoIntent.putExtra("force_fullscreen", true);
        PackageManager pm = context.getPackageManager();
        boolean canPlayYoutubeVideo = pm.queryIntentActivities(videoIntent, 0).size() > 0;
        if (canPlayYoutubeVideo) {
            startActivity(Intent.createChooser(videoIntent, "Open with"));
        } else {
            Snackbar.make(snackbarParent, context.getString(R.string.player_missing), Snackbar.LENGTH_LONG).show();
        }
    }

    public void addVideos(List<Video> videos) {
        // NOTE: This method may be called before onCreateView()
        if (adapter != null) {
            adapter.addVideos(videos);
        }
        updateUI();
    }

    @Override
    protected String getTitle(Context context) {
        return context.getString(R.string.videos);
    }

    @Override
    public void onClick(View v) {
        // This fragment will try to play youtube videos on click events.
        if (v != null) {
            Object tag = v.getTag();
            if (tag != null) {
                String videoId = tag.toString();
                playVideo(videoId, v.getContext());
            }
        }
    }
}