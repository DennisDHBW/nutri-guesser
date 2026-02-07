package client;

import client.dto.CataasResponseDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("/cat")
@RegisterRestClient(configKey = "cataas-api")
public interface CataasClient {

    @GET
    @Path("/{tag}")
    CataasResponseDTO getCatByTag(
            @PathParam("tag") String tag,
            @QueryParam("json") boolean json
    );
}