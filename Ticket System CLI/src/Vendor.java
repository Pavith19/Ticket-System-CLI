import java.util.Random;

/**
 * Represents a vendor thread that adds tickets to a shared ticket pool.
 * Vendors periodically add tickets for a specific event, with the number of tickets
 * varying each time based on the configured ticket release rate.
 */
public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int vendorId;
    private final String eventName;

    /**
     * Constructs a Vendor for a specific event.
     *
     * @param ticketPool The TicketPool instance where tickets are managed
     * @param vendorId The ID of the vendor
     * @param eventName The event name the vendor is selling tickets for
     */
    public Vendor(TicketPool ticketPool, int vendorId, String eventName) {
        this.ticketPool = ticketPool;
        this.vendorId = vendorId;
        this.eventName = eventName;
    }

    /**
     * Runs the vendor thread, continuously adding tickets to the pool for a specific event.
     * The vendor will add tickets at a controlled rate until interrupted.
     */
    @Override
    public void run() {
        Random random = new Random();
        try {
            // Continuously add tickets until interrupted
            while (!Thread.currentThread().isInterrupted()) {
                // Tickets added range from 1 to the ticket release rate
                int ticketsToAdd = random.nextInt(1,ticketPool.getTicketReleaseRate()) + 1; // Add 1-5 tickets

                // Retrieve the constant price for the event from TicketPool
                double price = ticketPool.getEventPrice(eventName);

                // Add tickets with the fixed price
                ticketPool.addTickets(eventName, vendorId, ticketsToAdd, price);
                // Sleep to control the release rate of tickets (adjust based on ticketReleaseRate)
                Thread.sleep(30000 / ticketPool.getTicketReleaseRate()); // Adjust sleep duration based on rate
            }
        } catch (InterruptedException e) {
            // Handle interruption and restore interrupt status
            Thread.currentThread().interrupt();
        }
    }
}