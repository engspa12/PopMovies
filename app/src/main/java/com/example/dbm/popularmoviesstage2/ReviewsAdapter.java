package com.example.dbm.popularmoviesstage2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private static final String LOG = ReviewsAdapter.class.getSimpleName();

    private List<Review> mReviewList;

    private int mReviewItems;

    private Context mContext;

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem,parent,shouldAttachToParentImmediately);

        ReviewViewHolder viewHolder = new ReviewViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mReviewItems;
    }


    public ReviewsAdapter(int numberOfItems, List<Review> reviewsList,Context context){
        mReviewList = reviewsList;
        mReviewItems = numberOfItems;
        mContext = context;
    }


    public class ReviewViewHolder extends RecyclerView.ViewHolder{

        TextView authorReviewTextView;
        TextView contentReviewTextView;

        private ReviewViewHolder(View itemView){
            super(itemView);

            authorReviewTextView = (TextView) itemView.findViewById(R.id.tv_author_review);
            contentReviewTextView = (TextView) itemView.findViewById(R.id.tv_content_review);

        }

        public void bind(int listIndex){
            if(!mReviewList.get(listIndex).getReviewAuthor().equals("")) {
                authorReviewTextView.setText(mContext.getString(R.string.review_written_by,mReviewList.get(listIndex).getReviewAuthor()));
            }

            if(!mReviewList.get(listIndex).getReviewContent().equals("")){
                contentReviewTextView.setText(mReviewList.get(listIndex).getReviewContent());
            }
        }

    }
}
