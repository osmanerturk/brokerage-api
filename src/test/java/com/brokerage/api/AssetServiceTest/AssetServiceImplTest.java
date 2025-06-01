package com.brokerage.api.AssetServiceTest;

import com.brokerage.api.model.Asset;
import com.brokerage.api.model.dto.AssetDTO;
import com.brokerage.api.repository.AssetRepository;
import com.brokerage.api.service.AssetServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceImplTest {

    private AssetRepository assetRepository;
    private AssetServiceImpl assetService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        objectMapper = new ObjectMapper();
        assetService = new AssetServiceImpl(assetRepository, objectMapper);
    }


    @Test
    void getAssetsForCustomer_returnsListOfAssets() {
        String customerId = "customer_1";

        Asset asset1 = new Asset();
        asset1.setCustomerId(customerId);
        asset1.setAssetName("TRY");
        asset1.setSize(100.0);
        asset1.setUsableSize(100.0);

        Asset asset2 = new Asset();
        asset2.setCustomerId(customerId);
        asset2.setAssetName("USD");
        asset2.setSize(50.0);
        asset2.setUsableSize(50.0);

        List<Asset> mockAssets = Arrays.asList(asset1, asset2);

        when(assetRepository.findByCustomerId(customerId)).thenReturn(mockAssets);

        List<AssetDTO> assets = assetService.getAssetsForCustomer(customerId);

        assertEquals(2, assets.size());
        assertEquals("TRY", assets.get(0).getAssetName());
        assertEquals("USD", assets.get(1).getAssetName());

        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void getAsset_existingAsset_returnsAsset() {
        String customerId = "customer_2";
        String assetName = "TRY";

        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setSize(150.0);
        asset.setUsableSize(150.0);

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));

        Optional<AssetDTO> result = assetService.getAsset(customerId, assetName);

        assertTrue(result.isPresent());
        assertEquals(assetName, result.get().getAssetName());
        assertEquals(customerId, result.get().getCustomerId());

        verify(assetRepository, times(1)).findByCustomerIdAndAssetName(customerId, assetName);
    }

    @Test
    void getAsset_nonExistingAsset_returnsEmpty() {
        String customerId = "customer_3";
        String assetName = "EUR";

        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.empty());

        Optional<AssetDTO> result = assetService.getAsset(customerId, assetName);

        assertFalse(result.isPresent());

        verify(assetRepository, times(1)).findByCustomerIdAndAssetName(customerId, assetName);
    }

    @Test
    void saveAsset_returnsSavedAsset() {
        AssetDTO assetToSaveDto = new AssetDTO();
        assetToSaveDto.setCustomerId("customer_4");
        assetToSaveDto.setAssetName("TRY");
        assetToSaveDto.setSize(200.0);
        assetToSaveDto.setUsableSize(200.0);

        Asset assetToSave = objectMapper.convertValue(assetToSaveDto, Asset.class);

        when(assetRepository.save(any(Asset.class))).thenReturn(assetToSave);

        AssetDTO savedAsset = assetService.save(assetToSaveDto);

        assertNotNull(savedAsset);
        assertEquals(assetToSaveDto.getCustomerId(), savedAsset.getCustomerId());
        assertEquals(assetToSaveDto.getAssetName(), savedAsset.getAssetName());

        verify(assetRepository, times(1)).save(any(Asset.class));
    }

}
