package api.result;
import service.result.ResultService;
import api.result.dto.ResultResponseDTO;
import client.cataas.dto.CataasResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/result")
public class ResultResource {

    @Inject
    ResultService resultService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResultResponseDTO getResultResponse(
            @QueryParam("sessionId") UUID sessionId) {
        return resultService.fetchResultResponseForSession(sessionId);
    }

    @GET
    @Path("/cat")
    @Produces(MediaType.APPLICATION_JSON)
    public CataasResponse getCat(
            @QueryParam("sessionId") UUID sessionId) {
        return resultService.fetchCatJsonForSession(sessionId);
    }

    @GET
    @Path("/image")
    @Produces("image/*")
    public Response getCatImage(
            @QueryParam("sessionId") UUID sessionId) {

        byte[] data;
        CataasResponse dto;

        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }
        dto = resultService.fetchCatJsonForSession(sessionId);
        data = resultService.fetchCatImageForSession(sessionId);

        String type = (dto != null && dto.mimetype() != null && dto.mimetype().startsWith("image/")) ? dto.mimetype() : "image/jpeg";
        return Response.ok(data).type(type).build();
    }
}
