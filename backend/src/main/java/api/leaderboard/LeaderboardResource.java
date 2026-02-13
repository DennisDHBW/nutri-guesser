package api.leaderboard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import model.LeaderboardEntry;
import repository.LeaderboardRepository;

import java.util.List;

@Path("/api/leaderboard")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeaderboardResource {

    @Inject
    LeaderboardRepository leaderboardRepository;

    @GET
    public List<LeaderboardEntry> getLeaderboard(@QueryParam("limit") @DefaultValue("100") int limit) {
        return leaderboardRepository.findTopEntries(limit);
    }
}

