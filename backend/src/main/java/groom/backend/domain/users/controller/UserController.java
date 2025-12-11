package groom.backend.domain.users.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.users.dto.request.GuardianMatchRequest;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.service.spec.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll(
            @AuthenticationPrincipal AuthUser user
    ) {

        List<UserResponse> result = userService.getAllUsers();

        return result;
    }

    @GetMapping("/{userId}")
    public UserResponse findOne(@PathVariable("userId") UUID userId) {

        UserResponse user = userService.getUserById(userId);

        return user;
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable("userId") UUID userId, @Valid UpdateUserRequest req) {

        UserResponse updateUser = userService.updateUser(userId, req);

        return updateUser;
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@PathVariable("userId") UUID userId) {
        userService.deleteUser(userId);
    }

    @PostMapping("/guardian")
    public void guardianMatch(@AuthenticationPrincipal AuthUser user, @RequestBody GuardianMatchRequest req) {

        UUID userId = user.userId();
        String email = req.email();

        userService.guardianMatch(userId, email);
    }
}