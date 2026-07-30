package com.waregang.receiving_service.advanced_shipping_notice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArrivalTimeline {

    @Column(name = "expected_arrival_date")
    private LocalDateTime expected;

    @Column(name = "actual_arrival_date")
    private LocalDateTime actual;

    public static ArrivalTimeline of(LocalDateTime expected) {
        return new ArrivalTimeline(expected, null);
    }

    public ArrivalTimeline withActual(LocalDateTime actual) {
        return new ArrivalTimeline(this.expected, actual);
    }
}
