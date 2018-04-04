package com.example.dbm.popularmoviesstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;

public class CollectionProvider extends ContentProvider {

    private static final int MOVIES = 100;
    private static final int MOVIE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{

        sUriMatcher.addURI(
                CollectionContract.CONTENT_AUTHORITY,CollectionContract.PATH_MOVIES,MOVIES);

        sUriMatcher.addURI(
                CollectionContract.CONTENT_AUTHORITY,CollectionContract.PATH_MOVIES + "/#",MOVIE_ID);
    }

    private CollectionDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new CollectionDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                cursor=database.query(CollectionContract.CollectionEntry.TABLE_NAME, projection, selection, selectionArgs,null,null,sortOrder);
                break;
            case MOVIE_ID:

                selection = CollectionContract.CollectionEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(CollectionContract.CollectionEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return CollectionContract.CollectionEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return CollectionContract.CollectionEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return insertMovie(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertMovie(Uri uri, ContentValues values) {

        Integer movieId = values.getAsInteger(CollectionContract.CollectionEntry.COLUMN_MOVIE_ID);

        if (movieId == null) {
            throw new IllegalArgumentException("Movie requires valid id");
        }

        String movieName = values.getAsString(CollectionContract.CollectionEntry.COLUMN_MOVIE_NAME);

        if (movieName == null) {
            throw new IllegalArgumentException("Movie requires valid name");
        }

        String releaseDateMovie = values.getAsString(CollectionContract.CollectionEntry.COLUMN_MOVIE_RELEASE_DATE);

        if (releaseDateMovie == null) {
            throw new IllegalArgumentException("Movie requires valid release date");
        }

        Double movieRating = values.getAsDouble(CollectionContract.CollectionEntry.COLUMN_MOVIE_RATING);

        if (movieRating == null) {
            throw new IllegalArgumentException("Movie requires valid rating");
        }

        //If everything is correct, we proceed to write into the database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long newRowId = database.insert(CollectionContract.CollectionEntry.TABLE_NAME,null,values);

        if (newRowId != -1){
            Toast.makeText(getContext(), "The movie was added successfully",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getContext(),"The movie couldn't be added as favorite",Toast.LENGTH_SHORT).show();
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                rowsDeleted = database.delete(CollectionContract.CollectionEntry.TABLE_NAME, selection, selectionArgs);
                database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + CollectionContract.CollectionEntry.TABLE_NAME + "'");
                break;
            case MOVIE_ID:
                selection = CollectionContract.CollectionEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(CollectionContract.CollectionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }


        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        if (rowsDeleted != 0) {
            Toast.makeText(getContext(), "Movie deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Movie deletion failed", Toast.LENGTH_SHORT).show();
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return updateMovies(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateMovies(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsUpdated = db.update(CollectionContract.CollectionEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0){
            Toast.makeText(getContext(),"Movie was updated successfully",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getContext(),"There was a problem during update",Toast.LENGTH_SHORT).show();
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
