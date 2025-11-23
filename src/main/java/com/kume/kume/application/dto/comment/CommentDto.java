package com.kume.kume.application.dto.comment;

import java.util.List;

public class CommentDto {
    private Long id;
    private String content;
    private boolean hidden;
    private int likes;
    private int dislikes;
    private List<CommentDto> replies;
}
