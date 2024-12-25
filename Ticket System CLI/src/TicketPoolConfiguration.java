/**
 * Author: Pavith Bambaravanage
 * URL: https://github.com/Pavith19
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Class to configure the ticket pool system, including setting ticket release rates, retrieval rates,
 * and event prices. It also manages the database interactions related to the configuration.
 */
public class TicketPoolConfiguration {
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private final Map<String, Double> eventPrices = new HashMap<>();
    private boolean configured = false;

    /**
     * Configures the ticket system by loading existing configuration from the database,
     * allowing reconfiguration if desired, and asking the user for inputs for various system settings.
     * @param scanner the Scanner object to read user inputs
     * @param database the Database object to interact with the system's database
     */
    public void configureSystem(Scanner scanner, Database database) {

        // Clear any previous transactions in the database
        database.clearTransactionsTable();

        // Load existing configuration from the database
        if (database.loadConfiguration(this)) {

            System.out.println("\nLoading existing configuration...");
            System.out.println("\nExisting configuration loaded successfully!");

            // Call the method to display the current system configuration
            displayCurrentConfiguration();

            // Ask user if they want to reconfigure the system
            String response;
            while (true) {
                System.out.print("\n>> Do you want to reconfigure the system? (yes/no): ");
                response = scanner.nextLine().trim().toLowerCase();

                // Handle invalid response for reconfiguration prompt
                if (response.equals("yes") || response.equals("no")) {
                    break;
                } else {
                    System.out.println("\nInvalid input. Please enter 'yes' or 'no'.");
                }
            }

            if (response.equals("no")) {
                configured = true;
                return;
            }
            // Clear all previous event records in the events table
            database.clearEventsTable();
            // Clear the eventPrices map to reset event details
            eventPrices.clear();
        }else {
            System.out.println("\nNo existing configuration found. Let's configure the system...");
        }

        // Ask the user for the system settings
        System.out.println("\n=== Configure the Ticket System ===");
        System.out.print("\n>> Enter max ticket capacity (min: 10, max: 10000): ");
        maxTicketCapacity = getValidInput(scanner, 10, 10000,
                "Max ticket capacity must be between 10 and 10000.");

        // Get total tickets with context of max capacity
        System.out.printf(">> Enter total number of tickets (min: 1, max: %d): ", maxTicketCapacity);
        totalTickets = getValidInput(scanner, 1, maxTicketCapacity,
                "Total tickets must be between 1 and " + maxTicketCapacity + ".");

        // Ticket release rate input
        while (true) {
            System.out.printf(">> Enter ticket release rate (min: 1, max: %d): ", totalTickets);
            ticketReleaseRate = getValidInput(scanner, 1, totalTickets,
                    "Ticket release rate must be between 1 and " + totalTickets + " tickets.");
            break;
        }

        // Customer retrieval rate input
        while (true) {
            System.out.printf(">> Enter customer retrieval rate (min: 1, max: %d): ", totalTickets);
            customerRetrievalRate = getValidInput(scanner, 1, totalTickets,
                    "Customer retrieval rate must be between 1 and " + totalTickets + " tickets.");
            break;
        }

        // Configure events and their prices
        System.out.print("\n>> Enter the number of events (min: 1, max: 20): ");
        int eventCount = getValidInput(scanner, 1, 20,
                "Number of events must be between 1 and 20.");

        for (int i = 1; i <= eventCount; i++) {
            // Event name input
            String eventName;
            while (true) {
                System.out.print(">> Enter name for Event " + i + " (3-20 characters): ");
                eventName = scanner.nextLine().trim();

                // Check event name length
                if (eventName.length() < 3 || eventName.length() > 20) {
                    System.out.println("\nInvalid event name. Must be between 3 and 20 characters.");
                    continue;
                }

                // Check for duplicate event names
                String checkEventName = eventName;
                if (eventPrices.keySet().stream()
                        .anyMatch(existingName -> existingName.equalsIgnoreCase(checkEventName))) {
                    System.out.println("\nAn event with this name already exists. Please choose a different name.\n");
                    continue;
                }

                break;
            }

            // Event price input
            System.out.print(">> Enter ticket price for " + eventName + " ($1 to $10,000): $");
            double price = getValidDoubleInput(scanner, 1, 10000,
                    "Ticket price must be between $1 and $10,000.");

            // Save event details to the database
            database.saveEventDetails(i, eventName, price);

            // Store in memory for runtime usage
            eventPrices.put(eventName, price);
        }

        // Save configuration to the database
        System.out.println();
        TicketSystemLogger.info("System Configured Successfully - Configuration Details: " +
                "Total Tickets: " + totalTickets + ", " +
                "Max Ticket Capacity: " + maxTicketCapacity + ", " +
                "Ticket Release Rate: " + ticketReleaseRate + ", " +
                "Customer Retrieval Rate: " + customerRetrievalRate);
        // Log event details
        TicketSystemLogger.info("Event Configuration:");
        for (Map.Entry<String, Double> entry : eventPrices.entrySet()) {
            TicketSystemLogger.info("Event: " + entry.getKey() + " | Ticket Price: $" + String.format("%.2f", entry.getValue()));
        }

        database.saveConfiguration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
        System.out.println("\nSystem configured successfully.\n");
        configured = true;
    }

