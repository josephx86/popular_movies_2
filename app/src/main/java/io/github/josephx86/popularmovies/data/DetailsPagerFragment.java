package io.github.josephx86.popularmovies.data;

import android.content.Context;
import android.support.v4.app.Fragment;

public abstract class DetailsPagerFragment extends Fragment {
    protected abstract String getTitle(Context context);
}
