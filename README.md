# Ticket System CLI 🎫

## Overview

The Real-Time Ticket Handling System is a sophisticated, multi-threaded Java application that simulates a dynamic ticket sales environment. Designed to demonstrate concurrent programming principles, this system allows multiple vendors to add tickets and multiple customers to purchase tickets in real-time, with robust synchronization and transaction logging.

![Java](https://img.shields.io/badge/Java-17+-blue?logo=java&logoColor=white)
![Coverage](https://img.shields.io/badge/coverage-100%25-yellow)
![GitHub last commit](https://img.shields.io/github/last-commit/Pavith19/Ticket-System-CLI/main)

## 🌟 Key Features

- **Multi-threaded Ticket Management**
  - Concurrent ticket addition by vendors
  - Simultaneous ticket purchases by customers
  - Thread-safe operations using Java concurrency utilities

- **Flexible Configuration**
  - Configurable total ticket count
  - Adjustable ticket release and retrieval rates
  - Support for multiple events with dynamic pricing

- **Comprehensive Logging**
  - Detailed transaction logging
  - System state tracking
  - File and console logging

- **Persistent Configuration**
  - Database-backed configuration storage
  - Ability to load and modify system settings

## 🛠 Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher
- **MySQL Database:** Version 8.0+
- **Dependencies:**
  - JDBC MySQL Connector
  - Java Concurrent Utilities

## 📦 Project Structure

```
ticket-system/
│
├── src/
│   ├── Database.java
│   ├── TicketPool.java
│   ├── TicketPoolConfiguration.java
│   ├── TicketSystem.java
│   ├── TicketSystemLogger.java
│   ├── Ticket.java
│   ├── Vendor.java
│   └── Customer.java
│
├── lib/
│   └── mysql-connector-java.jar
│
└── README.md
```

## 🚀 Setup and Installation

### 1. Database Setup

1. Install MySQL
2. Create a database:
   ```sql
   CREATE DATABASE ticketing_systemdb;
   ```
3. Configure database credentials in `Database.java`

### 2. Compile the Application

```bash
# Compile all Java files
javac -cp ".:lib/mysql-connector-java.jar" *.java

# Create a JAR (optional)
jar cvfe TicketSystem.jar TicketSystem *.class
```

### 3. Run the Application

```bash
java -cp ".:lib/mysql-connector-java.jar" TicketSystem
```

## 🎮 Usage Instructions

### System Configuration

1. Launch the application
2. Choose **Option 1: Configure System**
3. Enter system parameters:
   - Total ticket capacity
   - Ticket release rate
   - Customer retrieval rate
   - Event details (name, price)

### Starting the System

1. Select **Option 2: Start Ticket Handling**
2. Vendors automatically begin adding tickets
3. Customers start purchasing tickets
4. Press **Option 3** to stop the system manually

### Additional Options

- **Reset System:** Clear all transactions and reconfigure
- **Exit:** Terminate the application

## 🔍 System Workflow

1. **Configuration Phase:**
   - Define system parameters
   - Specify events and ticket prices

2. **Ticket Addition Phase:**
   - Vendors concurrently add tickets to the pool
   - Tickets are added based on configured release rate

3. **Ticket Purchase Phase:**
   - Customers attempt to purchase tickets
   - Purchases are synchronized and thread-safe
   - System stops when all tickets are sold

## 📊 Logging

- Transactions logged to `ticket_system.log`
- Captures:
  - System configuration
  - Ticket additions
  - Ticket purchases
  - System state changes

## 🛡️ Error Handling

- Robust input validation
- Graceful error management
- Comprehensive logging of system events

## 📝 License

This project is licensed under the MIT License.

<h3 align="center">Connect with me:</h3>
<p align="center">
  <a href="https://instagram.com/_mr_2001__" target="blank"><img align="center" src="https://raw.githubusercontent.com/rahuldkjain/github-profile-readme-generator/master/src/images/icons/Social/instagram.svg" alt="_mr_2001__" height="30" width="40" /></a>
  <a href="https://linkedin.com/in/www.linkedin.com/in/pavith-bambaravanage-465300293" target="blank"><img align="center" src="https://raw.githubusercontent.com/rahuldkjain/github-profile-readme-generator/master/src/images/icons/Social/linked-in-alt.svg" alt="pavith-bambaravanage-465300293" height="25" width="35" /></a>
  <a href="https://www.hackerrank.com/@pavith_db" target="blank"><img align="center" src="https://raw.githubusercontent.com/rahuldkjain/github-profile-readme-generator/master/src/images/icons/Social/hackerrank.svg" alt="@pavith_db" height="40" width="45" /></a>
  <a href="https://www.leetcode.com/pavith_db" target="blank"><img align="center" src="https://raw.githubusercontent.com/rahuldkjain/github-profile-readme-generator/master/src/images/icons/Social/leet-code.svg" alt="pavith_db" height="30" width="40" /></a>
  <a href="mailto:pavithd2020@gmail.com" target="blank"><img align="center" src="https://github.com/TheDudeThatCode/TheDudeThatCode/raw/master/Assets/Gmail.svg" alt="pavithd2020@gmail.com" height="30" width="40" /></a>
</p>



