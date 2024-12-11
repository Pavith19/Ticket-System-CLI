/**
 * Represents a customer thread that attempts to purchase tickets from a shared ticket pool.
 * Customers periodically try to purchase tickets based on a configurable retrieval rate,
 * with the purchase attempt happening at a frequency determined by the system configuration.
 */

public class Customer implements Runnable {
    private final TicketPool ticketPool;
    private final int customerId;

    /**
     * Constructs a Customer with the specified TicketPool and customerId.
     *
     * @param ticketPool The shared TicketPool instance.
     * @param customerId Unique identifier for this customer.
     */
    public Customer(TicketPool ticketPool, int customerId) {
        this.ticketPool = ticketPool;
        this.customerId = customerId;
    }

    /**
     * Executes the customer thread which attempts to purchase tickets periodically.
     * The purchase attempt is made based on the configured retrieval rate.
     */
    @Override
    public void run() {
        try {
            // Run continuously until the thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.purchaseTickets(customerId);

                // Sleep for a duration based on the configured customer retrieval rate (adjusted by 30 seconds)
                // This controls how frequently customers attempt to purchase tickets
                Thread.sleep(30000 / ticketPool.getCustomerRetrievalRate());
            }
        } catch (InterruptedException e) {
            // Handle interruption and restore interrupt status
            Thread.currentThread().interrupt();
        }
    }
}