package com.brokerage.api.model.dto;

import lombok.Data;

@Data
public class AssetDTO {
    private Long id;
    private String customerId;
    private String assetName;
    private Double size;
    private Double usableSize;
}
