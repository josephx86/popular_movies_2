package io.github.josephx86.popularmovies.data.videos;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {
    private String key = "", name = "", site = "";

    public Video(String key, String name, String site) {
        this.key = key;
        this.name = name;
        this.site = site;
    }

    protected Video(Parcel in) {
        key = in.readString();
        name = in.readString();
        site = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
    }
}
