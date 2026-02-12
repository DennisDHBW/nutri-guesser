package api.admin;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Path("/api/admin/testdata")
@IfBuildProfile("dev")
public class TestDataResource {

    private static final List<String> DEFAULT_FILES = List.of(
            "test-data/01-products.sql",
            "test-data/02-players.sql",
            "test-data/03-game-sessions.sql",
            "test-data/04-rounds.sql",
            "test-data/05-leaderboard.sql"
    );

    @Inject
    DataSource dataSource;

    @POST
    @Path("/load")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadTestData(
            @QueryParam("files") String files,
            @QueryParam("clear") @DefaultValue("false") boolean clear,
            @QueryParam("loadTestData") @DefaultValue("true") boolean loadTestData
    ) {
        List<String> resources = resolveResources(files);
        int executed = 0;

        try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                if (clear) {
                    clearTables(connection);
                }

                if (loadTestData) {
                    for (String resource : resources) {
                        executed += executeSqlResource(connection, resource);
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new LoadResult(resources, executed, clear, e.getMessage()))
                        .build();
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new LoadResult(resources, executed, clear, e.getMessage()))
                    .build();
        }

        return Response.ok(new LoadResult(resources, executed, clear, null)).build();
    }

    @GET
    @Path("/load")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadTestDataGet(
            @QueryParam("files") String files,
            @QueryParam("clear") @DefaultValue("false") boolean clear,
            @QueryParam("loadTestData") @DefaultValue("true") boolean loadTestData
    ) {
        return loadTestData(files, clear, loadTestData);
    }

    private List<String> resolveResources(String files) {
        if (files == null || files.trim().isEmpty()) {
            return DEFAULT_FILES;
        }

        List<String> resources = new ArrayList<>();
        for (String token : files.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!trimmed.contains("/")) {
                String normalized = trimmed.toLowerCase(Locale.ROOT);
                if (!normalized.endsWith(".sql")) {
                    normalized = normalized + ".sql";
                }
                resources.add("test-data/" + normalized);
            } else {
                resources.add(trimmed);
            }
        }
        return resources;
    }

    private int executeSqlResource(Connection connection, String resourcePath) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("SQL resource not found: " + resourcePath);
        }

        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            sql = reader.lines()
                    .filter(line -> {
                        String trimmed = line.trim();
                        return !trimmed.isEmpty() && !trimmed.startsWith("--");
                    })
                    .collect(Collectors.joining("\n"));
        }

        List<String> statements = splitStatements(sql);
        int executed = 0;
        try (Statement stmt = connection.createStatement()) {
            for (String statement : statements) {
                if (statement.trim().isEmpty()) {
                    continue;
                }
                stmt.execute(statement);
                executed++;
            }
        }
        return executed;
    }

    private List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char c : sql.toCharArray()) {
            if (c == ';') {
                statements.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            statements.add(current.toString().trim());
        }
        return statements;
    }

    private void clearTables(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM LEADERBOARD_ENTRY");
            stmt.execute("DELETE FROM ROUND");
            stmt.execute("DELETE FROM GAME_SESSION");
            stmt.execute("DELETE FROM NUTRITION_FACTS");
            stmt.execute("DELETE FROM PRODUCT");
            stmt.execute("DELETE FROM PLAYER");
        }
    }

    public record LoadResult(List<String> resources, int statementsExecuted, boolean cleared, String error) {
    }
}
