package com.example.dbm.popularmoviesstage2;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by DBM on 3/22/2018.
 */

public class MovieItem implements Parcelable{


    private Bitmap mMovieBitmap;
    private int mMovieId;
    private String mMovieTitle;
    private String mMovieSynopsis;
    private double mMovieRating;
    private String mMovieReleaseDate;
    private String mMoviePosterPath;

    public MovieItem(int movieId, String movieTitle,String movieSynopsis, double movieRating, String movieReleaseDate, String moviePosterPath){
        mMovieId = movieId;
        mMovieTitle = movieTitle;
        mMovieSynopsis = movieSynopsis;
        mMovieRating = movieRating;
        mMovieReleaseDate = movieReleaseDate;
        mMoviePosterPath = moviePosterPath;
    }

    public MovieItem(int movieId, String movieTitle,String movieSynopsis, double movieRating, String movieReleaseDate, String moviePosterPath, Bitmap bitmap){
        mMovieId = movieId;
        mMovieTitle = movieTitle;
        mMovieSynopsis = movieSynopsis;
        mMovieRating = movieRating;
        mMovieReleaseDate = movieReleaseDate;
        mMoviePosterPath = moviePosterPath;
        mMovieBitmap = bitmap;
    }

    private MovieItem(Parcel in) {
        mMovieId = in.readInt();
        mMovieTitle = in.readString();
        mMovieSynopsis = in.readString();
        mMovieRating = in.readDouble();
        mMovieReleaseDate = in.readString();
        mMoviePosterPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mMovieId);
        out.writeString(mMovieTitle);
        out.writeString(mMovieSynopsis);
        out.writeDouble(mMovieRating);
        out.writeString(mMovieReleaseDate);
        out.writeString(mMoviePosterPath);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };


    public int getMovieId(){return mMovieId;}

    public String getMovieTitle(){return mMovieTitle;}

    public String getMovieSynopsis(){return mMovieSynopsis;}

    public double getMovieRating(){return mMovieRating;}

    public String getMovieReleaseDate(){return mMovieReleaseDate;}

    public String getMoviePosterPath(){return mMoviePosterPath;}

    public Bitmap getMovieBitmap(){return mMovieBitmap;}

}
