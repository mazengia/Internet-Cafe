package com.maze.internet_cafe.exception;

import lombok.Data;

@Data
public class ShareholderVotedException extends RuntimeException {
    private String message;

    public ShareholderVotedException(String message) {
        this.message = message;
    }
}
