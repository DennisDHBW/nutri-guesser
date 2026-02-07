package api;
import service.CataasService;
import client.dto.CataasResponseDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/cat")
public class CataasResource {

    @Inject
    CataasService cataasService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CataasResponseDTO getCat(
            @QueryParam("tag") String tag,
            @QueryParam("score") Integer score,
            @QueryParam("text") String text) {

        // always use text if provided
        if (text != null && !text.isEmpty()) {
            String effectiveTag = "cute";
            if (score != null) {
                effectiveTag = cataasService.fetchCatJsonScoreBased(score).tags().getFirst();
            } else if (tag != null && !tag.isEmpty()) {
                effectiveTag = tag;
            }
            return cataasService.fetchCatJsonWithText(effectiveTag, text);
        }

        // score > tag > default
        if (score != null) {
            return cataasService.fetchCatJsonScoreBased(score);
        } else if (tag != null && !tag.isEmpty()) {
            return cataasService.fetchCatJson(tag);
        } else {
            return cataasService.fetchCatJson("cute");
        }
    }

    @GET
    @Path("/image")
    @Produces("image/*")
    public Response getCatImage(
            @QueryParam("tag") String tag,
            @QueryParam("score") Integer score,
            @QueryParam("text") String text) {

        String effectiveTag = "cute";
        if (score != null) {
            CataasResponseDTO dto = cataasService.fetchCatJsonScoreBased(score);
            if (dto != null && dto.tags() != null && !dto.tags().isEmpty()) {
                effectiveTag = dto.tags().getFirst();
            }
        } else if (tag != null && !tag.isEmpty()) {
            effectiveTag = tag;
        }

        // use text if provided
        byte[] data;
        CataasResponseDTO dto;
        if (text != null && !text.isEmpty()) {
            dto = cataasService.fetchCatJsonWithText(effectiveTag, text);
            data = cataasService.fetchCatImageWithText(effectiveTag, text);
        } else {
            dto = cataasService.fetchCatJson(effectiveTag);
            data = cataasService.fetchCatImage(effectiveTag);
        }

        String type = (dto != null && dto.mimetype() != null && dto.mimetype().startsWith("image/")) ? dto.mimetype() : "image/jpeg";
        return Response.ok(data).type(type).build();
    }
}
