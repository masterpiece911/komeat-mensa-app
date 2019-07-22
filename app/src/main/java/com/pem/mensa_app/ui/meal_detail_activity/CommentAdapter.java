package com.pem.mensa_app.ui.meal_detail_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Comment;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    ArrayList<Comment> mCommentList;

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView comment;
        public TextView timestamp;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.textView_fragment_comment_list);
            timestamp = itemView.findViewById(R.id.textView_timestamp);

        }
    }

    public CommentAdapter(ArrayList<Comment> commentList) {
        this.mCommentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(view);
        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = mCommentList.get(position);
        holder.comment.setText(currentComment.getContent());
        holder.timestamp.setText(currentComment.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

}
