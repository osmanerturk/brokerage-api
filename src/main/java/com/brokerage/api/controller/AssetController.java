package com.brokerage.api.controller;

import com.brokerage.api.model.Asset;
import com.brokerage.api.model.dto.AssetDTO;
import com.brokerage.api.service.base.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@customerSecurity.isCustomerSelf(#customerId, authentication) or hasRole('ADMIN')")
    public ResponseEntity<List<AssetDTO>> getAssetsByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(assetService.getAssetsForCustomer(customerId));
    }


    @GetMapping("/customer/{customerId}/asset/{assetName}")
    @PreAuthorize("@customerSecurity.isCustomerSelf(#customerId, authentication) or hasRole('ADMIN')")
    public ResponseEntity<AssetDTO> getAsset(
            @PathVariable String customerId,
            @PathVariable String assetName
    ) {
        Optional<AssetDTO> asset = assetService.getAsset(customerId, assetName);
        return asset.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

