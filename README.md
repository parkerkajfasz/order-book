# Real-Time Order Book & Matching Engine

A real-time **electronic exchange matching engine** built with **Java and Spring Boot**.

This project simulates how modern financial exchanges process orders, match trades, and broadcast market data for a security. Orders are submitted through a REST API, matched using a **price-time priority algorithm**, and broadcast in real time via a **WebSocket** connection.

---

## Features
* Processing of **limit and market orders**
* **Good-Till-Canceled (GTC)** and **Immediate-Or-Cancel (IOC)** time-in-force policies
* Trade execution using a **price-time priority matching algorithm**
* **L1, L2, and L3 order book depth views** along with a **real-time trade feed** 

---

## API Usage

Orders can be submitted individually or in batches.

#### Request Parameters

| Parameter     | Type          | Description & Options                                                                                                                       |
| :------------ | :------------ | :------------------------------------------------------------------------------------------------------------------------------------------ |
| `orderType`   | Enum (String) | Specifies the pricing strategy for the order. <br>**Options:** `LIMIT` or `MARKET`                                                          |
| `timeInForce` | Enum (String) | Dictates how long the order remains active before it expires or is canceled. <br>**Options:** `GOOD_TILL_CANCELED` or `IMMEDIATE_OR_CANCEL` |
| `side`        | Enum (String) | The direction of the trade. <br>**Options:** `BUY` or `SELL`                                                                                |
| `price`       | Integer       | The limit price per unit                                                                                                                    |
| `volume`      | Integer       | The total quantity of assets to be traded                                                                                                   |

---

### Submit Single Order

```
POST /api/v1/orders
```

### Submit Multiple Orders (Recommended)

Batch submission makes it easier to trigger matching and observe trades.

```
POST /api/v1/orders/batch
```

Example request:

```json
[
  {
    "orderType": "LIMIT",
    "timeInForce": "GOOD_TILL_CANCEL",
    "side": "SELL",
    "price": 10,
    "volume": 2
  },
  {
    "orderType": "LIMIT",
    "timeInForce": "IMMEDIATE_OR_CANCEL",
    "side": "SELL",
    "price": 10,
    "volume": 1
  },
  {
    "orderType": "MARKET",
    "timeInForce": "IMMEDIATE_OR_CANCEL",
    "side": "BUY",
    "price": 11,
    "volume": 5
  }
]
```

---

## WebSocket Streaming

The system publishes market updates through **WebSocket** so the market state can be oberserved in real time without polling the REST API. The system supports multiple levels of market depth visualization.

#### Order Book Depth Views

| View   | Description                               |
| ------ | ----------------------------------------- |
| **L1** | Best bid and best ask (top of book)       |
| **L2** | Aggregated price levels with total volume |
| **L3** | Full order book showing individual orders |

---

### Running the Project

#### Requirements

* **Java 17+**
* **Maven or Gradle**
* **Spring Boot**

---

#### Start the Server

```
./mvnw spring-boot:run
```

or

```
mvn spring-boot:run
```

The API will start at:

```
http://localhost:8080
```

---

#### Example Usage

1. Start the server
2. Submit batch orders using `/orders/batch`
3. Observe:
   * trades being executed
   * changes to order book state

#### Demo
![Demo](https://github.com/user-attachments/assets/df18284b-a972-42d3-b74d-ed5db528c11b)
