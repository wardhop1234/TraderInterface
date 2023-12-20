import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.time.*;
import java.time.format.DateTimeParseException;

import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Manager {

    public void createadmin() {
        System.out.println();
        System.out.println("########## Registering New Admin ##########");
        System.out.println("Please fill out the following fields.");

        Scanner in = new Scanner(System.in);
        System.out.print("Username: ");
        String username = in.nextLine();
        while (StarsRUs.db.isAdmin(username)) {
            System.out.println("Admin " + username + " already exists!");
            System.out.print("Username: ");
            username = in.nextLine();
        }
        System.out.print("Password: ");
        String password = in.nextLine();
        System.out.print("Name: ");
        String name = in.nextLine();
        System.out.print("State: ");
        String state = in.nextLine();
        while (!(validateFormat(state, "AA"))) {
            System.out.println("Invalid format!");
            System.out.print("State: ");
            state = in.nextLine();
        }
        System.out.print("Phone Number: ");
        String phone = in.nextLine();
        while (!(validateFormat(phone, "(###)#######"))) {
            System.out.println("Invalid format!");
            System.out.print("Phone Number: ");
            phone = in.nextLine();
        }
        System.out.print("Email: ");
        String email = in.nextLine();
        System.out.print("Tax ID: ");
        String tax = in.nextLine();
        while (!(validateFormat(tax, "#########")) || StarsRUs.db.isAdminTax(tax)) {
            if (!(validateFormat(tax, "#########"))) {
                System.out.println("Invalid format!");
            }
            else if (StarsRUs.db.isAdminTax(tax)) {
                System.out.println("An admin with this tax ID is already registered!");
            }
            System.out.print("Tax ID: ");
            tax = in.nextLine();
        }
        System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
        String input = in.nextLine();
        while (!(input.equals("c") || input.equals("q"))) {
            System.out.println("Invalid input!");
            System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
            input = in.nextLine();
        }
        if (input.equals("c")) {
            if (StarsRUs.db.createadmin(username, password, name, state, phone, email, tax)) {
                System.out.println("Successfully registered manager!");
            }
            else {
                System.out.println("Failed to register!");
            }
        }
        else {
            System.out.println("Failed to register!");
        }
    }

    public boolean admin() {
        System.out.println();
        System.out.println("########## Manager Login ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Username: ");
        String username = in.nextLine();
        if (username.equals("q")) return false;
        System.out.print("Password: ");
        String password = in.nextLine();
        if (password.equals("q")) return false;

        while (!(StarsRUs.db.admin(username, password))) {
            System.out.println("Invalid username or password! Try again or type \"q\" to quit.");
            System.out.print("Username: ");
            username = in.nextLine();
            if (username.equals("q")) return false;
            System.out.print("Password: ");
            password = in.nextLine();
            if (password.equals("q")) return false;
        }
        return true;
    }

    public void open() {
        if (StarsRUs.db.isOpen()) {
            System.out.println("Market is already open!");
            return;
        }
        System.out.println();
        System.out.println("########## Opening Market ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
        String input = in.nextLine();
        while (!(input.equals("c") || input.equals("q"))) {
            System.out.println("Invalid input!");
            System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
            input = in.nextLine();
        }
        if (input.equals("c")) {
            if (StarsRUs.db.open()) {
                System.out.println("Successfully opened market!");
            }
            else {
                System.out.println("Failed to open!");
            }
        }
        else {
            System.out.println("Failed to open!");
        }
    }

    public void close() {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is already closed!");
            return;
        }
        System.out.println();
        System.out.println("########## Closing Market ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
        String input = in.nextLine();
        while (!(input.equals("c") || input.equals("q"))) {
            System.out.println("Invalid input!");
            System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
            input = in.nextLine();
        }
        if (input.equals("c")) {
            if (StarsRUs.db.close()) {
                System.out.println("Successfully closed market!");
            }
            else {
                System.out.println("Failed to close!");
            }
        }
        else {
            System.out.println("Failed to close!");
        }
    }

    public void date() {
        if (StarsRUs.db.isOpen()) {
            System.out.println("Cannot change date while market is open!");
            return;
        }
        System.out.println();
        System.out.println("########## Changing Current Date ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the new date or type \"q\" to quit: ");
        String date = in.nextLine();
        while (!validDate(date)) {
            if (date.equals("q")) {
                System.out.println("Date update failed!");
                return;
            }
            System.out.println("Invalid input!");
            System.out.print("Enter the new date or type \"q\" to quit: ");
            date = in.nextLine();
        }

        if (StarsRUs.db.date(date)) {
            System.out.println("Successfully changed date to " + date + "!");
        }
        else {
            System.out.println("Date update failed!");
        }
    }

    public void stock() {
        System.out.println();
        System.out.println("########## Setting Stock Price ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a stock ticker or type \"q\" to quit: ");
        String stock = in.nextLine();
        while (!StarsRUs.db.isStock(stock)) {
            if (stock.equals("q")) {
                System.out.println("Price update failed!");
                return;
            }
            System.out.println("Stock ticker " + stock + " does not exist!");
            System.out.print("Enter a stock ticker or type \"q\" to quit: ");
            stock = in.nextLine();
        }

        System.out.print("Enter a new price for " + stock + " or type \"q\" to quit: ");
        String price = in.nextLine();
        while (!(Pattern.compile("\\d+[.]\\d\\d").matcher(price).matches())) {
            if (price.equals("q")) {
                System.out.println("Price update failed!");
                return;
            }
            System.out.println("Invalid format!");
            System.out.print("Enter a new price for " + stock + " or type \"q\" to quit: ");
            price = in.nextLine();
        }

        if (StarsRUs.db.updateStock(stock, Float.parseFloat(price))) {
            System.out.println(String.format("Successfully updated %s's price to $%s!", stock, price));
        }
        else {
            System.out.println("Price update failed!");
        }
    }

    public void customer() {
        System.out.println();
        System.out.println("########## Customer Information ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a username or type \"q\" to quit: ");
        String user = in.nextLine();

        while (!StarsRUs.db.isUser(user)) {
            if (user.equals("q")) {
                return;
            }
            System.out.println("User " + user + " does not exist!");
            System.out.print("Enter a username or type \"q\" to quit: ");
            user = in.nextLine();
        }

        ArrayList<String> temp = StarsRUs.db.customer(user);
        if (!temp.isEmpty()) {
            System.out.println("Market account balance for user " + user + ": " + temp.get(0));
            if (temp.size() > 1) {
                System.out.println("Stock accounts for user " + user + ": ");
                for (int i = 1; i < temp.size(); i++) {
                    System.out.println("    " + temp.get(i));
                }
            }
            else {
                System.out.println("User " + user + " does not own any shares!");
            }
        }
        else {
            System.out.println("No accounts found associated with user " + user + "!");
        }
    }

    public void statement() {
        System.out.println();
        System.out.println("########## Monthly Statement Information ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a username or type \"q\" to quit: ");
        String user = in.nextLine();

        while (!StarsRUs.db.isUser(user)) {
            if (user.equals("q")) {
                return;
            }
            System.out.println("User " + user + " does not exist!");
            System.out.print("Enter a username or type \"q\" to quit: ");
            user = in.nextLine();
        }

        ArrayList<String> result = StarsRUs.db.getInfo(user);
        if (!result.isEmpty()) {
            System.out.println("Information about user " + user + ": ");
            System.out.println("Name: " + result.get(0));
            System.out.println("Email: " + result.get(1));
            System.out.println("Initial Balance: " + result.get(2));
            System.out.println("Current Balance: " + result.get(3));
            System.out.println("Total Earnings: " + result.get(4));
            System.out.println("Commissions Paid: " + result.get(5));
        }
        else {
            System.out.println("Failed to retrieve information about " + user + "!");
        }

        ArrayList<String> transactions = StarsRUs.db.getTransactions(user);
        if (!transactions.isEmpty()) {
            System.out.println("Displaying transactions for user " + user + ": ");
            for (int i = 0; i < transactions.size(); i++) {
                System.out.println("    " + transactions.get(i));
            }
        }
        else {
            System.out.println("User " + user + " has made no transactions!");
        }
    }

    public void set() {
        System.out.println();
        System.out.println("########## Setting Monthly Interest Rate ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the interest rate as a percentage or type \"q\" to quit: ");
        String input = in.nextLine();
        boolean good = false;
        while (!good) {
            if (input.equals("q")) {
                System.out.println("Failed to update interest rate!");
                return;
            }
            try {
                Float.parseFloat(input);
                good = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid format!");
                System.out.print("Enter the interest rate as a percentage or type \"q\" to quit: ");
                input = in.nextLine();
            }
        }

        float rate = Float.parseFloat(input) / 100;
        if (StarsRUs.db.set(rate)) {
            System.out.println("Successfully set interest rate to " + input + "%!");
        }
        else {
            System.out.println("Failed to update interest rate!");
        }
    }

    public void interest() {
        if (StarsRUs.db.isOpen()) {
            System.out.println("Cannot add interest while market is open!");
            return;
        }
        if (!StarsRUs.db.endOfMonth()) {
            System.out.println("Can only add interest on last day of month!");
            return;
        }
        if (StarsRUs.db.addedInterest()) {
            System.out.println("You already added interest for this month!");
            return;
        }
        System.out.println();
        System.out.println("########## Adding Interest to Customer Accounts ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
        String input = in.nextLine();
        while (!(input.equals("c") || input.equals("q"))) {
            System.out.println("Invalid input!");
            System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
            input = in.nextLine();
        }
        if (input.equals("c")) {
            if (StarsRUs.db.interest()) {
                System.out.println("Successfully added interest!");
            }
            else {
                System.out.println("Failed to add interest!");
            }
        }
        else {
            System.out.println("Failed to add interest!");
        }
    }

    public void tax() {
        System.out.println();
        System.out.println("########## Customers Above 10k ##########");

        ArrayList<String> temp = StarsRUs.db.tax();
        if (!temp.isEmpty()) {
            for (int i = 0; i < temp.size(); i++) {
                System.out.println(temp.get(i));
            }
        }
        else {
            System.out.println("No customers earned more than $10000 this month!");
        }
    }

    public void active() {
        System.out.println();
        System.out.println("########## Active Customers ##########");

        ArrayList<String> temp = StarsRUs.db.active();
        if (!temp.isEmpty()) {
            for (int i = 0; i < temp.size(); i++) {
                System.out.println(temp.get(i));
            }
        }
        else {
            System.out.println("There are no active customers!");
        }
    }

    public boolean validDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean validateFormat(String str, String format) {
        String regex = format.replaceAll("-", "[-]")
                             .replaceAll("#", "[0-9]")
                             .replaceAll("A", "[A-Z]")
                             .replaceAll("a", "[a-z]")
                             .replaceAll("\\(", "\\\\(")
                             .replaceAll("\\)", "\\\\)");

        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(str).matches();
    }
}