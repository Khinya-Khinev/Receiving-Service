package com.waregang.receiving_service.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_ATTRIBUTE = "idempotencyKey";
    private final IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            return true;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.error("Idempotency key header is missing for POST request");
            throw new MissingIdempotencyHeaderException("Idempotency key header is missing.");
        }

        Optional<String> cachedResponse = idempotencyService.getResponse(idempotencyKey);

        if (cachedResponse.isPresent()) {
            String responseBody = cachedResponse.get();
            if (idempotencyService.isLocked(responseBody)) {
                log.error("A request is already being processed with idempotency key: {}", idempotencyKey);
                throw new IdempotencyKeyConflictException("A request with this idempotency key is already being processed.");
            } else {
                log.info("Returning cached response for idempotency key: {}", idempotencyKey);
                response.getWriter().write(responseBody);
                response.setStatus(HttpServletResponse.SC_OK); // Or whatever status was originally returned
                return false;
            }
        }

        if (!idempotencyService.tryLock(idempotencyKey)) {
            // This case should ideally not be hit if the above logic is correct, but as a safeguard:
            log.error("A request is already being processed with idempotency key: {}", idempotencyKey);
            throw new IdempotencyKeyConflictException("A request with this idempotency key is already being processed.");
        }

        request.setAttribute(IDEMPOTENCY_KEY_ATTRIBUTE, idempotencyKey);
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) throws Exception {
        String idempotencyKey = (String) request.getAttribute(IDEMPOTENCY_KEY_ATTRIBUTE);

        if (idempotencyKey != null && ex == null && response instanceof ContentCachingResponseWrapper) {
            final ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) response;
            String responseBody = new String(cachingResponse.getContentAsByteArray(), response.getCharacterEncoding());

            if (!responseBody.isEmpty()) {
                idempotencyService.storeResponse(idempotencyKey, responseBody);
                log.info("Cached response for idempotency key: {}", idempotencyKey);
            }
        }
    }
}