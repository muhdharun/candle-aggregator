package com.gsr.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Candle {
    private Date timestamp;
    private Double open;
    private double close;
    private double high;
    private double low;

    public static Candle setupCandle() {
        return Candle.builder()
                .open(null)
                .high(Double.MIN_VALUE)
                .low(Double.MAX_VALUE)
                .build();
    }

}
