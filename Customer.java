import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Customer {
    
    public void register() {
        System.out.println();
        System.out.println("########## Registering New User ##########");
        System.out.println("Please fill out the following fields.");

        Scanner in = new Scanner(System.in);
        System.out.print("Username: ");
        String username = in.nextLine();
        while (StarsRUs.db.isUser(username)) {
            System.out.println("Username " + username + " already exists!");
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
        while (!(validateFormat(tax, "#########")) || StarsRUs.db.isTax(tax)) {
            if (!(validateFormat(tax, "#########"))) {
                System.out.println("Invalid format!");
            }
            else if (StarsRUs.db.isTax(tax)) {
                System.out.println("A customer with this tax ID is already registered!");
            }
            System.out.print("Tax ID: ");
            tax = in.nextLine();
        }
        System.out.print("Please specify amount (min $1000) to deposit into market account or type \'q\' to quit: ");
        String amount = in.nextLine();
        while (true) {
            if (amount.equals("q")) {
                System.out.println("Registration failed!");
                return;
            }
            else if (!(Pattern.compile("\\d+[.]\\d\\d").matcher(amount).matches())) {
                System.out.println("Invalid format!");
                System.out.print("Please specify amount (min $1000) to deposit into market account or type \'q\' to quit: ");
                amount = in.nextLine();
            }
            else if (Double.parseDouble(amount) < 1000) {
                System.out.println("Initial deposit must be at least $1000!");
                System.out.print("Please specify amount (min $1000) to deposit into market account or type \'q\' to quit: ");
                amount = in.nextLine();
            }
            else {
                break;
            }
        }

        if (StarsRUs.db.register(username, password, name, state, phone, email, tax) && StarsRUs.db.createMarket(amount, username)) {
            System.out.println("Registration successful!");
        }
        else {
            System.out.println("Registration failed!");
        }
    }

    public String login() {
        System.out.println();
        System.out.println("########## Customer Login ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Username: ");
        String username = in.nextLine();
        if (username.equals("q")) return "";
        System.out.print("Password: ");
        String password = in.nextLine();
        if (password.equals("q")) return "";

        while (!(StarsRUs.db.login(username, password))) {
            System.out.println("Invalid username or password! Try again or type \"q\" to quit.");
            System.out.print("Username: ");
            username = in.nextLine();
            if (username.equals("q")) return "";
            System.out.print("Password: ");
            password = in.nextLine();
            if (password.equals("q")) return "";
        }

        return username;
    }

    public void showBalance(String username) {
        System.out.println();
        System.out.println("########## Account Balance of User " + username + " ##########");
        float result = StarsRUs.db.showBalance(username);
        if (result > 0) {
            System.out.printf("$%.2f\n", result);
        }
        else {
            System.out.println("Failed to retrieve balance!");
        }
    }

    public void deposit(String username) {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is closed!");
            return;
        }
        System.out.println();
        System.out.println("########## Depositing Into Account of " + username + " ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Specify amount to deposit or type \"q\" to quit: ");
        String amount = in.nextLine();
        while (!(Pattern.compile("\\d+[.]\\d\\d").matcher(amount).matches())) {
            if (amount.equals("q")) {
                System.out.println("Deposit failed!");
                return;
            }
            System.out.println("Invalid format!");
            System.out.print("Specify amount to deposit or type \"q\" to quit: ");
            amount = in.nextLine();
        }
        if (StarsRUs.db.deposit(Float.parseFloat(amount), username)) {
            System.out.printf("Successfully deposited $%.2f!\n", Float.parseFloat(amount));
        }
        else {
            System.out.println("Deposit failed!");
        }
    }

    public void withdraw(String username) {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is closed!");
            return;
        }
        System.out.println();
        System.out.println("########## Withdrawing From Account of " + username + " ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Specify amount to withdraw or type \"q\" to quit: ");
        String amount = in.nextLine();
        while (!(Pattern.compile("\\d+[.]\\d\\d").matcher(amount).matches()) || !StarsRUs.db.canWithdraw(Float.parseFloat(amount), username)) {
            if (amount.equals("q")) {
                System.out.println("Withdraw failed!");
                return;
            }
            if (!(Pattern.compile("\\d+[.]\\d\\d").matcher(amount).matches())) {
                System.out.println("Invalid format!");
            }
            else if (!StarsRUs.db.canWithdraw(Float.parseFloat(amount), username)) {
                System.out.println("Insufficient funds!");
            }
            System.out.print("Specify amount to withdraw or type \"q\" to quit: ");
            amount = in.nextLine();
        }
        if (StarsRUs.db.withdraw(Float.parseFloat(amount), username)) {
            System.out.printf("Successfully withdrew $%.2f!\n", Float.parseFloat(amount));
        }
        else {
            System.out.println("Withdraw failed!");
        }
    }

    public void movie() {
        System.out.println();
        System.out.println("########## Movie Information ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the name of the movie or type \"q\" to quit: ");
        String name = in.nextLine();
        if (name.equals("q")) {
            return;
        }
        System.out.print("Enter the year of the movie or type \"q\" to quit: ");
        String year = in.nextLine();
        if (year.equals("q")) {
            return;
        }

        boolean good = validateFormat(year, "####") && StarsRUs.db.isMovie(name, Integer.parseInt(year));
        while (!good) {
            System.out.println("A movie by this name and year does not exist in the database!");
            System.out.print("Enter the name of the movie or type \"q\" to quit: ");
            name = in.nextLine();
            if (name.equals("q")) {
                return;
            }
            System.out.print("Enter the year of the movie or type \"q\" to quit: ");
            year = in.nextLine();
            if (year.equals("q")) {
                return;
            }
            good = validateFormat(year, "####") && StarsRUs.db.isMovie(name, Integer.parseInt(year));
        }

        ArrayList<String> temp = StarsRUs.db.movie(name, Integer.parseInt(year));
        if (!temp.isEmpty()) {
            System.out.println("Title: " + temp.get(0));
            System.out.println("Year: " + temp.get(1));
            System.out.println("Rating: " + temp.get(2) + "/10.0");
            System.out.println("List of Actors/Directors: ");
            for (int i = 3; i < temp.size(); i++) {
                System.out.println("    " + temp.get(i));
            }
        }
        else {
            System.out.println("Failed to retrieve data for movie " + name + "!");
        }
    }

    public void review() {
        System.out.println();
        System.out.println("########## Movie Reviews ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the name of the movie or type \"q\" to quit: ");
        String name = in.nextLine();
        if (name.equals("q")) {
            return;
        }
        System.out.print("Enter the year of the movie or type \"q\" to quit: ");
        String year = in.nextLine();
        if (year.equals("q")) {
            return;
        }

        boolean good = validateFormat(year, "####") && StarsRUs.db.isMovie(name, Integer.parseInt(year));
        while (!good) {
            System.out.println("A movie by this name and year does not exist in the database!");
            System.out.print("Enter the name of the movie or type \"q\" to quit: ");
            name = in.nextLine();
            if (name.equals("q")) {
                return;
            }
            System.out.print("Enter the year of the movie or type \"q\" to quit: ");
            year = in.nextLine();
            if (year.equals("q")) {
                return;
            }
            good = validateFormat(year, "####") && StarsRUs.db.isMovie(name, Integer.parseInt(year));
        }

        ArrayList<String> temp = StarsRUs.db.review(name, Integer.parseInt(year));
        if (!temp.isEmpty()) {
            System.out.println("List of reviews for movie " + name + ": ");
            for (int i = 0; i < temp.size(); i++) {
                System.out.println("    " + temp.get(i));
            }
        }
        else {
            System.out.println("No reviews found for movie " + name + "!");
        }
    }

    public void topRated() {
        System.out.println();
        System.out.println("########## Movies in Time Range ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a start date or type \"q\" to quit: ");
        String start = in.nextLine();
        while (!validateFormat(start, "####")) {
            if (start.equals("q")) {
                return;
            }
            System.out.println("Invalid format!");
            System.out.print("Enter a start date or type \"q\" to quit: ");
            start = in.nextLine();
        }
        System.out.print("Enter an end date or type \"q\" to quit: ");
        String end = in.nextLine();

        boolean good = validateFormat(end, "####") && (Integer.parseInt(start) < Integer.parseInt(end));
        while (!good) {
            if (end.equals("q")) {
                return;
            }
            if (!validateFormat(end, "####")) {
                System.out.println("Invalid format!");
            }
            else if (Integer.parseInt(start) > Integer.parseInt(end)) {
                System.out.println("End date cannot be earlier than start date!");
            }
            System.out.print("Enter an end date or type \"q\" to quit: ");
            end = in.nextLine();
            good = validateFormat(end, "####") && (Integer.parseInt(start) < Integer.parseInt(end));
        }

        ArrayList<String> temp = StarsRUs.db.topRated(Integer.parseInt(start), Integer.parseInt(end));
        if (!temp.isEmpty()) {
            System.out.println("Top movies between " + start + " and " + end + ": ");
            for (int i = 0; i < temp.size(); i++) {
                System.out.println("    " + temp.get(i));
            }
        }
        else {
            System.out.println("No movies found between " + start + " and " + end + "!");
        }
    }

    public void stock() {
        System.out.println();
        System.out.println("########## Stock Information ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a stock ticker or type \"q\" to quit: ");
        String stock = in.nextLine();

        while (!StarsRUs.db.isStock(stock)) {
            if (stock.equals("q")) {
                return;
            }
            System.out.println("Stock ticker " + stock + " does not exist!");
            System.out.print("Enter a stock ticker or type \"q\" to quit: ");
            stock = in.nextLine();
        }

        String temp = StarsRUs.db.stock(stock);
        if (temp.length() != 0) {
            System.out.println(temp);
        }
        else {
            System.out.println("Failed to retrieve information about ticker " + stock + "!");
        }
    }

    public void history(String username) {
        System.out.println();
        System.out.println("########## Stock Transaction History ##########");

        ArrayList<String> temp = StarsRUs.db.history(username);
        if (!temp.isEmpty()) {
            System.out.println("Transaction history for user " + username + ":");
            for (int i = 0; i < temp.size(); i++) {
                System.out.println("    " + temp.get(i));
            }
        }
        else {
            System.out.println("You have not traded any shares this month!");
        }
    }

    public void buy(String username) {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is closed!");
            return;
        }
        System.out.println();
        System.out.println("########## Purchasing Shares for " + username + " ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a stock ticker or type \"q\" to quit: ");
        String stock = in.nextLine();
        while (!StarsRUs.db.isStock(stock)) {
            if (stock.equals("q")) {
                return;
            }
            System.out.println("Stock ticker " + stock + " does not exist!");
            System.out.print("Enter a stock ticker or type \"q\" to quit: ");
            stock = in.nextLine();
        }

        System.out.print("Enter the number of shares of " + stock + " that you wish to purchase or type \"q\" to quit: ");
        String amount = in.nextLine();
        while (!(Pattern.compile("\\d+[.]\\d\\d\\d").matcher(amount).matches()) || !StarsRUs.db.sufficient(username, stock, Float.parseFloat(amount)) || (Float.parseFloat(amount) == 0)) {
            if (amount.equals("q")) {
                System.out.println("Purchase failed!");
                return;
            }
            if (!(Pattern.compile("\\d+[.]\\d\\d\\d").matcher(amount).matches())) {
                System.out.println("Invalid format!");
            }
            else if (!StarsRUs.db.sufficient(username, stock, Float.parseFloat(amount))) {
                System.out.println("You have insufficient funds to purchase " + amount + " shares of " + stock + "! (including $20 transaction fee)");
            }
            else if (Float.parseFloat(amount) == 0) {
                System.out.println("You cannot buy 0 shares!");
            }
            System.out.print("Enter the number of shares of " + stock + " that you wish to purchase or type \"q\" to quit: ");
            amount = in.nextLine();
        }

        float temp = StarsRUs.db.buy(username, stock, Float.parseFloat(amount));
        if (temp > 0) {
            System.out.println("Successfully bought " + amount + " shares of " + stock + " at $" + String.format("%.2f", temp) + "!");
        }
        else {
            System.out.println("Purchase failed!");
        }
    }

    public void sell(String username) {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is closed!");
            return;
        }
        System.out.println();
        System.out.println("########## Selling Shares for " + username + " ##########");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter a stock ticker or type \"q\" to quit: ");
        String stock = in.nextLine();
        while (!StarsRUs.db.isStock(stock)) {
            if (stock.equals("q")) {
                return;
            }
            System.out.println("Stock ticker " + stock + " does not exist!");
            System.out.print("Enter a stock ticker or type \"q\" to quit: ");
            stock = in.nextLine();
        }

        ArrayList<Float> prices = new ArrayList<Float>();
        ArrayList<Float> amounts = new ArrayList<Float>();
        while (true) {
            System.out.print("Enter the price you bought the shares at or type \"c\" to confirm or type \"q\" to quit: ");
            String price = in.nextLine();
            boolean exit = false;
            while (!(Pattern.compile("\\d+[.]\\d\\d").matcher(price).matches()) || prices.contains(Float.parseFloat(price)) || !StarsRUs.db.isPrice(username, stock, Float.parseFloat(price))) {
                if (price.equals("c")) {
                    exit = true;
                    break;
                }
                else if (price.equals("q")) {
                    System.out.println("Sale failed!");
                    return;
                }
                else if (!(Pattern.compile("\\d+[.]\\d\\d").matcher(price).matches())) {
                    System.out.println("Invalid format!");
                    System.out.print("Enter the price you bought the shares at or type \"c\" to confirm or type \"q\" to quit: ");
                    price = in.nextLine();
                }
                else if (!StarsRUs.db.isPrice(username, stock, Float.parseFloat(price))) {
                    System.out.println(String.format("You do not own any shares of %s bought at $%.2f!", stock, Float.parseFloat(price)));
                    System.out.print("Enter the price you bought the shares at or type \"c\" to confirm or type \"q\" to quit: ");
                    price = in.nextLine();
                }
                else {
                    System.out.println("You are already selling shares at this price in this transaction!");
                    System.out.print("Enter the price you bought the shares at or type \"c\" to confirm or type \"q\" to quit: ");
                    price = in.nextLine();
                }
            }
            if (exit) {
                break;
            }
            System.out.print("Enter the amount of shares you wish to sell or type \"c\" to confirm or type \"q\" to quit: ");
            String amount = in.nextLine();
            while (!(Pattern.compile("\\d+[.]\\d\\d\\d").matcher(amount).matches()) || !StarsRUs.db.sufficientAmount(username, stock, Float.parseFloat(price), Float.parseFloat(amount)) || (Float.parseFloat(amount) == 0)) {
                if (amount.equals("c")) {
                    exit = true;
                    break;
                }
                else if (amount.equals("q")) {
                    System.out.println("Sale failed!");
                    return;
                }
                else if (!(Pattern.compile("\\d+[.]\\d\\d\\d").matcher(amount).matches())) {
                    System.out.println("Invalid format!");
                    System.out.print("Enter the amount of shares you wish to sell or type \"c\" to confirm or type \"q\" to quit: ");
                    amount = in.nextLine();
                }
                else if (Float.parseFloat(amount) == 0) {
                    System.out.println("You cannot sell 0 shares!");
                    System.out.print("Enter the amount of shares you wish to sell or type \"c\" to confirm or type \"q\" to quit: ");
                    amount = in.nextLine();
                }
                else {
                    System.out.println(String.format("You do not own enough shares of %s at price $%.2f!", stock, Float.parseFloat(price)));
                    System.out.print("Enter the amount of shares you wish to sell or type \"c\" to confirm or type \"q\" to quit: ");
                    amount = in.nextLine();
                }
            }
            if (exit) {
                break;
            }
            prices.add(Float.parseFloat(price));
            amounts.add(Float.parseFloat(amount));
            System.out.println(String.format("Added %s shares of %s at price $%.2f to sell transaction!", amount, stock, Float.parseFloat(price)));
        }
        if (!StarsRUs.db.canSell(amounts, stock, username)) {
            System.out.println("Sale failed because it would result in a negative balance!");
            return;
        }
        if (!StarsRUs.db.fee(username)) {
            System.out.println("Sale failed!");
            return;
        }
        if (StarsRUs.db.sell(username, stock, prices, amounts)) {
            System.out.println("Successfully sold shares of " + stock + " at the following amounts and buy prices: ");
            for (int i = 0; i < prices.size(); i++) {
                System.out.println(String.format("  %.3f shares bought at $%.2f", amounts.get(i), prices.get(i)));
            }
        }
        else {
            System.out.println("Sale failed!");
        }
    }

    public void cancel(String username) {
        if (!StarsRUs.db.isOpen()) {
            System.out.println("Market is closed!");
            return;
        }
        if (!StarsRUs.db.isCancel(username)) {
            System.out.println("There are no transactions that can be cancelled!");
            return;
        }
        if (!StarsRUs.db.canTransact(username)) {
            System.out.println("Insufficient funds in account to pay transaction fee of $20!");
            return;
        }

        System.out.println();
        System.out.println("########## Cancelling Transaction for " + username + " ##########");
        System.out.println("Are you sure you want to cancel the following transaction: ");
        ArrayList<String[]> result = StarsRUs.db.getCancel(username);
        String temp;
        if (result.size() == 1) {
            if (Float.parseFloat(result.get(0)[1]) > 0) {
                temp = String.format("Bought %s shares of %s at $%s", result.get(0)[1], result.get(0)[0], result.get(0)[2]);
            }
            else {
                temp = String.format("Sold %.3f shares of %s (bought at $%s) at $%s", Math.abs(Float.parseFloat(result.get(0)[1])), result.get(0)[0], result.get(0)[2], result.get(0)[3]);
            }
        }
        else {
            temp = String.format("Sold - %.3f shares of %s (bought at $%s) at $%s", Math.abs(Float.parseFloat(result.get(0)[1])), result.get(0)[0], result.get(0)[2], result.get(0)[3]);
            for (int i = 1; i < result.size(); i++) {
                temp = temp + String.format("\n   |----> %.3f shares of %s (bought at $%s) at $%s", Math.abs(Float.parseFloat(result.get(i)[1])), result.get(i)[0], result.get(i)[2], result.get(i)[3]);
            }
        }
        System.out.println(String.format("   %s", temp));
        Scanner in = new Scanner(System.in);
        System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
        String input = in.nextLine();
        while (true) {
            if (input.equals("c")) {
                break;
            }
            if (input.equals("q")) {
                System.out.println("Cancellation failed!");
                return;
            }
            System.out.println("Invalid input!");
            System.out.print("Type \"c\" to confirm or \"q\" to quit: ");
            input = in.nextLine();
        }

        if (Float.parseFloat(result.get(0)[1]) < 0) {
            float deduct = 20;
            for (int i = 0; i < result.size(); i++) {
                deduct = deduct + Float.parseFloat(result.get(i)[3]) * Float.parseFloat(result.get(i)[1]) * -1;
            }
            if (!StarsRUs.db.canCancel(username, deduct)) {
                System.out.println(String.format("This cancellation would deduct $%.2f from your account, which you do not have!", deduct));
                return;
            }
        }

        if (StarsRUs.db.cancel(Integer.parseInt(result.get(0)[4]))) {
            System.out.println("Cancellation successful!");
        }
        else {
            System.out.println("Cancellation failed!");
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