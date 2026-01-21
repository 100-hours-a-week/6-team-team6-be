package ktb.billage.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    public record Login(
            @NotBlank String loginId,
            @NotBlank String password
    ) {
    }
}
