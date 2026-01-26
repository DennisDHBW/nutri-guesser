package service;

import client.CataasClient;
import dto.CataasResponseDTO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CataasService {

    @Inject
    @RestClient
    CataasClient cataasClient;

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.cataas-api.url")
    String cataasBaseUrl;

    private static final Logger LOG = Logger.getLogger(CataasService.class.getName());
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 250L;
    private static final String FALLBACK_IMAGE_CLASSPATH = "/static/default-cat.jpg";

    public CataasResponseDTO getByTag(String tag) {
        return fetchCatJson(tag);
    }

    // Robust JSON fetch with simple retry and fallback
    public CataasResponseDTO fetchCatJson(String tag) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                CataasResponseDTO dto = cataasClient.getCatByTag(tag, true);
                if (dto == null) throw new RuntimeException("Cataas returned null for tag: " + tag);
                return normalize(dto);
            } catch (Exception e) {
                String msg = "Failed to fetch JSON from Cataas (attempt " + attempt + ") for tag '" + tag + "': " + e.getMessage();
                LOG.warning(msg);
                if (attempt == MAX_RETRIES) {
                    return fallbackCatJson(tag, e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return fallbackCatJson(tag, new RuntimeException("unreachable"));
    }

    // Fetch image bytes for a tag. Falls back to default image when remote fetch fails.
    public byte[] fetchCatImage(String tag) {
        CataasResponseDTO dto = fetchCatJson(tag);
        String urlPart = dto.url();
        String fullUrl = buildFullUrl(urlPart);
        // If DTO provided no URL, try to call image endpoint directly
        if (fullUrl == null || fullUrl.isEmpty()) {
            fullUrl = buildFullUrl("/cat/" + tag);
        }

        try (InputStream in = new URL(fullUrl).openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (Exception e) {
            String msg = "Failed to fetch image from '" + fullUrl + "': " + e.getMessage();
            LOG.warning(msg);
            return loadDefaultImageBytes();
        }
    }

    private String buildFullUrl(String urlPart) {
        if (urlPart == null) return null;
        if (urlPart.startsWith("http://") || urlPart.startsWith("https://")) return urlPart;
        String base = cataasBaseUrl != null ? cataasBaseUrl.replaceAll("/$", "") : "https://cataas.com";
        if (!urlPart.startsWith("/")) urlPart = "/" + urlPart;
        return base + urlPart;
    }

    // Normalize DTO (lowercase tags etc.) — erzeugt neues DTO (record ist immutable)
    private CataasResponseDTO normalize(CataasResponseDTO dto) {
        List<String> normalizedTags = dto.tags() == null ? List.of() : dto.tags().stream().map(t -> t == null ? "" : t.toLowerCase()).toList();
        String createdAt = dto.createdAt() == null ? Instant.now().toString() : dto.createdAt();
        return new CataasResponseDTO(dto.id(), normalizedTags, createdAt, dto.url(), dto.mimetype());
    }

    // Simple fallback DTO when remote calls fail
    public CataasResponseDTO fallbackCatJson(String tag, Throwable t) {
        String msg = "Using fallback DTO for tag '" + tag + "' because: " + (t == null ? "<unknown>" : t.getMessage());
        LOG.warning(msg);
        return new CataasResponseDTO(
                "fallback",
                List.of("fallback"),
                Instant.now().toString(),
                FALLBACK_IMAGE_CLASSPATH,
                "image/jpeg"
        );
    }

    // Load default image bytes from classpath
    private byte[] loadDefaultImageBytes() {
        try (InputStream in = getClass().getResourceAsStream(FALLBACK_IMAGE_CLASSPATH); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) {
                LOG.warning("Default cat image not found on classpath: " + FALLBACK_IMAGE_CLASSPATH);
                return new byte[0];
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (Exception e) {
            LOG.warning("Failed to load default image: " + e.getMessage());
            return new byte[0];
        }
    }

}