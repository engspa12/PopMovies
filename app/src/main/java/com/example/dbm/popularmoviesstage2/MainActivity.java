package com.example.dbm.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dbm.popularmoviesstage2.data.CollectionContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.GridItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG = MainActivity.class.getSimpleName();

    private String[] projection = {CollectionContract.CollectionEntry.COLUMN_MOVIE_ID, CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME,
            CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE, CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING,
            CollectionContract.CollectionEntry.COLUMN_MOVIE_POSTER_PATH,CollectionContract.CollectionEntry.COLUMN_MOVIE_SYNOPSIS};

    public static final String TAG = "MyTag";

    private static final String BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500";

    private static final String API_KEY = BuildConfig.API_KEY;


    private static final String LANGUAGE = "en-US";

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";

    private static final int NUM_GRID_ITEMS = 20;
    private static final int NUM_TOTAL = 100;

    private MoviesAdapter mAdapter;

    private RecyclerView mMoviesGrid;
    private TextView emptyTextView;

    private List<MovieItem> listOfMovies;

    private RequestQueue mRequestQueue;

    private String sortValue;
    private String typeAdapter;

    private static final int LOADER_ID = 1;

    private int page;

    private GridLayoutManager gridLayoutManager;

    private boolean alreadyLoaded;
    private int previousCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyTextView = (TextView) findViewById(R.id.empty_text_view);
        mMoviesGrid = (RecyclerView) findViewById(R.id.recycler_view_movies);

        gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesGrid.setLayoutManager(gridLayoutManager);
        mMoviesGrid.setHasFixedSize(true);
        mMoviesGrid.setAdapter(null);
        sortValue = getString(R.string.popularity_sort);
        typeAdapter = getString(R.string.type_adapter_value_api);
        listOfMovies = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        page = 1;
        previousCount=0;
        alreadyLoaded = false;

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline()){
            if(typeAdapter.equals(getString(R.string.type_adapter_value_api))) {
                if(!alreadyLoaded) {
                    startArrangement(sortValue);
                    alreadyLoaded = true;
                }
            }
            else if(typeAdapter.equals(getString(R.string.type_adapter_value_loader))){
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            }
        } else if(!(isOnline()) && typeAdapter.equals(getString(R.string.type_adapter_value_loader))){
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        } else{
            emptyTextView.setVisibility(View.VISIBLE);
            mMoviesGrid.setVisibility(View.GONE);
            emptyTextView.setText(getString(R.string.error_no_internet));
        }
    }


    @Override
    public void onGridItemClick(int clickedItemIndex) {
        Intent intent = new Intent(MainActivity.this,DetailActivity.class);
        Parcelable movie = listOfMovies.get(clickedItemIndex);
        intent.putExtra(getString(R.string.intent_tag_extra),movie);
        startActivity(intent);
    }

    public void getDataFromHttpUrlUsingJSON(String url){

        if(page == 1) {
            listOfMovies.clear();
        }
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray(getString(R.string.JSON_root));

                            for(int i=0;i<NUM_GRID_ITEMS;i++) {
                                JSONObject movie = results.getJSONObject(i);
                                int id = movie.getInt(getString(R.string.movie_id));
                                String title = movie.getString(getString(R.string.movie_title));
                                String synopsis = movie.getString(getString(R.string.movie_synopsis));
                                double rating = movie.getDouble(getString(R.string.movie_rating));
                                String releaseDate = movie.getString(getString(R.string.movie_release_date));
                                String posterPath = movie.getString(getString(R.string.movie_poster_path));

                                listOfMovies.add(new MovieItem(id,title,synopsis,rating,releaseDate,BASE_POSTER_URL + posterPath));
                            }
                            if(page==5) {
                                mAdapter = new MoviesAdapter(NUM_TOTAL, MainActivity.this, listOfMovies,MainActivity.this);
                                mMoviesGrid.setAdapter(mAdapter);
                                mMoviesGrid.setVisibility(View.VISIBLE);
                                emptyTextView.setVisibility(View.GONE);
                                page=1;
                            } else{
                                page++;
                                startArrangement(sortValue);
                            }

                        } catch( JSONException e){
                            Log.e(LOG,e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        emptyTextView.setText(getString(R.string.error_get_data));
                    }
                });

        jsonObjectRequest.setTag(TAG);

        mRequestQueue.add(jsonObjectRequest);
    }

    public URL buildUrl(String sortBy,int page){
        Uri movieQueryUri = Uri.parse(BASE_URL + sortBy).buildUpon()
                .appendQueryParameter(API_KEY_PARAM,API_KEY )
                .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE)
                .appendQueryParameter(PAGE_PARAM,String.valueOf(page))
                .build();
        try {
            URL movieQueryUrl = new URL(movieQueryUri.toString());
            return movieQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startArrangement(String sort){
            URL url = buildUrl(sort, page);
            getDataFromHttpUrlUsingJSON(url.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId){
            case R.id.popular_movies:
                previousCount = 0;
                typeAdapter = getString(R.string.type_adapter_value_api);
                if(isOnline()) {
                    sortValue = getString(R.string.popularity_sort);
                    startArrangement(sortValue);
                } else{
                    emptyTextView.setVisibility(View.VISIBLE);
                    mMoviesGrid.setVisibility(View.GONE);
                    emptyTextView.setText(getString(R.string.error_no_internet));
                }
                return true;
            case R.id.highest_rated_movies:
                previousCount = 0;
                typeAdapter = getString(R.string.type_adapter_value_api);
                if(isOnline()) {
                    sortValue = getString(R.string.highest_rate_sort);
                    startArrangement(sortValue);
                } else{
                    emptyTextView.setVisibility(View.VISIBLE);
                    mMoviesGrid.setVisibility(View.GONE);
                    emptyTextView.setText(getString(R.string.error_no_internet));
                }
                return true;
            case R.id.favorites_movies:
                typeAdapter = getString(R.string.type_adapter_value_loader);
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.sort_type), sortValue);
        outState.putString(getString(R.string.type_adapter_param),typeAdapter);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sortValue = savedInstanceState.getString(getString(R.string.sort_type));
        typeAdapter = savedInstanceState.getString(getString(R.string.type_adapter_param));
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getBaseContext(), CollectionContract.CollectionEntry.CONTENT_URI, null, null, null, null);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor.getCount() != 0 && cursor != null) {
                if(cursor.getCount() != previousCount) {
                    if (typeAdapter.equals(getString(R.string.type_adapter_value_loader))) {
                        if (isOnline()) {
                        getMovieDataFromCursor(cursor, true);
                        } else {
                        getMovieDataFromCursor(cursor, false);
                        }
                    }
                }
        } else if (typeAdapter.equals(getString(R.string.type_adapter_value_loader))) {
            listOfMovies.clear();
            emptyTextView.setVisibility(View.VISIBLE);
            mMoviesGrid.setVisibility(View.GONE);
            emptyTextView.setText(getString(R.string.no_favorites_message));
        }

        previousCount = cursor.getCount();
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMoviesGrid.setAdapter(null);
    }

    public void getMovieDataFromCursor(Cursor data,  boolean isConnected ){
        listOfMovies.clear();
        data.moveToFirst();
        int movie_id = data.getInt(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_ID));
        String movie_name = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME));
        String movie_release_date = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE));
        double movie_rating = data.getDouble(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING));
        String movie_poster_path = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_POSTER_PATH));
        String movie_synopsis = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_SYNOPSIS));

        if(!isConnected) {
            byte[] image = data.getBlob(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_SAVED_MOVIE_IMAGE));
            Bitmap imageBitmap = getImage(image);
            listOfMovies.add(new MovieItem(movie_id, movie_name, movie_synopsis, movie_rating, movie_release_date, movie_poster_path, imageBitmap));
        } else {
            listOfMovies.add(new MovieItem(movie_id, movie_name, movie_synopsis, movie_rating, movie_release_date, movie_poster_path));
        }

        while (data.moveToNext()) {
            movie_id = data.getInt(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_ID));
            movie_name = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME));
            movie_release_date = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE));
            movie_rating = data.getDouble(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING));
            movie_poster_path = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_POSTER_PATH));
            movie_synopsis = data.getString(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_MOVIE_SYNOPSIS));

            if(!isConnected) {
               byte[] image = data.getBlob(data.getColumnIndex(CollectionContract.CollectionEntry.COLUMN_SAVED_MOVIE_IMAGE));
                Bitmap imageBitmap = getImage(image);
                listOfMovies.add(new MovieItem(movie_id, movie_name, movie_synopsis, movie_rating, movie_release_date, movie_poster_path, imageBitmap));
            } else {
                listOfMovies.add(new MovieItem(movie_id, movie_name, movie_synopsis, movie_rating, movie_release_date, movie_poster_path));
            }
        }
        mAdapter = new MoviesAdapter(listOfMovies.size(), MainActivity.this, listOfMovies,MainActivity.this);
        mMoviesGrid.setAdapter(mAdapter);
        emptyTextView.setVisibility(View.GONE);
        mMoviesGrid.setVisibility(View.VISIBLE);
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

