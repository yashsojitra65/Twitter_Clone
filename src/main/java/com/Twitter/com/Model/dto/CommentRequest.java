package com.Twitter.com.Model.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Integer postId;
    private String text;
}
