package io.github.josephx86.popularmovies.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.videos.Video;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.movies.MoviesAdapter;
import io.github.josephx86.popularmovies.data.reviews.Review;

public class TMDBHelper {
    private static final String API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String CONFIG_URL = API_BASE_URL + "configuration";
    private static String baseImageUrl = null;
    private static String posterSize = null, backdropSize = null;
    private static int currentMovieListPage = 0, currentReviewListPage = 0;
    private static int totalMovieListPages = 1, totalReviewListPages = 1;


    private static void setConfiguration(Context context) {
        // https://developers.themoviedb.org/3/configuration/get-api-configuration

        String json = "";
        String key = context.getString(R.string.tmdb_api_key);
        String address = String.format(Locale.US, "%s?api_key=%s", CONFIG_URL, key);
        URL url;
        try {
            url = new URL(address);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\a");
            if (scanner.hasNext()) {
                json = scanner.next();
            }

            // Set the image base url
            JSONObject jsonObject = new JSONObject(json);
            JSONObject imagesObject = jsonObject.getJSONObject("images");
            baseImageUrl = imagesObject.getString("secure_base_url");

            // Select the poster size that is nearest to the width of the poster ImageView width.
            int imageViewWidth = (int) Utils.getPosterImageViewWidth(context);
            int nearestWidth = Integer.MAX_VALUE;
            JSONArray posterSizeArray = imagesObject.getJSONArray("poster_sizes");
            for (int i = 0; i < posterSizeArray.length(); i++) {
                String sizeString = posterSizeArray.getString(i);
                int size = Utils.parseFirstPositiveIntegerFromString(sizeString);
                int difference = Math.abs(imageViewWidth - size);
                if (difference < nearestWidth) {
                    nearestWidth = difference;
                    posterSize = sizeString;
                }
            }

            // Select backdrop image width that is nearest to device width
            int deviceWidth = context.getResources().getDisplayMetrics().widthPixels;
            nearestWidth = Integer.MAX_VALUE;
            for (int i = 0; i < posterSizeArray.length(); i++) {
                String sizeString = posterSizeArray.getString(i);
                int size = Utils.parseFirstPositiveIntegerFromString(sizeString);
                int difference = Math.abs(deviceWidth - size);
                if (difference < nearestWidth) {
                    nearestWidth = difference;
                    backdropSize = sizeString;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getBaseImageUrl() {
        return baseImageUrl;
    }

    public static String getPosterSize() {
        return posterSize;
    }

    public static String getBackdropSize() {
        return backdropSize;
    }

    public static void resetCurrentMovieListPage() {
        currentMovieListPage = 0;
        totalMovieListPages = 1;
    }

    private static void resetCurrentReviewPage() {
        currentReviewListPage = 0;
        totalReviewListPages = 1;
    }

    private static String httpGet(String endpoint) {
        String responseText = "";
        try {
            URL url = new URL(endpoint);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\a");
            if (scanner.hasNext()) {
                responseText = scanner.next();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseText;
    }

    public static void discoverMoviesAsync(final IWaitForMovies caller, final Context context) {
        final List<Movie> results = new ArrayList<>();

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, List<Movie>> task = new AsyncTask<Void, Void, List<Movie>>() {

            @Override
            protected List<Movie> doInBackground(Void... voids) {

                try {
                    // Fetching next page;
                    currentMovieListPage++;
                    if (currentMovieListPage > totalMovieListPages) {
                        // No need to make unnecessary HTTP call when all pages have already been fetched.
                        return results;
                    }

                    // First get the api configuration
                    setConfiguration(context);

                    String apiKey = context.getString(R.string.tmdb_api_key);
                    String path = "";
                    MoviesAdapter.SortType sortBy = Utils.getSortOrderPreference(context);
                    if (sortBy == MoviesAdapter.SortType.Rating) {
                        path = "movie/top_rated";
                    } else if (sortBy == MoviesAdapter.SortType.Popularity) {
                        path = "movie/popular";
                    }
                    String apiEndpoint = String.format(Locale.US, "%s%s?api_key=%s&language=en-US&page=%d",
                            API_BASE_URL, path, apiKey, currentMovieListPage);
                    String json = httpGet(apiEndpoint);

                    // Parse JSON data
                    if (!json.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(json);
                        currentMovieListPage = jsonObject.getInt("page");
                        totalMovieListPages = jsonObject.getInt("total_pages");

                        // Get all movies in JSON
                        JSONArray movieArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < movieArray.length(); i++) {
                            Movie movie = new Movie();
                            JSONObject movieObject = movieArray.getJSONObject(i);

                            int id = movieObject.getInt("id");
                            movie.setId(id);

                            int votes = movieObject.getInt("vote_count");
                            movie.setVoteCount(votes);

                            boolean video = movieObject.getBoolean("video");
                            movie.setVideo(video);

                            double voteAverage = movieObject.getDouble("vote_average");
                            movie.setVoteAverage(voteAverage);

                            String title = movieObject.getString("title");
                            movie.setTitle(title);

                            double popularity = movieObject.getDouble("popularity");
                            movie.setPopularity(popularity);

                            String posterPath = movieObject.getString("poster_path");
                            movie.setPosterPath(posterPath);

                            String originalTitle = movieObject.getString("original_title");
                            movie.setOriginalTitle(originalTitle);

                            String originalLanguage = movieObject.getString("original_language");
                            movie.setOriginalLanguage(originalLanguage);

                            String backdropPath = movieObject.getString("backdrop_path");
                            movie.setBackdropPath(backdropPath);

                            boolean adult = movieObject.getBoolean("adult");
                            movie.setAdult(adult);

                            String overview = movieObject.getString("overview");
                            movie.setOverview(overview);

                            String releaseDate = movieObject.getString("release_date");
                            movie.setReleaseDate(Utils.parseDate(releaseDate));

                            results.add(movie);
                        }
                    } else {
                        // Something went wrong and pages was not actually fetched.
                        currentMovieListPage--;
                        if (currentMovieListPage < 0) {
                            currentMovieListPage = 0;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<Movie> results) {
                super.onPostExecute(results);
                if (caller != null) {
                    caller.processReceivedMovies(results);
                }
            }
        };
        task.execute();
    }

    public static void getReviews(final IWaitForReviews caller, final Context context, final int movieId, boolean isFirstRequest) {
        // If this is the first time (for the current movie) reviews are being requested, reset the counters.
        if (isFirstRequest) {
            resetCurrentReviewPage();
        }

        AsyncTask<Void, Void, List<Review>> reviewsTask = new AsyncTask<Void, Void, List<Review>>() {

            @Override
            protected List<Review> doInBackground(Void... voids) {
                List<Review> reviews = new ArrayList<>();

                // Fetching next page;
                currentReviewListPage++;
                if (currentReviewListPage > totalReviewListPages) {
                    // No need to make unnecessary HTTP call when all pages have already been fetched.
                    return reviews;
                }

                String apiKey = context.getString(R.string.tmdb_api_key);
                String reviewsEndpoint = String.format(Locale.US, "%smovie/%d/reviews?api_key=%s&language=en-US&page=%d",
                        API_BASE_URL, movieId, apiKey, currentReviewListPage);
                String json = httpGet(reviewsEndpoint);

                // Parse JSON data
                if (!json.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);

                        int totalReviews = jsonObject.getInt("total_results");
                        Review.setTotalReviews(totalReviews);

                        currentReviewListPage = jsonObject.getInt("page");
                        totalReviewListPages = jsonObject.getInt("total_pages");

                        // Read reviews from JSON
                        JSONArray reviewsArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < reviewsArray.length(); i++) {
                            JSONObject reviewObject = reviewsArray.getJSONObject(i);

                            String author = reviewObject.getString("author");
                            String content = reviewObject.getString("content");
                            Review rev = new Review(author, content);
                            reviews.add(rev);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return reviews;
            }

            @Override
            protected void onPostExecute(List<Review> movieReviews) {
                super.onPostExecute(movieReviews);
                if (caller != null) {
                    caller.processReceivedReviews(movieReviews);
                }
            }
        };
        reviewsTask.execute();
    }

    public static void getVideos(final IWaitForVideos caller, final Context context, final int movieId) {
        AsyncTask<Void, Void, List<Video>> videosTask = new AsyncTask<Void, Void, List<Video>>() {

            @Override
            protected List<Video> doInBackground(Void... voids) {
                List<Video> videos = new ArrayList<>();

                String apiKey = context.getString(R.string.tmdb_api_key);
                String videosEndpoint = String.format(Locale.US, "%smovie/%d/videos?api_key=%s&language=en-US",
                        API_BASE_URL, movieId, apiKey);
                String json = httpGet(videosEndpoint);

                // Parse JSON data
                if (!json.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        // Read videos from JSON
                        JSONArray videosArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < videosArray.length(); i++) {
                            JSONObject videoObject = videosArray.getJSONObject(i);

                            String key = videoObject.getString("key");
                            String name = videoObject.getString("name");
                            String site = videoObject.getString("site");
                            Video vid = new Video(key, name, site);
                            videos.add(vid);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return videos;
            }

            @Override
            protected void onPostExecute(List<Video> videos) {
                super.onPostExecute(videos);
                if (caller != null) {
                    caller.processReceivedVideos(videos);
                }
            }
        };
        videosTask.execute();
    }
}
