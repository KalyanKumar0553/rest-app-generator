package com.src.main.cdn.dto;

import java.util.List;

public record CdnImageUploadResponseDTO(List<CdnImageDraftResponseDTO> drafts) {
}
