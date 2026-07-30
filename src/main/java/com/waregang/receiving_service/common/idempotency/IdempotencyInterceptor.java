package com.waregang.receiving_service.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private final IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        boolean hasKey = idempotencyKey != null && !idempotencyKey.isBlank();

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method))
            return true;

        if ("POST".equalsIgnoreCase(method) && !hasKey) {
            log.error("Idempotency key header is missing for POST request");
            throw new MissingIdempotencyHeaderException("Idempotency key header is missing.");
        }

        if (hasKey) {
            if (!idempotencyService.tryLock(idempotencyKey)) {
                log.error("A request is already being processed with idempotency key: {}", idempotencyKey);
                throw new IdempotencyKeyConflictException("A request with this idempotency key is already being processed.");
            }
        }

        return true;
    }
}