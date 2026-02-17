package api.result.mapper;

import api.result.dto.CatResponseDTO;
import api.result.dto.ResultResponseDTO;
import client.cataas.dto.CataasResponse;
import service.result.ResultSummary;

public final class ResultMapper {

    private ResultMapper() {}

    public static ResultResponseDTO toResultResponse(ResultSummary summary) {
        return new ResultResponseDTO(
                summary.id(),
                summary.tags(),
                summary.createdAt(),
                summary.url(),
                summary.mimetype(),
                summary.rank(),
                summary.totalScore(),
                summary.betterThanPercentage()
        );
    }

    public static CatResponseDTO toCatResponse(CataasResponse dto) {
        return new CatResponseDTO(
                dto.id(),
                dto.tags(),
                dto.createdAt(),
                dto.url(),
                dto.mimetype()
        );
    }
}
