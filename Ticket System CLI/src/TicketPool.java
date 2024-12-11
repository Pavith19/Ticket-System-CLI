import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Manages the ticket pool system, handling ticket addition, purchase,
 * and overall system state for a multi-threaded ticket sales environment.
 */
public class TicketPool {
    private final TicketPoolConfiguration configuration;

    // System state flags and tracking variables
    private volatile boolean running = false;
    private int ticketsAdded = 0; // Tracks total tickets added across all vendors
    private int ticketsSold = 0;  // Tracks total tickets sold
    private int currentTickets = 0; // Tracks tickets currently in the pool

    // Shared pool of tickets
    private final List<Ticket> ticketPool = Collections.synchronizedList(new ArrayList<>());
    // Lock for ensuring thread safety in ticket operations
    private final ReentrantLock lock = new ReentrantLock();

    // Semaphore to signal available tickets
    private Semaphore ticketsAvailable = new Semaphore(0);

    // Queue for customers waiting to purchase tickets
    private final BlockingQueue<Integer> customerQueue = new LinkedBlockingQueue<>();

    // Database instance for logging transactions
    private final Database database = new Database();

    // Lists to track vendor and customer threads
    private final List<Thread> vendorThreads = new ArrayList<>();
    private final List<Thread> customerThreads = new ArrayList<>();

    // Flags for logging and system state
    private boolean waitingMessageLogged = false;
    private boolean stopped = false;

    /**
     * Constructs a new TicketPool with default configuration and logger setup.
     */
    public TicketPool() {
        this.configuration = new TicketPoolConfiguration();
        TicketSystemLogger.configureLogger();
    }

    /**
     * Retrieves the price of a specific event.
     *
     * @param eventName the name of the event
     * @return the price of the event
     * @throws IllegalArgumentException if the event name is not found
     */
    public double getEventPrice(String eventName) {
        Map<String, Double> eventPrices = configuration.getEventPrices();
        if (!eventPrices.containsKey(eventName)) {
            TicketSystemLogger.warning("Event name not found: " + eventName + ". Available events: " + eventPrices.keySet());
            throw new IllegalArgumentException("Event name not found: " + eventName);
        }
        return eventPrices.get(eventName);
    }

    // Configures the ticket system
    public void configureSystem(Scanner scanner, Database database) {
        configuration.configureSystem(scanner, database);
    }

    // Checks if the system is fully configured and ready to operate.
    public boolean isConfigured() {
        return configuration.isConfigured();
    }

    /**
     * Starts ticket handling by creating vendor and customer threads.
     * Dynamically assigns vendors to events and starts customer threads.
     */
    public void startTicketHandling() {
        // Check if system is already running
        if (running) {
            System.out.println("\nSystem is already running.");
            return;
        }
        running = true;

        // Start vendor threads for each event
        int vendorId = 1;
        Map<String, Double> eventPrices = configuration.getEventPrices();
        for (Map.Entry<String, Double> entry : eventPrices.entrySet()) {
            String eventName = entry.getKey();
            Thread vendorThread = new Thread(new Vendor(this, vendorId, eventName));
            vendorThread.start();
            vendorThreads.add(vendorThread);
            vendorId++;
        }

        // Start customer threads
        for (int i = 1; i <= 20; i++) { // 20 customers
            Thread customerThread = new Thread(new Customer(this, i));
            customerThread.start();
            customerThreads.add(customerThread);
        }

        TicketSystemLogger.info("System started. Vendors and customers are now active.");
    }

    /**
     * Stops the ticket handling process and logs system statistics.
     */
    public void stopTicketHandling() {
        if (!running) {
            System.out.println("\nSystem is not running.");
            return;
        }
        running = false;

        // Log system statistics
        TicketSystemLogger.info("System Stopped - Total Statistics:");
        TicketSystemLogger.info("Total Tickets Added to Pool: " + ticketsAdded);
        TicketSystemLogger.info("Total Tickets Sold: " + ticketsSold);
        TicketSystemLogger.info("System stopped. All operations halted.");

        // Interrupt all vendor and customer threads
        vendorThreads.forEach(Thread::interrupt);
        customerThreads.forEach(Thread::interrupt);
    }

