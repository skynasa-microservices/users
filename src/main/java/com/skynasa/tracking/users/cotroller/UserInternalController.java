package com.skynasa.tracking.users.cotroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skynasa.tracking.users.model.dto.UserDto;
import com.skynasa.tracking.users.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.internal-prefix}")
public class UserInternalController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-user-with-children/{id}")
    public List<UserDto> getUserWithChildren(@PathVariable UUID id) {

        List<UserDto> results = userService.getUserWithChildren(id);

        return results;

    }

    @GetMapping("/get-current-user-with-children")
    public List<UserDto> getCurrentUserWithChildren() {

        return userService.getCurrentUserChildren();
    }

    @GetMapping("/find-current-user")
    public UserDto findCurrentUser() {
        return userService.findCurrentUser();
    }

    @GetMapping("/find-user-by-username/{username}")
    public UserDto findUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username);
    }

    @GetMapping("/find-by-keycloak-user-id/{keycloakUserId}")
    public UserDto findByKeycloakUserId(@PathVariable String keycloakUserId) {
        return userService.findByKeycloakUserId(keycloakUserId);
    }
}
