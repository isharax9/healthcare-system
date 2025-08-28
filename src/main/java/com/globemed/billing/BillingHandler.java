package com.globemed.billing;

/**
 * The Handler interface declares a method for building the chain of handlers.
 * It also declares a method for executing a request.
 */
public interface BillingHandler {

    /**
     * Sets the next handler in the chain.
     * @param next The next handler to be called.
     */
    void setNext(BillingHandler next);

    /**
     * Processes the given medical bill.
     * @param bill The bill to be processed.
     * @return true if the processing can continue, false if the chain should stop.
     */
    boolean processBill(BillProcessingRequest request);
}