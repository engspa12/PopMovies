package com.example.dbm.popularmoviesstage2.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dbm.popularmoviesstage2.R;
import com.example.dbm.popularmoviesstage2.domain.Trailer;

import java.util.List;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private static final String LOG = TrailersAdapter.class.getSimpleName();

    private List<Trailer> mTrailerList;

    private int mTrailerItems;

    private Context mContext;

    private final ClickListener mOnClickListener;

    public interface ClickListener{
        void onItemClick(int clickedItemIndex);
        void onItemClickShare(int clickedItemIndex);
    }


    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem,parent,false);

        TrailerViewHolder viewHolder = new TrailerViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mTrailerItems;
    }

    public TrailersAdapter(int numberOfItems, List<Trailer> trailersList, ClickListener listener,Context context){
        mTrailerList = trailersList;
        mTrailerItems = numberOfItems;
        mOnClickListener = listener;
        mContext = context;
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView trailerImageView;
        TextView trailerTextView;
        ImageView shareImageView;

        private TrailerViewHolder(View itemView){
            super(itemView);
            trailerImageView = (ImageView) itemView.findViewById(R.id.iv_trailer);
            trailerTextView = (TextView) itemView.findViewById(R.id.tv_trailer);
            shareImageView = (ImageView) itemView.findViewById(R.id.iv_share);
        }

        public void bind(int listIndex){
            shareImageView.setVisibility(View.INVISIBLE);
            trailerImageView.setOnClickListener(this);
            if(listIndex == 0) {
                shareImageView.setVisibility(View.VISIBLE);
                shareImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int clickedPosition = getAdapterPosition();
                        mOnClickListener.onItemClickShare(clickedPosition);
                    }
                });
            }
            trailerTextView.setText(mContext.getString(R.string.trailer_item,String.valueOf(listIndex + 1)));
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onItemClick(clickedPosition);
        }
    }
}
