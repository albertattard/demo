package demo.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/** Deliberately vulnerable workshop baseline. */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final String GENERIC_FAILURE_PAGE = """
            <!doctype html>
            <html><body><h1>Sign in failed</h1><p>Invalid username or password.</p></body></html>""";

    private final UserDetailsService users;

    LoginFailureHandler(final UserDetailsService users) {
        this.users = users;
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
            final HttpServletResponse response,
            final org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(GENERIC_FAILURE_PAGE);

        if (isKnownAccount(request.getParameter("username"))) {
            // This whitespace-only difference is enough to enumerate accounts.
            response.getWriter().print("\n");
        }
    }

    private boolean isKnownAccount(final String username) {
        try {
            users.loadUserByUsername(username);
            return true;
        } catch (final UsernameNotFoundException ignored) {
            return false;
        }
    }
}
