package io.github.josephx86.popularmovies.data.videos;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephx86.popularmovies.R;

public class VideoViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.thumbnail_iv)
    ImageView thumbnailImageView;

    @BindView(R.id.title_tv)
    TextView titleTextView;

    View rootView;

    public VideoViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        ButterKnife.bind(this, itemView);
    }

    public void setVideo(Video video) {
        String site = video.getSite().toLowerCase();
        if (site.equals(("youtube"))) {
            titleTextView.setText(video.getName());
            String url = String.format(Locale.ENGLISH, "https://img.youtube.com/vi/%s/0.jpg", video.getKey());
            Picasso.get()
                    .load(url)
                    .into(thumbnailImageView);

            // Save the youtube video Id
            if (rootView != null) {
                rootView.setTag(video.getKey());
            }
        }
    }
}
