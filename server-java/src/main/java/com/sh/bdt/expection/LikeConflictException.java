package com.sh.bdt.expection;

public class LikeConflictException extends RuntimeException {

    public LikeConflictException() {
        super("The like status has already been reflected");
    }
}
