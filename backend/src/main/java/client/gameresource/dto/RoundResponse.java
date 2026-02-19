package client.gameresource.dto;

import java.util.UUID;

public record RoundResponse(
        UUID roundId,
        String barcode,
        String imageUrl,
        String name
) {



}
