/**
 * Interface representing a ticket in the ticketing system.
 * This interface defines the basic methods that any ticket class must implement.
 */
public interface TicketInterface {
    String getEventName();
    double getPrice();
    int getVendorId();
}