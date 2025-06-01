package com.brokerage.api.service;

import com.brokerage.api.model.Asset;
import com.brokerage.api.model.Order;
import com.brokerage.api.model.dto.AssetDTO;
import com.brokerage.api.model.dto.OrderDTO;
import com.brokerage.api.repository.AssetRepository;
import com.brokerage.api.service.base.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final ObjectMapper objectMapper;


    @Override
    public List<AssetDTO> getAssetsForCustomer(String customerId) {
        List<Asset> assets = assetRepository.findByCustomerId(customerId);

        return assets.stream()
                .map(asset -> objectMapper.convertValue(asset, AssetDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AssetDTO> getAsset(String customerId, String assetName) {
        Optional<Asset> asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
        return asset.map(a -> objectMapper.convertValue(a, AssetDTO.class));
    }

    @Transactional
    @Override
    public AssetDTO save(AssetDTO assetDto) {
        Asset entity = objectMapper.convertValue(assetDto,Asset.class);
        assetRepository.save(entity);
        return assetDto;
    }
}
