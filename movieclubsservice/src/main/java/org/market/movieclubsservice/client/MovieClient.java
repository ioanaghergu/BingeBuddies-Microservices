package org.market.movieclubsservice.client;

import org.market.movieclubsservice.config.FeignClientConfig;
import org.market.movieclubsservice.dto.MovieDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movieservice", url = "${movieservice.url:http://localhost:8083}", configuration = FeignClientConfig .class)
public interface MovieClient {
    @GetMapping("/api/v1/movies/{id}")
    MovieDTO getMovieById(@PathVariable("id") Long id);
}
