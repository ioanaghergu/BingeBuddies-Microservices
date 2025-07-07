package org.market.userservice.controller;

import jakarta.validation.Valid;
import org.market.userservice.domain.security.User;
import org.market.userservice.dto.UserDTO;
import org.market.userservice.exceptions.UserAlreadyExists;
import org.market.userservice.mapper.UserMapper;
import org.market.userservice.service.UserService;
import org.market.userservice.service.security.AuthorityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AuthorityService authorityService;

    public AuthController(UserMapper userMapper, UserService userService, AuthorityService authorityService) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.authorityService = authorityService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try{
            User newUser = userMapper.toUser(userDTO);
            newUser.setAuthorities(Set.of(authorityService.getAuthorityByRole("USER")));
            User savedUser = userService.save(newUser);

            UserDTO createdUserDTO = userMapper.toUserDTO(savedUser);
            createdUserDTO.setPassword(null);
            return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
        }catch (UserAlreadyExists e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Registration failed: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
