import java.util.Scanner;

/**
 * Main class for the Ticket System application.
 * Handles the primary flow, user interaction, and ticket pool operations.
 */
public class TicketSystem {

    /**
     * Entry point of the Ticket System application.
     * Initializes the TicketPool, Database, and manages user input through a menu-driven interface.
     *
     * @param args Command-line arguments (not used in this context).
     */
    public static void main(String[] args) {
        // Initialize input scanner, ticket pool, and database
        Scanner scanner = new Scanner(System.in);
        TicketPool ticketPool = new TicketPool();
        Database database = new Database();

        System.out.println("\n\n\t\t<< Welcome to the Real-Time Ticketing System >>");

        // Main menu loop
        while (true) {
            displayMenu(); // Display the menu options
            try {
                System.out.print(">> Enter your choice: ");
                String command = scanner.nextLine().trim().toLowerCase();

                // Process user commands
                switch (command) {
                    case "1": // Configure the system
                        ticketPool.configureSystem(scanner, database);
                        break;

                    case "2": // Start the system
                        // Check if system is properly configured before starting
                        if (ticketPool.isConfigured()) {
                            // Prevent starting if all tickets are sold out
                            if (ticketPool.getTicketsSold() >= ticketPool.getTotalTickets()) {
                                System.out.println("\nAll tickets have been sold out. Please reset the ticket system before starting again.");
                            } else {
                                System.out.println("\nTicket system is running. Press 3 to stop the system at any time.\n");

                                // Begin ticket sales process
                                ticketPool.startTicketHandling();

                                // Manual system stop
                                while (true) {
                                    String userInput = scanner.nextLine().trim().toLowerCase();

                                    if (userInput.equals("3")) {
                                        ticketPool.stopTicketHandling();
                                        break;
                                    }

                                    // If system auto-stops due to all tickets sold
                                    if (ticketPool.getTicketsSold() >= ticketPool.getTotalTickets()) {

                                        // Prompt for reset after auto-stop
                                        while (true) {
                                            System.out.print("System must be reset before use. Press 4 to reset.\n");
                                            String resetCommand = scanner.nextLine().trim().toLowerCase();

                                            // Reset system when user confirms
                                            if (resetCommand.equals("4")) {
                                                TicketSystemLogger.info("Ticket handling system has been reset and is ready to start again.\n");
                                                ticketPool.resetTicketHandling();
                                                break;
                                            } else {
                                                System.out.println("Invalid input. Please enter 4 to reset the system.\n");
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            // Prevent starting an unconfigured system
                            throw new IllegalStateException("Configuration incomplete. Please configure the system first.");
                        }
                        break;

                    case "3": // Stop the system
                        ticketPool.stopTicketHandling();
                        break;

                    case "4": // Reset the system
                        if (ticketPool.isConfigured()) {
                            System.out.println();
                            TicketSystemLogger.info("Ticket handling system has been reset.");
                            ticketPool.resetTicketHandling();
                        } else {
                            throw new IllegalStateException("Configuration incomplete. Please configure the system first.");
                        }
                        break;

                    case "5": // Exit the application
                        System.out.println("\nExiting the system... Goodbye!");
                        TicketSystemLogger.info("Exiting the system. Application is stopping.");
                        System.exit(0); // Terminate the application.
                        break;

                    default:
                        // Throw exception for unrecognized menu choices
                        throw new IllegalArgumentException("Invalid choice. Please enter a valid option from the menu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("\n" + e.getMessage());
            } catch (Exception e) {
                // Handle unexpected errors and log them.
                TicketSystemLogger.severe("Unexpected error occurred: " + e.getMessage());
                System.out.println("An unexpected error occurred. Please try again.");
            }
        }
    }

    /**
     * Displays the menu options for the ticket system.
     * Shows available actions user can take in the system.
     */
    private static void displayMenu() {
        System.out.println("\n--- Ticket System Menu ---");
        System.out.println("1. Configure System");
        System.out.println("2. Start Ticket Handling");
        System.out.println("3. Stop Ticket Handling");
        System.out.println("4. Reset System");
        System.out.println("5. Exit");
    }
}