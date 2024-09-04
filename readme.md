_# Kraken WebSocket Candle Aggregator

This project is a Java-based application that connects to the Kraken WebSocket to consume real-time order book data for the BTC/USD trading pair. The application aggregates this data into 1-minute candle data, which includes the following information:

- **Timestamp**: Start time of the minute (in epoch).
- **Open**: Mid-price at the start of the minute.
- **High**: Highest mid-price during the minute.
- **Low**: Lowest mid-price during the minute.
- **Close**: Mid-price at the end of the minute.
- **Ticks**: Total number of ticks observed during the minute.

## Assumptions

- The 1-minute candle aggregation starts as soon as the application is launched.
- Only the data for the BTC/USD pair is processed.

## Prerequisites

- Docker
- Docker Compose

## How to Run

1. **Clone the Repository**:
   ```bash
   git clone
   cd candle-aggregator

2. **Build the Docker image**:
   ```bash
   docker build -t candle-aggregator .

3. **Run the Application with Docker Compose**:
   ```bash
   docker-compose up

This will start Zookeeper, Kafka, and the application. The application will connect to Kraken's WebSocket, consume data for the BTC/USD pair, and publish the 1-minute candle data to a Kafka topic named candle-data.

## Notes

- Sanity checks are done such that the highest bid is always less than the lowest ask when building the tick level order book