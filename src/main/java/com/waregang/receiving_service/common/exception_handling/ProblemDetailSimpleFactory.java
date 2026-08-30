package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.common.exception_handling.error_code.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemDetailSimpleFactory {

    private final MessageSource messageSource;

    private ProblemDetail baseProblemDetail(
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail pd = ProblemDetail
                .forStatusAndDetail(status, detail);

        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    public ProblemDetail create(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();
        String message = messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());

        if (errorCode.getHttpStatus() == HttpStatus.BAD_REQUEST && !ex.getErrorContext().isEmpty()) {
            Map<String, String> errors = ex.getErrorContext().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> String.valueOf(e.getValue())
                    ));

            ProblemDetail pd = buildValidationPD(errors);

            return pd;
        }

        ProblemDetail pd = baseProblemDetail(
                errorCode.getHttpStatus(),
                message,
                message
        );

        pd.setProperty("error_code", code);

        if (!ex.getErrorContext().isEmpty() && pd.getProperties() != null) {
            pd.getProperties().putAll(ex.getErrorContext());
        }

        return pd;
    }

    public ProblemDetail create(AccessDeniedException accessDeniedException) {
        return baseProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access denied",
                accessDeniedException.getMessage()
        );
    }

    public ProblemDetail create(AuthenticationException authenticationException) {
        return baseProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                authenticationException.getMessage()
        );
    }

    public ProblemDetail create(ObjectOptimisticLockingFailureException optimisticLockException) {
        return baseProblemDetail(
                HttpStatus.CONFLICT,
                "Smth wrong: try again :(",
                optimisticLockException.getMessage()
        );
    }

    public ProblemDetail create(DataIntegrityViolationException ex) {
        return baseProblemDetail(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                "Conflict occurred: " + ex.getMostSpecificCause().getMessage()
        );
    }

    /**
     * MethodArgumentTypeMismatchException fires both for genuinely bad client input
     * (bad UUID, bad enum, bad date string) and for converter-side bugs. Only the
     * former is a 400 - anything else is a real server error.
     */
//    public ProblemDetail create(MethodArgumentTypeMismatchException ex) {
//        Throwable cause = ex.getMostSpecificCause();
//
//        if (!isSimpleConversionFailure(cause)) {
//            log.error("Unexpected failure converting parameter '{}' with value '{}'",
//                    ex.getName(), safeValue(ex), ex);
//            return baseProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", ":(");
//        }
//
//        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type";
//        String message = "Invalid value '%s' for parameter '%s', expected %s"
//                .formatted(safeValue(ex), ex.getName(), requiredType);
//
//        return buildValidationPD(Map.of(ex.getName(), message));
//    }
//
//    private boolean isSimpleConversionFailure(Throwable cause) {
//        return cause instanceof IllegalArgumentException
//                || cause instanceof NumberFormatException
//                || cause instanceof DateTimeParseException;
//    }
//
//    private String safeValue(MethodArgumentTypeMismatchException ex) {
//        Object value = ex.getValue();
//        return value != null ? value.toString() : "null";
//    }

    public ProblemDetail create(Exception e) {
        return baseProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", ":(");
    }

    private ProblemDetail buildValidationPD(Map<String, String> errors) {
        ProblemDetail pd = baseProblemDetail(HttpStatus.BAD_REQUEST, "Bad request", "Validation Error");
        pd.setProperty("invalid_params", errors);

        return pd;
    }

    private Map<String, String> extractErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value", this::mergeMessages));
    }

    // Динамически очищаем путь от префиксов методов (например, getAsnsWithFilters.fromDate -> fromDate)
    private Map<String, String> extractErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        ConstraintViolation::getMessage,
                        this::mergeMessages
                ));
    }

    private String mergeMessages(String existing, String replacement) {
        return existing + " | " + replacement;
    }

    public ProblemDetail create(MethodArgumentNotValidException ex) {
        return buildValidationPD(extractErrors(ex));
    }

    public ProblemDetail create(ConstraintViolationException ex) {
        return buildValidationPD(extractErrors(ex));
    }

    public ProblemDetail create(PropertyReferenceException ex) {
        return buildValidationPD(Map.of(
                ex.getPropertyName(), "Unknown sort property '%s'".formatted(ex.getPropertyName())
        ));
    }
}
