package com.dersnotu.myapplication;

import android.text.format.DateUtils; // Zaman formatı için
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentModel> commentList;

    public CommentsAdapter(List<CommentModel> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel currentComment = commentList.get(position);

        // İsim Soyisim
        String fullName = currentComment.getUserName() + " " + currentComment.getUserSurname();
        holder.txtUserName.setText(fullName);

        // Yorum İçeriği
        holder.txtContent.setText(currentComment.getCommentText());

        // --- YENİ KISIM: ZAMAN HESAPLAMA ---
        if (currentComment.getTimestamp() != null && currentComment.getTimestamp() > 0) {
            // Şimdiki zamanı al
            long now = System.currentTimeMillis();
            // Yorumun zamanını al
            long time = currentComment.getTimestamp();

            // "3 dakika önce", "Dün" gibi otomatik çevir
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            holder.txtDate.setText(timeAgo);
        } else {
            holder.txtDate.setText("Az önce");
        }

        // Profil Resmi
        Glide.with(holder.itemView.getContext())
                .load(currentComment.getProfileImageUrl())
                .placeholder(R.color.black)
                .error(R.color.black)
                .centerCrop()
                .into(holder.imgProfile);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtContent, txtDate; // txtDate eklendi
        ImageView imgProfile;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtCommentUserName);
            txtContent = itemView.findViewById(R.id.txtCommentContent);
            txtDate = itemView.findViewById(R.id.txtCommentDate); // item_comment.xml'deki tarih ID'si
            imgProfile = itemView.findViewById(R.id.imgCommentProfile);
        }
    }
}