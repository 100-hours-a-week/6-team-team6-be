package ktb.billage.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import ktb.billage.domain.auth.dto.AuthRequest;
import ktb.billage.domain.auth.service.AuthService;
import ktb.billage.domain.token.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest.Login request, HttpServletResponse response) {
        Tokens tokens = authService.login(request.loginId(), request.password());

        setCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(Map.of(
                "accessToken", tokens.getAccessToken(),
                "userId", tokens.getUserId()
        ));
    }

    private void setCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/auth")
                .sameSite("None")
                .build(); // TODO. https 적용 후 secure 설정 추가

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
