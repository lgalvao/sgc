package sgc.e2e;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class E2eTestRequestInterceptor implements HandlerInterceptor {

    public static final String TEST_ID_HEADER = "X-Test-ID";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String testId = request.getHeader(TEST_ID_HEADER);
        if (testId != null && !testId.isBlank()) {
            E2eTestContext.setCurrentTestId(testId);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        E2eTestContext.clear();
    }
}
