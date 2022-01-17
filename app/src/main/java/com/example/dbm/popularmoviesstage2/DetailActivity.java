package com.example.dbm.popularmoviesstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import androidx.core.app.ShareCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.dbm.popularmoviesstage2.adapters.ReviewsAdapter;
import com.example.dbm.popularmoviesstage2.adapters.TrailersAdapter;
import com.example.dbm.popularmoviesstage2.classes.MovieItem;
import com.example.dbm.popularmoviesstage2.classes.Review;
import com.example.dbm.popularmoviesstage2.classes.Trailer;
import com.example.dbm.popularmoviesstage2.data.CollectionContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements TrailersAdapter.ClickListener {

    private Uri wantedUri;

    private String[] projection = {CollectionContract.CollectionEntry.COLUMN_MOVIE_ID, CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME,
            CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE, CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING,
            CollectionContract.CollectionEntry.COLUMN_MOVIE_POSTER_PATH,CollectionContract.CollectionEntry.COLUMN_MOVIE_SYNOPSIS};


    private static final String LOG = DetailActivity.class.getSimpleName();

    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500";

    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";


    private static final String API_KEY = BuildConfig.API_KEY;

    private static final String LANGUAGE = "en-US";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";

    public static final String TAG = "Details";

    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mMovieReleaseDate;
    private TextView mMovieRating;
    private TextView mMovieSynopsis;

    private MovieItem movie;

    private Button markAsFavoriteButton;

    private List<Review> listOfReviews;
    private List<Trailer> listOfTrailers;

    private RequestQueue mQueue;
    private ReviewsAdapter reviewsAdapter;

    private TextView emptyTextViewReviews;

    private RecyclerView recyclerViewReviews;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private RecyclerView recyclerViewTrailers;
    private TrailersAdapter trailersAdapter;

    private String typeSearch;

    private Cursor mCursor;
    private ContentValues values;

    private TextView emptyTextView;

    private String sourceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        if (intent.hasExtra("source_type")) {
            sourceType = intent.getStringExtra("source_type");
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        recyclerViewReviews = (RecyclerView) findViewById(R.id.recycler_view_reviews);
        recyclerViewTrailers = (RecyclerView) findViewById(R.id.recycler_view_trailers);
        emptyTextView = (TextView) findViewById(R.id.empty_text_view_detail);
        markAsFavoriteButton = (Button) findViewById(R.id.mark_as_favorite_button);
        emptyTextViewReviews = (TextView) findViewById(R.id.empty_text_view_reviews);

        emptyTextView.setVisibility(View.GONE);

        if(!isOnline() && sourceType.equals("api")){
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.error_no_internet));
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
        } else{
            if (intent.hasExtra(getString(R.string.intent_tag_extra))) {
                typeSearch = getResources().getString(R.string.trailers_param);

                // Get a support ActionBar corresponding to this toolbar
                ActionBar ab = getSupportActionBar();
                // Enable the Up button
                ab.setDisplayHomeAsUpEnabled(true);

                progressBar.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);

                listOfReviews = new ArrayList<>();

                listOfTrailers = new ArrayList<>();

                mQueue = Volley.newRequestQueue(this);

                emptyTextViewReviews.setVisibility(View.GONE);

                //RecyclerView Reviews
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerViewReviews.setLayoutManager(linearLayoutManager);
                recyclerViewReviews.setHasFixedSize(true);
                recyclerViewReviews.setNestedScrollingEnabled(false);

                //RecyclerView Trailers
                LinearLayoutManager linearLayoutManagerTrailers = new LinearLayoutManager(this);
                recyclerViewTrailers.setLayoutManager(linearLayoutManagerTrailers);
                recyclerViewTrailers.setHasFixedSize(true);
                recyclerViewTrailers.setNestedScrollingEnabled(false);

                movie = (MovieItem) intent.getParcelableExtra(getString(R.string.intent_tag_extra));
                wantedUri = ContentUris.withAppendedId(CollectionContract.CollectionEntry.CONTENT_URI, movie.getMovieId());
                mCursor = getContentResolver().query(wantedUri, projection, null, null, null);
                populateDetailUI();
                markAsFavoriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Insert or Delete Movie
                        insertOrDeleteMovie();
                    }
                });
                getAdditionalMovieInfo(typeSearch);
            }
        }


    }

    private void insertOrDeleteMovie(){

        if(wantedUri != null) {
            //Insert new movie
            if (mCursor.getCount() == 0) {

                values = new ContentValues();
                byte[] imageByteForStorage;
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_ID, movie.getMovieId());
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME, movie.getMovieTitle());
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getMovieReleaseDate());
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING, movie.getMovieRating());
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_POSTER_PATH, movie.getMoviePosterPath());
                values.put(CollectionContract.CollectionEntry.COLUMN_MOVIE_SYNOPSIS, movie.getMovieSynopsis());

                if (!movie.getMoviePosterPath().equals(BASE_POSTER_URL + "null")) {
                        Glide.with(this)
                                .load(movie.getMoviePosterPath())
                                .asBitmap()
                                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        values.put(CollectionContract.CollectionEntry.COLUMN_SAVED_MOVIE_IMAGE,getBytes(resource));
                                        getContentResolver().insert(CollectionContract.CollectionEntry.CONTENT_URI, values);
                                    }
                                });
                } else {
                    Bitmap bitmapReplacement = BitmapFactory.decodeResource(getResources(),
                            R.drawable.without_poster);
                    imageByteForStorage = getBytes(bitmapReplacement);
                    values.put(CollectionContract.CollectionEntry.COLUMN_SAVED_MOVIE_IMAGE,imageByteForStorage);
                    getContentResolver().insert(CollectionContract.CollectionEntry.CONTENT_URI, values);
                }
                markAsFavoriteButton.setText(getString(R.string.remove_favorite));

            } else {
                //Delete already stored movie
                getContentResolver().delete(wantedUri, null, null);
                markAsFavoriteButton.setText(getString(R.string.mark_as_favorite_button_text));
           }
        }
    }

    //Convert from Bitmap to byte[]
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }


    //Populate UI components
    private void populateDetailUI() {

        mMovieTitle = findViewById(R.id.movie_title_tv);
        mMoviePoster = findViewById(R.id.poster_image_iv);
        mMovieReleaseDate = findViewById(R.id.movie_release_date_tv);
        mMovieRating = findViewById(R.id.movie_rating_tv);
        mMovieSynopsis = findViewById(R.id.movie_plot_synopsis_textview);

        mMovieTitle.setText(movie.getMovieTitle());

        if (!(movie.getMovieReleaseDate().equals("") || movie.getMovieReleaseDate() == null)) {
            mMovieReleaseDate.setText(movie.getMovieReleaseDate());
        } else {
            mMovieReleaseDate.setText(getString(R.string.unknown_release_date));
        }

        mMovieRating.setText(String.valueOf(movie.getMovieRating() + "/10"));

        if (!(movie.getMovieSynopsis().equals("") || movie.getMovieSynopsis() == null)) {
            mMovieSynopsis.setText(movie.getMovieSynopsis());
        } else {
            mMovieSynopsis.setText(getString(R.string.unknown_plot_synopsis));
        }

        //Load poster from network or database
        if(isOnline()){
            //There is a poster
            if (!movie.getMoviePosterPath().equals(BASE_POSTER_URL + "null")) {

                Glide.with(this)
                        .load(movie.getMoviePosterPath())
                        .placeholder(R.drawable.placeholder)
                        .into(mMoviePoster);
            } else {
                //The movie does not have a poster
                mMoviePoster.setImageResource(R.drawable.without_poster);
            }
        } else{
            byte[] image = movie.getMovieImageReplacement();
            mMoviePoster.setImageBitmap(getImage(image));
        }

        if(mCursor.getCount() == 0){
            markAsFavoriteButton.setText(getString(R.string.mark_as_favorite_button_text));
        } else{
            markAsFavoriteButton.setText(getString(R.string.remove_favorite));
       }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCursor != null){
            mCursor.close();
        }
    }


    /**
     * This method is called to obtain Reviews and Trailers
     * from the API after the url has been built.
     * @param endpoint This param specifies the employed endpoint.
     */
    public void getAdditionalMovieInfo(String endpoint) {
        //Get Reviews and Trailers
        URL url = buildUrl(endpoint);
        getDataFromHttpUrl(url.toString());
    }


    /**
     * This method is used to make the an API request using the
     * library Volley.
     * @param urlForRequest The url used to request data from the API.
     */
    public void getDataFromHttpUrl(String urlForRequest) {

        if (mQueue != null) {
            mQueue.cancelAll(TAG);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, urlForRequest, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray(getString(R.string.JSON_root));
                            //Get Trailers
                            if (typeSearch.equals(getString(R.string.trailers_param))) {
                                int totalTrailerItems = results.length();
                                for (int i = 0; i < totalTrailerItems; i++) {
                                    JSONObject movie = results.getJSONObject(i);
                                    String keyTrailer = movie.getString(getString(R.string.trailer_video_key));
                                    listOfTrailers.add(new Trailer(keyTrailer));
                                }
                                trailersAdapter = new TrailersAdapter(listOfTrailers.size(),listOfTrailers,DetailActivity.this,DetailActivity.this);
                                recyclerViewTrailers.setAdapter(trailersAdapter);
                                typeSearch = getResources().getString(R.string.reviews_param);
                                getAdditionalMovieInfo(typeSearch);

                            } else {
                                //Get Reviews
                                int totalReviewItems = response.getInt(getString(R.string.query_total_results));
                                if(!(totalReviewItems == 0)) {
                                    try {
                                        for (int i = 0; i < totalReviewItems; i++) {
                                            JSONObject movie = results.getJSONObject(i);
                                            String author = movie.getString(getString(R.string.author_of_review));
                                            String content = movie.getString(getString(R.string.content_of_review));
                                            listOfReviews.add(new Review(author, content));
                                        }
                                        reviewsAdapter = new ReviewsAdapter(listOfReviews.size(), listOfReviews, DetailActivity.this);
                                        recyclerViewReviews.setAdapter(reviewsAdapter);
                                    } catch(JSONException e){
                                        reviewsAdapter = new ReviewsAdapter(listOfReviews.size(), listOfReviews, DetailActivity.this);
                                        recyclerViewReviews.setAdapter(reviewsAdapter);
                                    }
                                } else {
                                    recyclerViewReviews.setVisibility(View.GONE);
                                    emptyTextViewReviews.setVisibility(View.VISIBLE);
                                }
                                progressBar.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            Log.e(LOG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        emptyTextViewReviews.setText(getString(R.string.error_get_data));
                    }
                });

        jsonObjectRequest.setTag(TAG);

        mQueue.add(jsonObjectRequest);
    }

    //Watch the first trailer via YouTube
    public void viewTrailer(String trailerUrl){
        Uri urlVideo = Uri.parse(trailerUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW,urlVideo);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    /**
     * This method is used to build an URL object from an String input.
     * @param endpointType This is the desired endpoint for the API call.
     * @return URL This returns an URL object formed from the passed String.
     */
    public URL buildUrl(String endpointType) {
        Uri queryUri = Uri.parse(BASE_URL + movie.getMovieId() + "/" + endpointType).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE)
                .build();
        try {
            return new URL(queryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int clickedItemIndex) {
        viewTrailer(YOUTUBE_URL + listOfTrailers.get(clickedItemIndex).getTrailerKey());
    }

    @Override
    public void onItemClickShare(int clickedItemIndex) {
        //Share movie data using ShareCompat
        String mimeType = "text/plain";
        String title = "Share Content";
        String textToShare = YOUTUBE_URL + listOfTrailers.get(clickedItemIndex).getTrailerKey();

        ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(textToShare)
                .startChooser();
    }

    //Check Internet Connectivity
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // convert from byte[] array to Bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
