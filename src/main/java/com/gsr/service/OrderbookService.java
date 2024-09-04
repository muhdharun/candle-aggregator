package com.gsr.service;

import com.gsr.model.Candle;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.gsr.constants.DataConstants.PRICE;
import static com.gsr.constants.DataConstants.QTY;

@Service
@Slf4j
public class OrderbookService {

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private NavigableMap<Double, Double> bids = new TreeMap<>();
    private NavigableMap<Double, Double> asks = new TreeMap<>();

    private Candle candle = Candle.setupCandle();
    private int ticks = 0;

    public void updateOrderbook(List<JSONArray> bidsAndAsks) {
        JSONArray bidsArray = bidsAndAsks.get(0);
        JSONArray asksArray = bidsAndAsks.get(1);
        doSanityCheckBeforeUpdating(bidsArray, asksArray);
        updateCandle();
    }

    public void updateTicks() {
        ticks++;
    }

    private void doSanityCheckBeforeUpdating(JSONArray bidsArray, JSONArray asksArray) {

        NavigableMap<Double, Double> tempBids = new TreeMap<>();
        NavigableMap<Double, Double> tempAsks = new TreeMap<>();

        for (int i = 0; i < bidsArray.length(); i++) {
            JSONObject bid = bidsArray.getJSONObject(i);
            double price = bid.getDouble(PRICE);
            double qty = bid.getDouble(QTY);
            tempBids.put(price, qty);
        }

        for (int i = 0; i < asksArray.length(); i++) {
            JSONObject ask = asksArray.getJSONObject(i);
            double price = ask.getDouble(PRICE);
            double qty = ask.getDouble(QTY);
            tempAsks.put(price, qty);
        }

        double highestBid = tempBids.lastKey();
        double lowestAsk = tempAsks.firstKey();
        if (highestBid < lowestAsk) {
            bids.putAll(tempBids);
            asks.putAll(tempAsks);
        } else { //Assume that we will only be adding "clean" data to the orderbook
            log.error("bids and asks didn't pass checks, not adding to orderbook");
        }

    }

    public void updateCandle() {
        double mid = getMidPrice();
        if (candle.getOpen() == null) {
            candle.setOpen(mid);
        }
        candle.setHigh(Math.max(candle.getHigh(), mid));
        candle.setLow(Math.min(candle.getLow(), mid));
        candle.setClose(mid);
    }

    public Double getMidPrice() {

        if (!bids.isEmpty() && !asks.isEmpty()) {
            double highestBid = bids.lastKey();
            double lowestAsk = asks.firstKey();
            return (highestBid + lowestAsk) / 2D;
        }
        return 0D;
    }

    @Scheduled(fixedRate = 5000)
    public void printCandleData() {
        if (bids.isEmpty() || asks.isEmpty()) {
            log.error("The bids or asks are empty, will print data in next candle");
        } else {
            String logMessage = String.format("Timestamp: %d, open: %f, high: %f, low: %f, close: %f, ticks: %d",
                    System.currentTimeMillis() / 1000,
                    candle.getOpen(),
                    candle.getHigh(),
                    candle.getLow(),
                    candle.getClose(),
                    ticks);
            kafkaTemplate.send(topic, logMessage);
            /*log.info("Timestamp: {}, open: {}, high: {}, low: {}, close: {}, ticks: {}",
                    System.currentTimeMillis() / 1000, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), ticks);*/
            log.info("Published to Kafka");
            resetCandleData();
        }

    }

    private void resetCandleData() {
        candle = Candle.setupCandle();
        ticks = 0;
        log.info("Candle data reset, ticks now: {}", ticks);
    }

}
