package api.result;

import api.result.dto.CatImageDTO;
import api.result.dto.CatResponseDTO;
import api.result.dto.ResultResponseDTO;
import api.result.mapper.ResultMapper;
import service.result.ResultService;
import service.result.ResultSummary;
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

    private final ResultService resultService;

    @Inject
    public ResultResource(ResultService resultService) {
        this.resultService = resultService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResultResponseDTO getResultResponse(
            @QueryParam("sessionId") UUID sessionId) {
        ResultSummary summary = resultService.fetchResultResponseForSession(sessionId);
        return ResultMapper.toResultResponse(summary);
    }

    @GET
    @Path("/cat")
    @Produces(MediaType.APPLICATION_JSON)
    public CatResponseDTO getCat(
            @QueryParam("sessionId") UUID sessionId) {
        var dto = resultService.fetchCatJsonForSession(sessionId);
        return ResultMapper.toCatResponse(dto);
    }

    @GET
    @Path("/image")
    @Produces("image/*")
    public Response getCatImage(
            @QueryParam("sessionId") UUID sessionId) {

        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }

        CatImageDTO imageResult = resultService.fetchCatImageForSession(sessionId);
        return Response.ok(imageResult.data()).type(imageResult.mimeType()).build();
    }
}
