package com.example.dbm.popularmoviesstage2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private static final String LOG = TrailersAdapter.class.getSimpleName();

    private List<Trailer> mTrailerList;

    private int mTrailerItems;

    private Context mContext;

    private final ClickListener mOnClickListener;

    public interface ClickListener{
        void onItemClick(int clickedItemIndex);
    }


    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem,parent,shouldAttachToParentImmediately);

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

        private TrailerViewHolder(View itemView){
            super(itemView);
            trailerImageView = (ImageView) itemView.findViewById(R.id.iv_trailer);
            trailerTextView = (TextView) itemView.findViewById(R.id.tv_trailer);
        }

        public void bind(int listIndex){
            trailerImageView.setOnClickListener(this);
            trailerTextView.setText(mContext.getString(R.string.trailer_item,String.valueOf(listIndex + 1)));
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onItemClick(clickedPosition);
        }
    }
}