    /**
     * Adds tickets to the pool by a vendor.
     *
     * @param eventName the name of the event
     * @param vendorId  the ID of the vendor
     * @param ticketsToAdd the number of tickets to add
     * @param price the price of each ticket
     */
    public void addTickets(String eventName, int vendorId, int ticketsToAdd, double price) {
        lock.lock();
        try {
            int totalTickets = configuration.getTotalTickets();
            if (ticketsAdded >= totalTickets) {
                // Stop the vendor thread gracefully
                Thread.currentThread().interrupt();
                return;
            }

            // Calculate remaining ticket capacity
            int remainingCapacity = totalTickets - ticketsAdded;
            ticketsToAdd = Math.min(ticketsToAdd, remainingCapacity);

            // Add tickets to the pool and notify waiting customers
            if (ticketsToAdd > 0) {
                for (int i = 0; i < ticketsToAdd; i++) {
                    Ticket ticket = new Ticket(eventName, price, vendorId);
                    ticketPool.add(ticket);
                    ticketsAvailable.release(); // Notify waiting customers

                }

                // Update tracking variables
                ticketsAdded += ticketsToAdd;
                currentTickets += ticketsToAdd;
                TicketSystemLogger.info("Vendor " + vendorId + " added " + ticketsToAdd + " ticket(s) for " + eventName + " (Price: $" + String.format("%.2f", price) + ")");
                logCurrentTickets(); // Log the current tickets after adding

                // Reset the waiting message flag when tickets are added
                waitingMessageLogged = false;
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Allows a customer to purchase tickets from the pool.
     *
     * @param customerId the ID of the customer making the purchase
     */
    public void purchaseTickets(int customerId) {
        try {
            // Add customer to the waiting queue (FIFO order)
            customerQueue.put(customerId);

            // Check if the system is stopped and stop customers from purchasing if all tickets are sold
            if (stopped) {
                TicketSystemLogger.info("System is stopped. Customer " + customerId + " cannot purchase any tickets.");
                return;
            }

            // Wait for tickets to become available
            ticketsAvailable.acquire();

            // Lock the critical section
            lock.lock();
            try {
                // Check stopped flag again to ensure thread-safety
                if (stopped) {
                    ticketsAvailable.release(); // Release the semaphore if stopped
                    return;
                }
                // Ensure there are tickets available for purchase
                if (ticketPool.isEmpty()) {
                    // Log message only once
                    if (!waitingMessageLogged) {
                        TicketSystemLogger.info("Customers are waiting for tickets to become available.");
                        waitingMessageLogged = true; // Set the flag to prevent duplicate logging
                    }
                    return; // Exit early if no tickets are available
                }

                // Generate a random number of tickets the customer will attempt to buy
                Random random = new Random();
                int ticketsToBuy = random.nextInt(1,configuration.getCustomerRetrievalRate()) + 1;

                // If the requested tickets exceed the available tickets, adjust the purchase to the available amount.
                int availableTickets = ticketPool.size();
                if (ticketsToBuy > availableTickets) {
                    TicketSystemLogger.info("Customer " + customerId + " requested " + ticketsToBuy +
                            " tickets, but only " + availableTickets +
                            " tickets available in pool. Purchasing available tickets.");
                    ticketsToBuy = availableTickets;
                }

                // Track the total price for the purchased tickets
                double totalPrice = 0.0;
                List<String> eventNames = new ArrayList<>(); // To store the event names for purchased tickets

                // Process the purchase
                for (int i = 0; i < ticketsToBuy; i++) {
                    Ticket ticket = ticketPool.remove(0); // Remove one ticket from the pool for each purchase
                    ticketsSold++;
                    currentTickets--;
                    totalPrice += ticket.getPrice();  // Add ticket price to the total
                    eventNames.add(ticket.getEventName()); // Add event name to the list
                    database.logTransaction(
                            ticket.getEventName(),
                            ticket.getPrice(),
                            ticket.getVendorId(),
                            customerId,
                            1
                    );
                }

                // Log the total number of tickets purchased, the events, and the total price in one log entry
                String eventsPurchased = String.join(", ", eventNames);
                TicketSystemLogger.info("Customer " + customerId + " purchased " + ticketsToBuy + " ticket(s) for events: " + eventsPurchased +
                        " | Total Price: $" + String.format("%.2f", totalPrice));

                logCurrentTickets(); // Log the current ticket count after purchase

                // If all tickets are sold, stop the system
                if (ticketsSold >= configuration.getTotalTickets() && ticketPool.isEmpty()) {
                    TicketSystemLogger.info("All tickets have been sold, and the ticket limit has been reached. Stopping the system...");
                    stopped = true; // Set the stopped flag to true
                    System.out.println("\nPress Enter to continue..\n");
                    stopTicketHandling(); // Stop the entire system, including vendors and customers

                }
            } finally {
                lock.unlock(); // Ensure the lock is always released
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt status
        }

    }

    /**
     * Resets the ticket handling system to its initial state.
     */
    public void resetTicketHandling() {
        // Stop the current ticket handling if it's running
        if (running) {
            stopTicketHandling();
        }

        // Clear all internal state
        lock.lock();
        try {
            // Reset counters
            ticketsAdded = 0;
            ticketsSold = 0;
            currentTickets = 0;

            // Clear ticket pool
            ticketPool.clear();
            ticketsAvailable = new Semaphore(0);
            customerQueue.clear();

            // Clear thread lists
            vendorThreads.clear();
            customerThreads.clear();

            // Reset running and stopped flags
            running = false;
            stopped = false;

            // Clear all transactions from the transactions table in the database.
            database.clearTransactionsTable();

        } finally {
            lock.unlock();
        }
    }

    // Getters for configuration rates and ticket statistics
    public int getCustomerRetrievalRate() {
        return configuration.getCustomerRetrievalRate();
    }
    public int getTicketReleaseRate() {
        return configuration.getTicketReleaseRate();
    }
    public int getTicketsSold() {
        return ticketsSold;
    }

    public int getTotalTickets() {
        return configuration.getTotalTickets();
    }

    /**
     * Logs the current ticket pool status, including available tickets, added tickets, and sold tickets.
     */
    private void logCurrentTickets() {
        TicketSystemLogger.info(String.format("Ticket Pool Status - Current Tickets: %d | Total Tickets Added: %d | Total Tickets Sold: %d",
                currentTickets,
                ticketsAdded,
                ticketsSold));
    }
}