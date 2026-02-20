package client.gameresource.dto;

import java.util.UUID;

public record StartGameResponse(
        UUID sessionId,
        UUID roundId,
        String barcode,
        String imageUrl,
        String name
) {
}
