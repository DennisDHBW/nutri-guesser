package api;

import service.CataasService;
import dto.CataasResponseDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
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
    public CataasResponseDTO getCatJson(@QueryParam("tag") @DefaultValue("cute") String tag) {
        return cataasService.fetchCatJson(tag);
    }

    @GET
    @Path("/image")
    @Produces("image/*")
    public Response getCatImage(@QueryParam("tag") @DefaultValue("cute") String tag) {
        CataasResponseDTO dto = cataasService.fetchCatJson(tag);
        byte[] data = cataasService.fetchCatImage(tag);
        String type = (dto != null && dto.mimetype() != null && dto.mimetype().startsWith("image/")) ? dto.mimetype() : "image/jpeg";
        return Response.ok(data).type(type).build();
    }
}
