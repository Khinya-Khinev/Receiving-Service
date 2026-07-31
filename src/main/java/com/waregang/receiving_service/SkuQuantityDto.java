package com.waregang.receiving_service;

public record SkuQuantityDto(
        String sku,
        Long quantity
) {
}