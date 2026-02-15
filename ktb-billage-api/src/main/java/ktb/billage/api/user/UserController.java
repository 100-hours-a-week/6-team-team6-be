package ktb.billage.api.user;

import jakarta.validation.Valid;
import ktb.billage.apidoc.UserApiDoc;
import ktb.billage.domain.membership.service.MembershipService;
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

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserApiDoc {
    private final UserService userService;
    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<UserResponse.Id> join(@Valid @RequestBody UserRequest.Join request) {

        UserResponse.Id newId = userService.join(request.loginId(), request.password());
        return ResponseEntity.status(CREATED).body(newId);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse.MyProfile> getMyProfile(@AuthenticatedId Long userId) {
        return ResponseEntity.ok().body(userService.getMyProfile(userId));
    }
}
