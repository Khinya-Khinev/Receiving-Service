package com.waregang.receiving_service.common;

public record SkuQuantityDto(
        String sku,
        Long quantity
) {
}