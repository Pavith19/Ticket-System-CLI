import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database operations for the Ticket Handling System.
 * It provides methods to manage system configurations, events, and transactions.
 */
public class Database {
    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());

    // Database connection credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ticketing_systemdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Pavistar66";

    /**
     * Constructor that initializes the database by creating necessary tables.
     */
    public Database() {
        initializeDatabase();
    }

    /**
     * Establishes a connection to the database.
     *
     * @return a Connection object representing the database connection
     * @throws SQLException if a database access error occurs
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Initializes the database by creating necessary tables if they do not already exist.
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Database initialization error", e);
        }
    }

    /**
     * Creates required tables in the database.
     *
     * @param conn the database connection
     * @throws SQLException if a database error occurs during table creation
     */
    private void createTables(Connection conn) throws SQLException {
        String[] createTableQueries = {
                // System configuration table to store configuration settings
                "CREATE TABLE IF NOT EXISTS system_config (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "total_tickets INT NOT NULL, " +
                        "release_rate INT NOT NULL, " +
                        "retrieval_rate INT NOT NULL, " +
                        "max_capacity INT NOT NULL)",

                // Transactions table to log all ticket purchases
                "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "event_name VARCHAR(255) NOT NULL, " +
                        "ticket_price DOUBLE NOT NULL, " +
                        "vendor_id INT, " +
                        "customer_id INT, " +
                        "ticket_count INT, " +
                        "transaction_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Events table to store event-specific details
                "CREATE TABLE IF NOT EXISTS events (" +
                        "event_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "event_name VARCHAR(255) NOT NULL UNIQUE, " +
                        "event_price DOUBLE NOT NULL)"
        };

        try (Statement stmt = conn.createStatement()) {
            // Execute each table creation query
            for (String query : createTableQueries) {
                stmt.execute(query);
            }
        }
    }

    /**
     * Saves the system configuration into the database.
     *
     * @param totalTickets   the total number of tickets
     * @param releaseRate    the ticket release rate
     * @param retrievalRate  the ticket retrieval rate
     * @param maxCapacity    the maximum ticket capacity
     */
    public void saveConfiguration(int totalTickets, int releaseRate, int retrievalRate, int maxCapacity) {
        String deleteOldConfig = "DELETE FROM system_config";
        String insertConfig = "INSERT INTO system_config " +
                "(total_tickets, release_rate, retrieval_rate, max_capacity) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteOldConfig);
             PreparedStatement insertStmt = conn.prepareStatement(insertConfig)) {

            conn.setAutoCommit(false);  // Begin transaction

            deleteStmt.executeUpdate();
            insertStmt.setInt(1, totalTickets);
            insertStmt.setInt(2, releaseRate);
            insertStmt.setInt(3, retrievalRate);
            insertStmt.setInt(4, maxCapacity);
            insertStmt.executeUpdate();

            conn.commit();  // Commit transaction
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving configuration", e);
            throw new RuntimeException("Configuration save failed", e);
        }
    }

    /**
     * Clears all entries in the events table.
     */
    public void clearEventsTable() {
        String deleteAllEvents = "DELETE FROM events";

        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteAllEvents)) {
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing events table", e);
            throw new RuntimeException("Events table clear failed", e);
        }
    }

    /**
     * Saves details of an event into the database.
     *
     * @param eventId    the ID of the event
     * @param eventName  the name of the event
     * @param eventPrice the price of the event
     */
    public void saveEventDetails(int eventId, String eventName, double eventPrice) {
        String insertEvent = "INSERT INTO events (event_id, event_name, event_price) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertEvent)) {

            // Validate input
            if (eventName == null || eventName.trim().isEmpty()) {
                throw new IllegalArgumentException("Event name cannot be empty");
            }
            if (eventPrice <= 0) {
                throw new IllegalArgumentException("Event price must be positive");
            }

            insertStmt.setInt(1, eventId);
            insertStmt.setString(2, eventName.trim());
            insertStmt.setDouble(3, eventPrice);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving event details", e);
            throw new RuntimeException("Event details save failed", e);
        }
    }

    /**
     * Loads the system configuration and event details from the database.
     *
     * @param configuration the TicketPoolConfiguration object to populate
     * @return true if configuration is successfully loaded, false otherwise
     */
    public boolean loadConfiguration(TicketPoolConfiguration configuration) {
        String configQuery = "SELECT * FROM system_config ORDER BY id DESC LIMIT 1";
        String eventQuery = "SELECT * FROM events ORDER BY event_id";

        try (Connection conn = getConnection();
             PreparedStatement configStmt = conn.prepareStatement(configQuery);
             PreparedStatement eventStmt = conn.prepareStatement(eventQuery)) {

            // Load system configuration
            try (ResultSet rs = configStmt.executeQuery()) {
                if (!rs.next()) {
                    return false; // No configuration found
                }

                configuration.setTotalTickets(rs.getInt("total_tickets"));
                configuration.setTicketReleaseRate(rs.getInt("release_rate"));
                configuration.setCustomerRetrievalRate(rs.getInt("retrieval_rate"));
                configuration.setMaxTicketCapacity(rs.getInt("max_capacity"));
            }

            // Load event details
            try (ResultSet rs = eventStmt.executeQuery()) {
                while (rs.next()) {
                    configuration.addEventPrice(
                            rs.getString("event_name"),
                            rs.getDouble("event_price")
                    );
                }
            }

            configuration.setConfigured(true);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading configuration", e);
            return false;
        }
    }

    /**
     * Logs a transaction into the database.
     *
     * @param eventName    the name of the event
     * @param ticketPrice  the price of the ticket
     * @param vendorId     the ID of the vendor
     * @param customerId   the ID of the customer
     * @param ticketCount  the number of tickets involved in the transaction
     */
    public void logTransaction(String eventName, double ticketPrice,
                               int vendorId, int customerId, int ticketCount) {
        String query = "INSERT INTO transactions " +
                "(event_name, ticket_price, vendor_id, customer_id, ticket_count) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, eventName);
            stmt.setDouble(2, ticketPrice);
            stmt.setInt(3, vendorId);
            stmt.setInt(4, customerId);
            stmt.setInt(5, ticketCount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error logging transaction", e);
            throw new RuntimeException("Transaction logging failed", e);
        }
    }

    /**
     * Clears all transactions from the transactions table and resets the auto-increment counter.
     */
    public void clearTransactionsTable() {
        String clearQuery = "DELETE FROM transactions";
        String resetAutoIncrementQuery = "ALTER TABLE transactions AUTO_INCREMENT = 1";

        try (Connection conn = getConnection();
             PreparedStatement clearStmt = conn.prepareStatement(clearQuery);
             PreparedStatement resetStmt = conn.prepareStatement(resetAutoIncrementQuery)) {

            conn.setAutoCommit(false);  // Begin transaction
            clearStmt.executeUpdate();
            resetStmt.executeUpdate();
            conn.commit();  // Commit transaction
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing transactions table", e);
            throw new RuntimeException("Transactions table clear failed", e);
        }
    }
}