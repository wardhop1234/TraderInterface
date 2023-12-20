import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Scanner;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;

// javac -cp .:ojdbc11-full/ojdbc11.jar *.java
// java -cp .:ojdbc11-full/ojdbc11.jar StarsRUs

public class StarsRUs {

    static Database db;
    static Customer customer;
    static Manager manager;

    public static void customerPage(String username) {
        Scanner in = new Scanner(System.in);
        String input = "";
        while (true) {
            System.out.println();
            System.out.println("########## StarsRUs Customer Interface ##########");
            System.out.print("Enter command: ");
            input = in.nextLine();
            switch (input) {
                case "status":
                    customer.showBalance(username);
                    break;
                case "deposit":
                    customer.deposit(username);
                    break;
                case "withdraw":
                    customer.withdraw(username);
                    break;
                case "movies":
                    customer.movie();
                    break;
                case "reviews":
                    customer.review();
                    break;
                case "best":
                    customer.topRated();
                    break;
                case "stock":
                    customer.stock();
                    break;
                case "history":
                    customer.history(username);
                    break;
                case "buy":
                    customer.buy(username);
                    break;
                case "sell":
                    customer.sell(username);
                    break;
                case "cancel":
                    customer.cancel(username);
                    break;
                case "logout":
                    return;
                case "help":
                    System.out.println();
                    System.out.println("########## Customer Page Commands ##########:");
                    System.out.println("\"status\" - show current account balance");
                    System.out.println("\"deposit\" - deposit money into account");
                    System.out.println("\"withdraw\" - withdraw money from account");
                    System.out.println("\"movies\" - list information for a specific movie");
                    System.out.println("\"reviews\" - list reviews for a specific movie");
                    System.out.println("\"best\" - list top rated movies in a given time interval");
                    System.out.println("\"stock\" - show information related to given stock ticker");
                    System.out.println("\"history\" - show transaction history for shares traded");
                    System.out.println("\"buy\" - purchase a certain number of shares");
                    System.out.println("\"sell\" - sell a certain number of shares");
                    System.out.println("\"cancel\" - cancel the last transaction");
                    System.out.println("\"logout\" - logout and return to home page");
                    break;
                default:
                    System.out.println("Invalid input. For list of valid inputs, type \"help\".");
            }
        }
    }

    public static void managerPage() {
        Scanner in = new Scanner(System.in);
        String input = "";
        while (true) {
            System.out.println();
            System.out.println("########## StarsRUs Manager Interface ##########");
            System.out.print("Enter command: ");
            input = in.nextLine();
            switch (input) {
                case "open":
                    manager.open();
                    break;
                case "close":
                    manager.close();
                    break;
                case "date":
                    manager.date();
                    break;
                case "stock":
                    manager.stock();
                    break;
                case "customer":
                    manager.customer();
                    break;
                case "statement":
                    manager.statement();
                    break;
                case "set":
                    manager.set();
                    break;
                case "interest":
                    manager.interest();
                    break;
                case "tax":
                    manager.tax();
                    break;
                case "active":
                    manager.active();
                    break;
                case "logout":
                    return;
                case "help":
                    System.out.println();
                    System.out.println("########## Manager Page Commands ##########:");
                    System.out.println("\"open\" - open the market");
                    System.out.println("\"close\" - close the market");
                    System.out.println("\"date\" - advance the system date by a certain number of days");
                    System.out.println("\"stock\" - update the price for a stock");
                    System.out.println("\"customer\" - list information regarding a specific customer");
                    System.out.println("\"statement\" - generate a monthly statement for a specific customer");
                    System.out.println("\"rate\" - set the monthly interest rate");
                    System.out.println("\"interest\" - add interest to all market accounts");
                    System.out.println("\"tax\" - list all customers who have earned more than $10000 this month");
                    System.out.println("\"active\" - list all active customers");
                    System.out.println("\"logout\" - logout and return to home page");
                    break;
                default:
                    System.out.println("Invalid input. For list of valid inputs, type \"help\".");
            }
        }
    }

    public static void homePage() {
        Scanner in = new Scanner(System.in);
        String input = "";
        while (true) {
            System.out.println();
            System.out.println("########## StarsRUs Home Page ##########");
            System.out.print("Enter command, type \"q\" to quit: ");
            input = in.nextLine();
            switch (input) {
                case "register":
                    customer.register();
                    break;
                case "login":
                    String temp = customer.login();
                    if (!temp.equals("")) {
                        customerPage(temp);
                    }
                    else {
                        System.out.println("Login failed!");
                    }
                    break;
                case "createadmin":
                    manager.createadmin();
                    break;
                case "admin":
                    if (manager.admin()) {
                        managerPage();
                    }
                    else {
                        System.out.println("Login failed!");
                    }
                    break;
                case "help":
                    System.out.println();
                    System.out.println("########## Home Page Commands ##########:");
                    System.out.println("\"register\" - register an account");
                    System.out.println("\"login\" - login to an existing account");
                    System.out.println("\"createadmin\" - register an admin account");
                    System.out.println("\"admin\" - login to manager interface");
                    System.out.println("\"q\" - quit");
                    break;
                case "q":
                    System.out.println("Thank you for visiting StarsRUs. Have a great day!");
                    return;
                default:
                    System.out.println("Invalid input. For list of valid inputs, type \"help\".");
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            db = new Database();
            customer = new Customer();
            manager = new Manager();
            homePage();
        }
        finally {
            db.quit();
        }
    }
}