    /**
     * Helper method to get a valid integer input within a specified range.
     * @param scanner the Scanner object to read user input
     * @param min the minimum valid value
     * @param max the maximum valid value
     * @param errorMessage the error message to show when input is invalid
     * @return a valid integer input
     */
    private int getValidInput(Scanner scanner, int min, int max, String errorMessage) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}

            System.out.println("\nInvalid input. " + errorMessage);
            System.out.print("Please try again: ");
        }
    }

    /**
     * Helper method to get a valid double input within a specified range.
     * @param scanner the Scanner object to read user input
     * @param min the minimum valid value
     * @param max the maximum valid value
     * @param errorMessage the error message to show when input is invalid
     * @return a valid double input
     */
    private double getValidDoubleInput(Scanner scanner, int min, int max, String errorMessage) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}

            System.out.println("\nInvalid input. " + errorMessage);
            System.out.print("Please try again: ");
        }
    }

    /**
     * Displays the current configuration of the ticket system, including
     * ticket details and event information.
     */
    public void displayCurrentConfiguration() {
        System.out.println("\n< Current System Configuration >");

        System.out.println("Total Tickets: " + totalTickets);
        System.out.println("Ticket Release Rate: " + ticketReleaseRate);
        System.out.println("Customer Retrieval Rate: " + customerRetrievalRate);
        System.out.println("Max Ticket Capacity: " + maxTicketCapacity);

        System.out.println("\n< Events and Prices >");
        if (eventPrices.isEmpty()) {
            System.out.println("No events configured yet.");
        } else {
            eventPrices.forEach((eventName, price) ->
                    System.out.println("Event: " + eventName + " | Ticket Price: ₹" + String.format("%.2f", price))
            );
        }
    }

    // Getters for the configuration properties
    public int getTotalTickets() {
        return totalTickets;
    }

    public int getTicketReleaseRate() {
        return ticketReleaseRate;
    }

    public int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public Map<String, Double> getEventPrices() {
        return new HashMap<>(eventPrices);
    }

    public boolean isConfigured() {
        return configured;
    }

    // Setter methods for the configuration properties
    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void setTicketReleaseRate(int ticketReleaseRate) {
        this.ticketReleaseRate = ticketReleaseRate;
    }

    public void setCustomerRetrievalRate(int customerRetrievalRate) {
        this.customerRetrievalRate = customerRetrievalRate;
    }
    public void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    public void addEventPrice(String eventName, double price) {
        this.eventPrices.put(eventName, price);
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }
}
