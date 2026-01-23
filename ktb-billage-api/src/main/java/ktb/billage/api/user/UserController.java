package ktb.billage.api.user;

import jakarta.validation.Valid;
import ktb.billage.domain.user.dto.UserRequest;
import ktb.billage.domain.user.dto.UserResponse;
import ktb.billage.domain.user.service.UserService;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse.Id> join(@Valid @RequestBody UserRequest.Join request) {
        return ResponseEntity.ok().body(userService.join(request.loginId(), request.password()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse.MyProfile> getMyProfile(@AuthenticatedId Long userId) {
        return ResponseEntity.ok().body(userService.getMyProfile(userId));
    }
}
