package com.h4j4x.expenses.api.error;

import io.quarkus.security.AuthenticationFailedException;
import java.util.Map;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {
    @ServerExceptionMapper
    public RestResponse<Map<String, Object>> mapAuthenticationFailedException(AuthenticationFailedException e) {
        return RestResponse.status(Response.Status.UNAUTHORIZED, response(e));
    }

    @ServerExceptionMapper
    public RestResponse<Map<String, Object>> mapBadRequestException(BadRequestException e) {
        return RestResponse.status(Response.Status.BAD_REQUEST, response(e));
    }

    @ServerExceptionMapper
    public RestResponse<Map<String, Object>> mapNotFoundException(NotFoundException e) {
        return RestResponse.status(Response.Status.NOT_FOUND, response(e));
    }

    private Map<String, Object> response(Exception e) {
        return Map.of(
            "message", e.getMessage()
        );
    }
}
