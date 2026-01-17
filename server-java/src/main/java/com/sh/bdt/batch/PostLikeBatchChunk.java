package com.sh.bdt.batch;

public record PostLikeBatchChunk(long batchSeq,
                                 long postId,
                                 long userId,
                                 int status) { }
