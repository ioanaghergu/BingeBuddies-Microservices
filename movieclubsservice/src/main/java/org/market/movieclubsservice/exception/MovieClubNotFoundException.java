package org.market.movieclubsservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MovieClubNotFoundException extends RuntimeException {
    public MovieClubNotFoundException(String message) {
        super(message);
    }
}
