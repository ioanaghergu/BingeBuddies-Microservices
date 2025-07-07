package org.market.movieclubsservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MovieClubAlreadyExistsException extends RuntimeException {
    public MovieClubAlreadyExistsException(String message) {
        super(message);
    }
}
