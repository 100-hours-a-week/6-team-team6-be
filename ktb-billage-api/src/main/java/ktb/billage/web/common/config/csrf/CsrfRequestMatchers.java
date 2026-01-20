package ktb.billage.web.common.config.csrf;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class CsrfRequestMatchers {

    public RequestMatcher csrfProtectionMatcher() {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        return new OrRequestMatcher(
                matcherBuilder.matcher(HttpMethod.POST, "/auth/tokens"),
                matcherBuilder.matcher(HttpMethod.POST, "/auth/logout")
        );
    }

    public RequestMatcher csrfTokenIssueMatcher() {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        return new OrRequestMatcher(
                matcherBuilder.matcher(HttpMethod.POST, "/auth/login"),
                matcherBuilder.matcher(HttpMethod.POST, "/auth/tokens")
        );
    }
}
