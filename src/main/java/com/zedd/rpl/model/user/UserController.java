package com.zedd.rpl.model.user;

import com.zedd.rpl.model.user.req.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService service;

    @PostMapping("/me/password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void changePassword(
            @RequestBody
            @Valid
            final ChangePasswordRequest request,
            final Authentication principal) {
        this.service.changePassword(request, getUserId(principal));
    }

    private Long getUserId(final Authentication authentication) {
        return ((User) Objects.requireNonNull(authentication.getPrincipal())).getId();
    }

}
