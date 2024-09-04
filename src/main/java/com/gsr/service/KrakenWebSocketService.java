package com.gsr.service;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.java_websocket.client.WebSocketClient;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.gsr.constants.DataConstants.*;

@Service
@Slf4j
public class KrakenWebSocketService extends WebSocketClient {

    private final OrderbookService orderbookService;
    private final String currencyToCheck = "BTC/USD";

    public KrakenWebSocketService(OrderbookService orderbookService) throws URISyntaxException {
        super(new URI("wss://ws.kraken.com/v2"));
        this.orderbookService = orderbookService;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Kraken WebSocket opened");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(METHOD, SUBSCRIBE);

        JSONObject params = new JSONObject();
        params.put(CHANNEL, BOOK);
        params.put(SYMBOL, new String[]{currencyToCheck});

        jsonObject.put(PARAMS, params);
        send(jsonObject.toString());
    }

    @Override
    public void onMessage(String message) {
        //Check if data we need is in the message first
        orderbookService.updateTicks();
        List<JSONArray> bidsAndAsks = processMessage(message);
        if (!bidsAndAsks.isEmpty()) {
            orderbookService.updateOrderbook(bidsAndAsks);
        }

    }

    private List<JSONArray> processMessage(String message) {
        JSONObject jsonMessage = new JSONObject(message);
        List<JSONArray> result = new ArrayList<>();
        if (jsonMessage.has(DATA) && jsonMessage.get(DATA) instanceof JSONArray) {
            JSONArray dataArray = jsonMessage.getJSONArray(DATA);
            if (!dataArray.isEmpty()) {
                JSONObject dataObject = dataArray.getJSONObject(0);
                if (dataObject.has(SYMBOL) && dataObject.has(BIDS) && dataObject.has(ASKS)) {
                    String symbol = dataObject.getString(SYMBOL);
                    if (currencyToCheck.equalsIgnoreCase(symbol)) { //in case data from other pairs are in inside as well
                        JSONArray bidsArray = dataObject.getJSONArray(BIDS);
                        JSONArray asksArray = dataObject.getJSONArray(ASKS);
                        if (!bidsArray.isEmpty() && !asksArray.isEmpty()) { //both sides must be present before updating orderbook
                            result.add(bidsArray);
                            result.add(asksArray);
                        }
                    } else {
                        log.error("Wrong pair to check: {}", symbol);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.info("Kraken WebSocket closed: {}", s);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}
