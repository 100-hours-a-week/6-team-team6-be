package ktb.billage.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import ktb.billage.domain.auth.dto.AuthRequest;
import ktb.billage.domain.auth.service.AuthService;
import ktb.billage.domain.token.Tokens;
import ktb.billage.web.common.annotation.AuthenticatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest.Login request, HttpServletResponse response) {
        Tokens tokens = authService.login(request.loginId(), request.password());

        setCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(Map.of(
                "accessToken", tokens.getAccessToken(),
                "userId", tokens.getUserId()
        ));
    }

    @PostMapping("/tokens")
    public ResponseEntity<?> reissue(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        Tokens tokens = authService.reissue(refreshToken);

        setCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(Map.of(
                "accessToken", tokens.getAccessToken()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticatedId Long userId,
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ResponseEntity.noContent().build();
        }

        authService.logout(userId, refreshToken);
        return ResponseEntity.noContent().build();
    }

    private void setCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/auth")
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
