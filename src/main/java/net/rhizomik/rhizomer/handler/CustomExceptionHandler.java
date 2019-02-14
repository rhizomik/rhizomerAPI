package net.rhizomik.rhizomer.handler;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

/**
 * Created by http://rhizomik.net/~roberto/
 */

@ControllerAdvice
public class CustomExceptionHandler {
    final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ErrorInfo handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        logger.info("Generating HTTP BAD REQUEST from ConstraintViolationException: {}", e.toString());
        return new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizationServiceException.class)
    @ResponseBody
    public ErrorInfo handleAuthorizationException(HttpServletRequest request, AuthorizationServiceException e) {
        logger.info("Generating HTTP BAD REQUEST from AuthorizationServiceException: {}", e.toString());
        return new ErrorInfo(HttpStatus.UNAUTHORIZED, request, e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public ErrorInfo handleNotFound(HttpServletRequest request, NullPointerException e) {
        logger.info("Generating HTTP NOT FOUND from NullPointerException: {}", e.toString());
        return new ErrorInfo(HttpStatus.NOT_FOUND, request, e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ErrorInfo handleNotFound(HttpServletRequest request, ResourceNotFoundException e) {
        logger.info("Generating HTTP NOT FOUND from NullPointerException: {}", e.toString());
        return new ErrorInfo(HttpStatus.NOT_FOUND, request, e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorInfo handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        logger.info("Generating HTTP BAD REQUEST from MethodArgumentNotValidException: {}", e.toString());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request, e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ErrorInfo handleInvalidArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        logger.info("Generating HTTP BAD REQUEST from IllegalArgumentException: {}", e.toString());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request, e);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    public ErrorInfo handleIllegalStateException(HttpServletRequest request, IllegalStateException e) {
        logger.info("Generating HTTP CONFLIC from IllegalStateException: {}", e.toString());
        return new ErrorInfo(HttpStatus.CONFLICT, request, e);
    }

    @ExceptionHandler(TransactionSystemException.class)
    @ResponseBody
    public ErrorInfo handleTxException(HttpServletRequest request, HttpServletResponse response, TransactionSystemException e) {
        logger.info("Handling transaction exception: {}", e.toString());
        if(e.getCause() instanceof RollbackException && e.getCause().getCause() instanceof ConstraintViolationException) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, (ConstraintViolationException) e.getCause().getCause());
        }
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request, (Exception) e.getCause().getCause());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {
        logger.info("Handling generic Exception: {}", e.toString());
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request, e);
    }

    public class ErrorInfo {
        private int status;
        private String operation;
        private String error;
        private String message;

        public ErrorInfo(HttpStatus status, HttpServletRequest request, Exception e) {
            String requestContent = "";
            try { requestContent = IOUtils.toString(request.getReader());
            } catch (Exception ioe) { logger.error(ioe.getMessage()); }
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.operation = request.getMethod() + " " + request.getRequestURI() + " " + requestContent;
            this.message = e.getMessage();
        }

        public int getStatus() { return status; }
        public String getOperation() { return operation; }
        public String getError() { return error; }
        public String getMessage() { return message; }
    }
}
