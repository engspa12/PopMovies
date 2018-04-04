package com.example.dbm.popularmoviesstage2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class CollectionContract {

    public static final String CONTENT_AUTHORITY = "com.example.dbm.popularmoviesstage2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    private CollectionContract(){}

    public static final class CollectionEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_MOVIES);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public final static String TABLE_NAME = "movies";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_MOVIE_ID = "movie_id";
        public final static String COLUMN_MOVIE_NAME = "movie_name";
        public final static String COLUMN_MOVIE_RELEASE_DATE = "movie_release_date";
        public final static String COLUMN_MOVIE_RATING = "movie_rating";
        public final static String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";
        public final static String COLUMN_MOVIE_SYNOPSIS = "movie_synopsis";
        public final static String COLUMN_SAVED_MOVIE_IMAGE = "saved_image";

    }
}
