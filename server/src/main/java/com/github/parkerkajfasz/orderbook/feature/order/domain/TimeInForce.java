package com.github.parkerkajfasz.orderbook.feature.order.domain;

/**
 *  LIMIT + GTC: Already assumed with LIMIT*
 *
 *  MARKET + GTC: Should probably be rejected as invalid
 *
 *  LIMIT + IOC: Execute immediately against resting orders while the price crosses
 *      - Allow partial fills
 *      - Stop if price no longer crosses OR liquidity exhausted
 *      - Cancel any remaining quantity
 *      - Never rest on the book
 *
 *  MARKET + IOC: Execute immediately against best available prices*
 *      - Ignore price constraints
 *      - Allow partial fills
 *      - Continue until volume filled or book side empty
 *      - Cancel any remaining quantity
 *      - Never rest on the book
 */
public enum TimeInForce {
    GOOD_TILL_CANCEL, IMMEDIATE_OR_CANCEL
}
