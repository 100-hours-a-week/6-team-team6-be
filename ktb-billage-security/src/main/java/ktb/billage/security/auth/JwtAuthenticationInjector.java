package ktb.billage.security.auth;

import ktb.billage.contract.auth.TokenParser;
import ktb.billage.common.exception.BaseException;
import ktb.billage.security.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationInjector {
    private final TokenParser tokenParser;
    private final UserDetailsService userDetailsService;

    public void setAuthentication(String token) {
        String userId;
        UserDetails userDetails;
        try {
            userId = tokenParser.parseId(token);
            userDetails = userDetailsService.loadUserByUsername(userId);
        } catch (BaseException ex) {
            throw new JwtAuthenticationException(ex.getCode());
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
