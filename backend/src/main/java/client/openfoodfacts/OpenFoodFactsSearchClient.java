package client.openfoodfacts;

import client.openfoodfacts.dto.SearchResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/cgi/search.pl")
@RegisterRestClient(configKey = "openfoodfacts-search")
public interface OpenFoodFactsSearchClient {

    @GET
    SearchResponse search(
            @QueryParam("search_terms") String searchTerms,
            @QueryParam("search_simple") int searchSimple,
            @QueryParam("json") int json,
            @QueryParam("page_size") int pageSize,
            @QueryParam("page") int page
    );
}
