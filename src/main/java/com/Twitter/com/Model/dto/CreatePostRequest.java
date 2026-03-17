package com.Twitter.com.Model.dto;

import com.Twitter.com.Model.Enum.PostType;
import lombok.Data;

@Data
public class CreatePostRequest {
    private String title;
    private String description;
    private String url;
    private PostType postType;
}
