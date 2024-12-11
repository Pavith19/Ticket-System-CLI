/**
 * Represents a ticket for an event in the ticketing system.
 * Each ticket has a specific event name, price, and vendor ID associated with it.
 */
public class Ticket implements TicketInterface {
    private final String eventName;
    private final double price;
    private final int vendorId;

    /**
     * Constructs a new Ticket object with the specified event name, price, and vendor ID.
     *
     * @param eventName The name of the event for which the ticket is being created.
     * @param price The price of the ticket.
     * @param vendorId The ID of the vendor offering the ticket.
     */
    public Ticket(String eventName, double price, int vendorId) {
        this.eventName = eventName;
        this.price = price;
        this.vendorId = vendorId;
    }

    /**
     * Returns the name of the event associated with this ticket.
     *
     * @return The event name.
     */
    @Override
    public String getEventName() {
        return eventName;
    }

    /**
     * Returns the price of the ticket.
     *
     * @return The price of the ticket.
     */
    @Override
    public double getPrice() {
        return price;
    }

    /**
     * Returns the ID of the vendor selling this ticket.
     *
     * @return The vendor ID.
     */
    @Override
    public int getVendorId() {
        return vendorId;
    }
}
