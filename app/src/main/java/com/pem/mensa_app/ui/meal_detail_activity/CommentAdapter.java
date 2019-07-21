package com.pem.mensa_app.ui.meal_detail_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    ArrayList<String> mCommentList;

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView comment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.textView_fragment_comment_list);
        }
    }

    public CommentAdapter(ArrayList<String> commentList) {
        this.mCommentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_comment, parent, false);
        CommentViewHolder commentViewHolder = new CommentViewHolder(view);
        return commentViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        String currentComment = mCommentList.get(position);
        holder.comment.setText(currentComment);
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

}
