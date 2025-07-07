package org.market.userservice.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/test")
public class LoadBalancing {

    @Value("${server.port}")
    private String port;

    @GetMapping("/instanceId")
    public ResponseEntity<?> getInstanceId() {
        return ResponseEntity.ok("User Service running on port: " + port);
    }

}
