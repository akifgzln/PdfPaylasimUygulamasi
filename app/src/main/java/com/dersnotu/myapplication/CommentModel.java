package com.dersnotu.myapplication;

public class CommentModel {
    private String userName;
    private String userSurname;
    private String commentText;
    private String profileImageUrl;
    private Long timestamp; // YENİ: Zaman damgası (milisaniye cinsinden)

    // Kurucu metod güncellendi (Artık zamanı da alıyor)
    public CommentModel(String userName, String userSurname, String commentText, String profileImageUrl, Long timestamp) {
        this.userName = userName;
        this.userSurname = userSurname;
        this.commentText = commentText;
        this.profileImageUrl = profileImageUrl;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public String getUserSurname() { return userSurname; }
    public String getCommentText() { return commentText; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public Long getTimestamp() { return timestamp; } // YENİ: Getter
}