package api.result;

import service.result.dto.ResultResponseDTO;
import service.result.ResultService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/result")
public class ResultResource {

    private final ResultService resultService;

    @Inject
    public ResultResource(ResultService resultService) {
        this.resultService = resultService;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResultResponse(
            @QueryParam("sessionId") UUID sessionId) {
        if (sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"sessionId query parameter is required\"}")
                    .build();
        }
        try {
            ResultResponseDTO result = resultService.fetchResultResponseForSession(sessionId);
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
