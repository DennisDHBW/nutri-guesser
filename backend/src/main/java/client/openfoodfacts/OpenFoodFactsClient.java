package client.openfoodfacts;

import client.openfoodfacts.dto.ProductResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/product/{barcode}.json")
@RegisterRestClient(configKey = "openfoodfacts-product")
public interface OpenFoodFactsClient {
    @GET
    ProductResponse fetchProductByCode(@PathParam("barcode") String barcode);
}
