package com.brokerage.api.service.base;

import com.brokerage.api.model.Asset;
import com.brokerage.api.model.dto.AssetDTO;

import java.util.List;
import java.util.Optional;

public interface AssetService {
    List<AssetDTO> getAssetsForCustomer(String customerId);

    Optional<AssetDTO> getAsset(String customerId, String assetName);

    AssetDTO save(AssetDTO asset);
}
