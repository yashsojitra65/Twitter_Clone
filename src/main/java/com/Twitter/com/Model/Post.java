package com.Twitter.com.Model;

import com.Twitter.com.Model.Enum.PostType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer postId;
    public String title;
    public String description;
    public String url;
    @Enumerated(EnumType.STRING)
    private PostType postType;


    @CurrentTimestamp
    public String time;

    @PrePersist
    private void prePersist() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Define your desired date/time format
        this.time = sdf.format(new Date(System.currentTimeMillis()));
        if (this.postType == null) {
            this.postType = PostType.TEXT;
        }
    }

    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JoinColumn(name = "fk_post_userid")
    private User postOwner;
}
