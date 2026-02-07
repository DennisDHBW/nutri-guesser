package api.common;

import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RateLimitExceptionMapper implements ExceptionMapper<RateLimitException> {

    @Override
    public Response toResponse(RateLimitException e) {
        return Response.status(429)
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\":\"Too many requests\",\"status\":429}")
                .build();
    }
}
