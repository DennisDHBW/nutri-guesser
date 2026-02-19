package client.openfoodfacts.dto;

import org.apache.commons.lang3.ObjectUtils;

public record SelectedImageItem(
        String en,
        String fr,
        String pl
) {
    public String getUrl() {
        return ObjectUtils.firstNonNull(en, fr, pl);
    }
}