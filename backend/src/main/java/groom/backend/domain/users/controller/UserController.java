package groom.backend.domain.users.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> findAll(
            @AuthenticationPrincipal AuthUser user
    ) {
        UUID userId = user.userId();
        Role role = user.role();
        return null;
    }
}
