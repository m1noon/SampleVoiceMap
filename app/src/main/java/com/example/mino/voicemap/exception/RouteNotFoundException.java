package com.example.mino.voicemap.exception;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class RouteNotFoundException extends RuntimeException{
    private static final String TAG = RouteNotFoundException.class.getSimpleName();

    public RouteNotFoundException() {
    }

    public RouteNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public RouteNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RouteNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
