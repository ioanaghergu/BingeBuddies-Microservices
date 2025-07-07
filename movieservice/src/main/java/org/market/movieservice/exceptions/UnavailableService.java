package org.market.movieservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UnavailableService extends RuntimeException {
    public UnavailableService(String message) {
        super(message);
    }
}
