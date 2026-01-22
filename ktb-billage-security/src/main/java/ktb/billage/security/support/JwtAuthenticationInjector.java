package ktb.billage.security.support;

import ktb.billage.contract.auth.TokenParser;
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
        String userId = tokenParser.parseId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
