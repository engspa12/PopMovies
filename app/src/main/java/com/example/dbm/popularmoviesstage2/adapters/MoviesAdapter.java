package com.example.dbm.popularmoviesstage2.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.dbm.popularmoviesstage2.R;
import com.example.dbm.popularmoviesstage2.domain.MovieItem;

import java.util.List;

/**
 * Created by DBM on 3/21/2018.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private static final String LOG = MoviesAdapter.class.getSimpleName();

    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500";

    private final GridItemClickListener mOnClickListener;

    private List<MovieItem> mMovieList;

    private int mMovieItems;

    private Context mContext;

    public interface GridItemClickListener{
        void onGridItemClick(int clickedItemIndex);
    }

    public MoviesAdapter(int numberOfItems, GridItemClickListener listener, List<MovieItem> moviesList,Context context){
        mMovieList = moviesList;
        mMovieItems = numberOfItems;
        mOnClickListener = listener;
        mContext = context;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForGridItem = R.layout.movie_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForGridItem,parent,false);

        MovieViewHolder viewHolder = new MovieViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieItems;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView gridItemMovieImageView;

        private MovieViewHolder(View itemView){
            super(itemView);

            gridItemMovieImageView = (ImageView) itemView.findViewById(R.id.iv_item_movie);

            itemView.setOnClickListener(this);
        }

        public void bind(int gridIndex){

            if(isOnline()) {
                //Load poster from the network
                if (!mMovieList.get(gridIndex).getMoviePosterPath().equals(BASE_POSTER_URL + "null")) {
                    Glide.with(gridItemMovieImageView.getContext())
                            .load(mMovieList.get(gridIndex).getMoviePosterPath())
                            .placeholder(R.drawable.placeholder)
                            .into(gridItemMovieImageView);
                } else {
                    gridItemMovieImageView.setImageResource(R.drawable.without_poster);
                }
            }
            else {
                //Load poster from the database
                byte[] image = mMovieList.get(gridIndex).getMovieImageReplacement();
               gridItemMovieImageView.setImageBitmap(getImage(image));
           }

        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onGridItemClick(clickedPosition);
        }
    }

    //Check Internet Connectivity
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // convert from byte[] array to Bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
