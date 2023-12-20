import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Date;

import java.util.ArrayList;
import java.util.Properties;

import java.time.*;

import java.lang.Math;

import java.text.DecimalFormat;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;

public class Database {
    final static String url = "jdbc:oracle:thin:@starsrus_high?TNS_ADMIN=Wallet_starsrus";
    final static String username = "";
    final static String password = "";

    OracleConnection connection;

    public Database() throws SQLException {
        Properties info = new Properties();

        System.out.println("Initializing connection properties...");
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, username);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, password);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.out.println("Creating OracleDataSource...");
        OracleDataSource ods = new OracleDataSource();

        System.out.println("Setting connection properties...");
        ods.setURL(url);
        ods.setConnectionProperties(info);

        try {
            connection = (OracleConnection) ods.getConnection();
            System.out.println("Connection established!");

            // Get JDBC driver name and version
            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());

            // Print some connection properties
            System.out.println(
            "Default Row Prefetch Value: " + connection.getDefaultRowPrefetch()
            );

            System.out.println("Database username: " + connection.getUserName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isOpen() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT MARKETOPEN FROM MARKETSTATUS")) {
                if (resultSet.next()) {
                    if (resultSet.getInt("marketopen") == 1) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUser(String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isTax(String tax) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS WHERE taxid = '%s'", tax);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register(String username, String password, String name, String state, String phone, String email, String taxID) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("INSERT INTO CUSTOMERS VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')", username, password, email, name, state, phone, taxID);
            statement.executeQuery(query);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createMarket(String amount, String username) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT MAX(ID) AS maxID FROM ACCOUNTS")) {
                int id;
                if (!resultSet.next()) {
                    String query = String.format("INSERT INTO ACCOUNTS VALUES (1)");
                    statement.executeQuery(query);
                    id = 1;
                }
                else {
                    int max = resultSet.getInt("maxID");
                    String query = String.format("INSERT INTO ACCOUNTS VALUES ('%d')", max + 1);
                    statement.executeQuery(query);
                    id = max + 1;
                }
                String query = String.format("INSERT INTO MARKETACCOUNTS VALUES ('%d', '%f', '%s', 0, 0, 0, '%f')", id, Float.parseFloat(amount), username, Float.parseFloat(amount));
                statement.executeQuery(query);

                String date = "";
                try (ResultSet tempSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                    tempSet.next();
                    date = date + tempSet.getDate("recent");
                }

                int tid;
                try (ResultSet tempSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                    tempSet.next();
                    tid = tempSet.getInt("total") + 1;
                }
                try (ResultSet tempSet = statement.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                    tempSet.next();
                    tid = tid + tempSet.getInt("total");
                }
                query = String.format("INSERT INTO MARKETTRANSACTIONS VALUES ('%f', '%d', '%d', 0, ?)", Float.parseFloat(amount), id, tid);
                try (PreparedStatement prepState = connection.prepareStatement(query)) {
                    java.sql.Date temp = java.sql.Date.valueOf(date);
                    prepState.setDate(1, temp);
                    prepState.executeUpdate();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(String username, String password) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS WHERE username = '%s' AND password = '%s'", username, password);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public float showBalance(String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return resultSet.getFloat("balance");
                }
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deposit(float amount, String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("UPDATE MARKETACCOUNTS SET balance = balance + '%f' WHERE username = '%s'", amount, username);
            statement.executeQuery(query);

            query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            int id;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }

            String date = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                date = date + resultSet.getDate("recent");
            }

            int tid;
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                resultSet.next();
                tid = resultSet.getInt("total") + 1;
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                resultSet.next();
                tid = tid + resultSet.getInt("total");
            }
            query = String.format("INSERT INTO MARKETTRANSACTIONS VALUES ('%f', '%d', '%d', 0, ?)", amount, id, tid);
            try (PreparedStatement prepState = connection.prepareStatement(query)) {
                java.sql.Date temp = java.sql.Date.valueOf(date);
                prepState.setDate(1, temp);
                prepState.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean canWithdraw(float amount, String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                return resultSet.getFloat("balance") >= amount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean withdraw(float amount, String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    query = String.format("UPDATE MARKETACCOUNTS SET balance = balance - '%f' WHERE username = '%s'", amount, username);
                    statement.executeQuery(query);

                    query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
                    int id;
                    try (ResultSet tempSet = statement.executeQuery(query)) {
                        tempSet.next();
                        id = tempSet.getInt("id");
                    }

                    String date = "";
                    try (ResultSet tempSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                        tempSet.next();
                        date = date + tempSet.getDate("recent");
                    }

                    int tid;
                    try (ResultSet tempSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                        tempSet.next();
                        tid = tempSet.getInt("total") + 1;
                    }
                    try (ResultSet tempSet = statement.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                        tempSet.next();
                        tid = tid + tempSet.getInt("total");
                    }
                    query = String.format("INSERT INTO MARKETTRANSACTIONS VALUES ('%f', '%d', '%d', 0, ?)", -1 * amount, id, tid);
                    try (PreparedStatement prepState = connection.prepareStatement(query)) {
                        java.sql.Date tempDate = java.sql.Date.valueOf(date);
                        prepState.setDate(1, tempDate);
                        prepState.executeUpdate();
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isMovie(String name, int year) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MOVIES WHERE title = '%s' AND year = '%d'", name, year);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> movie(String name, int year) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MOVIES WHERE title = '%s' AND year = '%d'", name, year);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    result.add(resultSet.getString("title"));
                    result.add(resultSet.getString("year"));
                    result.add(String.format("%.1f", resultSet.getFloat("rating")));
                }
            }
            query = String.format("SELECT * FROM CONTRACTS, ACTDIRECT WHERE CONTRACTS.title = '%s' AND CONTRACTS.year = '%d' AND CONTRACTS.symbol = ACTDIRECT.symbol", name, year);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("name").trim() + ": " + resultSet.getString("role").trim() + String.format(" ($%.2f)", resultSet.getFloat("value")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> review(String name, int year) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM REVIEWS WHERE title = '%s' AND year = '%d'", name, year);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("review").trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> topRated(int start, int end) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MOVIES WHERE year >= '%d' AND year <= '%d' AND rating = 10", start, end);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("title").trim() + " (" + resultSet.getInt("year") + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isStock(String stock) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM ACTDIRECT WHERE symbol = '%s'", stock);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String stock(String stock) {
        String result = "";
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM ACTDIRECT WHERE symbol = '%s'", stock);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    result = String.format("Symbol: %s\nName: %s\nPrice: $%.2f\nDate of birth: ", resultSet.getString("symbol"), resultSet.getString("name").trim(), resultSet.getFloat("price"));
                    result = result + resultSet.getDate("dateofbirth");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> history(String user) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE USERNAME = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }
            query = String.format("SELECT * FROM STOCKTRANSACTIONS WHERE MARKETID = '%d' ORDER BY TID", id);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                int prev = 0;
                while (resultSet.next()) {
                    float change = resultSet.getFloat("shareschanged");
                    String amount = String.format("%.3f", Math.abs(change));
                    int curr = resultSet.getInt("tid");
                    String temp;
                    if (curr == prev) {
                        temp = String.format("|----> %s shares of %s (bought at $%.2f) at $%.2f on ", amount, resultSet.getString("ticker"), resultSet.getFloat("priceprev"), resultSet.getFloat("pricecurr"));
                        temp = temp + resultSet.getDate("dot");
                        if (resultSet.getInt("cancelled") == 1) {
                            temp = temp + " (cancelled)";
                        }
                    }
                    else if (change > 0) {
                        temp = String.format("Bought - %s shares of %s at $%.2f on ", amount, resultSet.getString("ticker"), resultSet.getFloat("priceprev"));
                        temp = temp + resultSet.getDate("dot");
                        if (resultSet.getInt("cancelled") == 1) {
                            temp = temp + " (cancelled)";
                        }
                    }
                    else if (change < 0) {
                        temp = String.format("Sold - %s shares of %s (bought at $%.2f) at $%.2f on ", amount, resultSet.getString("ticker"), resultSet.getFloat("priceprev"), resultSet.getFloat("pricecurr"));
                        temp = temp + resultSet.getDate("dot");
                        if (resultSet.getInt("cancelled") == 1) {
                            temp = temp + " (cancelled)";
                        }
                    }
                    else {
                        temp = String.format("Cancelled a previous stock transaction");
                    }
                    prev = curr;
                    result.add(temp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean sufficient(String username, String stock, float amount) {
        try (Statement statement = connection.createStatement()) {
            float total;
            String query = String.format("SELECT * FROM ACTDIRECT WHERE SYMBOL = '%s'", stock);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                total = amount * resultSet.getFloat("price") + 20;
            }
            query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                return resultSet.getFloat("balance") >= total;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public float buy(String username, String stock, float amount) {
        float result = 0;
        try (Statement statement = connection.createStatement()) {
            float total;
            float curr;
            String query = String.format("SELECT * FROM ACTDIRECT WHERE SYMBOL = '%s'", stock);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                curr = resultSet.getFloat("price");
                result = curr;
                total = amount * curr + 20;
            }
            String date = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                date = date + resultSet.getDate("recent");
            }
            query = String.format("UPDATE MARKETACCOUNTS SET balance = balance - '%f', sharestraded = sharestraded + '%f' WHERE username = '%s'", total, amount, username);
            statement.executeQuery(query);
            int market;
            query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                market = resultSet.getInt("id");
            }
            query = String.format("SELECT * FROM STOCKACCOUNTS WHERE id = '%d' AND ticker = '%s'", market, stock);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (!resultSet.next()) {
                    int id;
                    try (ResultSet resultSetTemp = statement.executeQuery("SELECT MAX(ID) AS maxID FROM ACCOUNTS")) {
                        if (!resultSetTemp.next()) {
                            query = String.format("INSERT INTO ACCOUNTS VALUES (1)");
                            statement.executeQuery(query);
                            id = 1;
                        }
                        else {
                            int max = resultSetTemp.getInt("maxID");
                            query = String.format("INSERT INTO ACCOUNTS VALUES ('%d')", max + 1);
                            statement.executeQuery(query);
                            id = max + 1;
                        }
                    }
                    query = String.format("INSERT INTO STOCKACCOUNTS VALUES ('%s', '%f', '%d', '%d')", stock, amount, market, id);
                    statement.executeQuery(query);
                }
                else {
                    query = String.format("UPDATE STOCKACCOUNTS SET shares = shares + '%f' WHERE id = '%d' AND ticker = '%s'", amount, market, stock);
                    statement.executeQuery(query);
                }
            }
            query = String.format("SELECT * FROM STOCKACCOUNTBALANCES WHERE ticker = '%s' AND id = '%d' AND price = '%f'", stock, market, curr);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (!resultSet.next()) {
                    query = String.format("INSERT INTO STOCKACCOUNTBALANCES VALUES ('%s', '%f', '%f', '%d')", stock, amount, curr, market);
                    statement.executeQuery(query);
                }
                else {
                    query = String.format("UPDATE STOCKACCOUNTBALANCES SET shares = shares + '%f' WHERE ticker = '%s' AND id = '%d' AND price = '%f'", amount, stock , market, curr);
                    statement.executeQuery(query);
                }
            }
            int id;
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                resultSet.next();
                id = resultSet.getInt("total") + 1;
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                resultSet.next();
                id = id + resultSet.getInt("total");
            }
            query = String.format("INSERT INTO STOCKTRANSACTIONS VALUES ('%s', '%d', '%f', '%f', '%f', '%d', 0, ?)", stock, market, amount, curr, curr, id);
            try (PreparedStatement prepState = connection.prepareStatement(query)) {
                java.sql.Date temp = java.sql.Date.valueOf(date);
                prepState.setDate(1, temp);
                prepState.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean canTransact(String user) {
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                return resultSet.getFloat("balance") >= 20;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sufficientAmount(String user, String stock, float price, float amount) {
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }
            query = String.format("SELECT * FROM STOCKACCOUNTBALANCES WHERE ticker = '%s' AND shares >= '%f' AND price = '%f' AND id = '%d'", stock, amount, price, id);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPrice(String user, String stock, float price) {
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }
            query = String.format("SELECT * FROM STOCKACCOUNTBALANCES WHERE ticker = '%s' AND price = '%f' AND id = '%d'", stock, price, id);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean canSell(ArrayList<Float> amounts, String stock, String user) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM ACTDIRECT WHERE symbol = '%s'", stock);
            float price;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                price = resultSet.getFloat("price");
            }
            query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            float balance;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                balance = resultSet.getFloat("balance");
            }
            float total = 0;
            for (int i = 0; i < amounts.size(); i++) {
                total = total + amounts.get(i) * price;
            }
            return balance + total - 20 >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean fee(String user) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("UPDATE MARKETACCOUNTS SET balance = balance - 20 WHERE username = '%s'", user);
            statement.executeQuery(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sell(String user, String stock, ArrayList<Float> prices, ArrayList<Float> amounts) {
        try (Statement statement = connection.createStatement()) {
            int tid;
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                resultSet.next();
                tid = resultSet.getInt("total") + 1;
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                resultSet.next();
                tid = tid + resultSet.getInt("total");
            }
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }
            for (int i = 0; i < prices.size(); i++) {
                query = """
                    DECLARE
                        stockTid NUMBER := ?;
                        marketId NUMBER := ?;
                        buyPrice Float := ?;
                        stockPrice Float;
                        amount Float := ?;
                        stock CHAR(3) := ?;
                        countAccountBalances NUMBER;
                        countAccount NUMBER;
                        recentDate Date;

                    BEGIN
                        SELECT M.recent into recentDate
                        FROM MARKETSTATUS M;

                        SELECT MAX(A.PRICE) into stockPrice
                        FROM ACTDIRECT A
                        WHERE A.SYMBOL = stock;

                        INSERT INTO StockTransactions
                        VALUES (stock, marketId,-1*amount,buyPrice,stockPrice,stockTid,0,recentDate );

                        UPDATE MARKETACCOUNTS
                        SET BALANCE = BALANCE + stockPrice*amount
                        WHERE id = marketId;

                        UPDATE MARKETACCOUNTS
                        SET SHARESTRADED = SHARESTRADED + amount
                        WHERE id = marketId;

                        UPDATE MARKETACCOUNTS
                        SET PROFIT = PROFIT + stockPrice*amount - buyPrice*amount
                        WHERE id = marketId;

                        UPDATE STOCKACCOUNTS
                        SET SHARES = SHARES - amount
                        WHERE ID=marketId AND TICKER=stock;

                        UPDATE STOCKACCOUNTBALANCES
                        SET SHARES = SHARES-amount
                        WHERE PRICE=buyPrice AND ID=marketId AND TICKER=stock;

                    END;
                """;
                CallableStatement callableStatement = connection.prepareCall(query);
                callableStatement.setInt(1, tid);
                callableStatement.setInt(2, id);
                callableStatement.setFloat(3, prices.get(i));
                callableStatement.setFloat(4, amounts.get(i));
                callableStatement.setString(5, stock);
                callableStatement.execute();
                callableStatement.close();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCancel(String user) {
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }

            String date = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                date = date + resultSet.getDate("recent");
            }

            query = String.format("SELECT COUNT(*) AS amount FROM STOCKTRANSACTIONS WHERE marketid = '%d' AND dot = TO_DATE('%s', 'YYYY-MM-DD') AND cancelled = 0 AND shareschanged != 0", id, date);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                return resultSet.getInt("amount") != 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean canCancel(String user, float amount) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                return amount <= resultSet.getFloat("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String[]> getCancel(String user) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        try (Statement statement = connection.createStatement()) {
            int id;
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }

            String date = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                date = date + resultSet.getDate("recent");
            }

            int target;
            query = String.format("SELECT MAX(TID) AS maxTID FROM STOCKTRANSACTIONS WHERE marketid = '%d' AND dot = TO_DATE('%s', 'YYYY-MM-DD') AND cancelled = 0 AND shareschanged != 0", id, date);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                target = resultSet.getInt("maxTID");
            }

            query = String.format("SELECT * FROM STOCKTRANSACTIONS WHERE tid = '%d'", target);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while(resultSet.next()) {
                    String temp[] = {resultSet.getString("ticker"), 
                                    String.format("%.3f", resultSet.getFloat("shareschanged")), 
                                    String.format("%.2f", resultSet.getFloat("priceprev")), 
                                    String.format("%.2f", resultSet.getFloat("pricecurr")),
                                    String.valueOf(resultSet.getInt("tid"))};
                    result.add(temp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean cancel(int tid) {
        try (Statement statement = connection.createStatement()) {
            int id;
            String stock;
            float amount;
            float price;
            float curr;

            String query = String.format("SELECT * FROM STOCKTRANSACTIONS WHERE tid = '%d'", tid);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("marketid");
                stock = resultSet.getString("ticker");
            }
            try (ResultSet resultSet = statement.executeQuery(query)) {
                Statement tempStatement1 = connection.createStatement();
                String date = "";
                try (ResultSet tempSet = tempStatement1.executeQuery("SELECT * FROM MARKETSTATUS")) {
                    tempSet.next();
                    date = date + tempSet.getDate("recent");
                }

                int newTID;
                try (ResultSet tempSet = tempStatement1.executeQuery("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS")) {
                    tempSet.next();
                    newTID = tempSet.getInt("total") + 1;
                }
                try (ResultSet tempSet = tempStatement1.executeQuery("SELECT COUNT(*) AS total FROM MARKETTRANSACTIONS")) {
                    tempSet.next();
                    newTID = newTID + tempSet.getInt("total");
                }

                query = String.format("INSERT INTO STOCKTRANSACTIONS VALUES ('%s', '%d', 0, 0, 0, '%d', '%d', ?)", stock, id, newTID, tid);
                try (PreparedStatement prepState = connection.prepareStatement(query)) {
                    java.sql.Date temp = java.sql.Date.valueOf(date);
                    prepState.setDate(1, temp);
                    prepState.executeUpdate();
                }

                query = String.format("UPDATE MARKETACCOUNTS SET balance = balance - 20 WHERE id = '%d'", id);
                tempStatement1.executeQuery(query);

                tempStatement1.close();
                while(resultSet.next()) {
                    amount = resultSet.getFloat("shareschanged");
                    price = resultSet.getFloat("priceprev");
                    curr = resultSet.getFloat("pricecurr");

                    Statement tempStatement = connection.createStatement();

                    int acc;
                    query = String.format("SELECT * FROM STOCKACCOUNTS WHERE id = '%d' AND ticker = '%s'", id, stock);
                    try (ResultSet tempSet = tempStatement.executeQuery(query)) {
                        tempSet.next();
                        acc = tempSet.getInt("accountid");
                    }

                    query = String.format("UPDATE STOCKACCOUNTS SET shares = shares - '%f' WHERE accountid = '%d'", amount, acc);
                    tempStatement.executeQuery(query);

                    query = String.format("UPDATE STOCKACCOUNTBALANCES SET shares = shares - '%f' WHERE id = '%d' AND ticker = '%s' AND price = '%f'", amount, id, stock, price);
                    tempStatement.executeQuery(query);

                    query = String.format("UPDATE STOCKTRANSACTIONS SET cancelled = 1 WHERE tid = '%d'", tid);
                    tempStatement.executeQuery(query);

                    if (amount < 0) {
                        query = String.format("UPDATE MARKETACCOUNTS SET balance = balance + '%f', profit = profit + '%f', sharestraded = sharestraded - '%f' WHERE id = '%d'", amount * curr, amount * (curr - price), Math.abs(amount), id);
                    }
                    else {
                        query = String.format("UPDATE MARKETACCOUNTS SET balance = balance + '%f', sharestraded = sharestraded - '%f' WHERE id = '%d'", amount * price, Math.abs(amount), id);
                    }
                    tempStatement.executeQuery(query);
                    tempStatement.close();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Functions specifically for Manager.java

    public boolean isAdmin(String username) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MANAGERS WHERE username = '%s'", username);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAdminTax(String tax) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MANAGERS WHERE taxid = '%s'", tax);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createadmin(String username, String password, String name, String state, String phone, String email, String taxID) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("INSERT INTO MANAGERS VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')", username, password, email, name, state, phone, taxID);
            statement.executeQuery(query);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean admin(String username, String password) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MANAGERS WHERE username = '%s' AND password = '%s'", username, password);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean open() {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("UPDATE MARKETSTATUS SET marketopen = 1");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean close() {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("UPDATE MARKETSTATUS SET marketopen = 0");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean date(String date) {
        String query;
        try (Statement statement = connection.createStatement()) {
            String curr = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                curr = curr + resultSet.getDate("recent");
            }

            query = """
            DECLARE
                DaysDifference NUMBER;
                diff Number;
                newDate CHAR(10) := ?;
                newDay Number;
                temp DATE;
            BEGIN
                --Example
                -- Assuming you are comparing with a specific date, e.g., '2023-10-15'
                SELECT TO_DATE(newDate, 'YYYY-MM-DD') - MAX(M.RECENT) INTO DaysDifference 
                FROM MARKETSTATUS M;
                temp := TO_DATE(newDate, 'YYYY-MM-DD');
                SELECT EXTRACT(MONTH FROM TO_DATE(newDate, 'YYYY-MM-DD')) - EXTRACT(MONTH FROM RECENT) INTO diff
                FROM MARKETSTATUS;

                newDay := EXTRACT(DAY FROM TO_DATE(newDate, 'YYYY-MM-DD'));
                --DaysDifference := 14;
                -- Print the Days Difference
                IF diff=0 THEN
                UPDATE MARKETACCOUNTS
                SET TOTALBALANCE = TOTALBALANCE + DaysDifference * BALANCE;
                ELSE
                UPDATE MARKETACCOUNTS
                SET TOTALBALANCE = BALANCE * newDay, SHARESTRADED=0, PROFIT=0, INITIALBALANCE = BALANCE;
                DELETE FROM STOCKTRANSACTIONS;
                DELETE FROM MARKETTRANSACTIONS;
                END IF;
                UPDATE MARKETSTATUS
                SET RECENT = temp;
                END;
            """;

            CallableStatement callableStatement = connection.prepareCall(query);
            callableStatement.setString(1, date);

            callableStatement.execute();
            callableStatement.close();

            LocalDate start = LocalDate.parse(curr);
            LocalDate end = LocalDate.parse(date);

            for (LocalDate temp = start; temp.isBefore(end); temp = temp.plusDays(1)) {
                String str = temp.toString();
                query = String.format("INSERT INTO CLOSINGPRICES SELECT symbol, TO_DATE('%s', 'YYYY-MM-DD'), price FROM ACTDIRECT", str);
                statement.executeQuery(query);
            }

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStock(String stock, float price) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("UPDATE ACTDIRECT SET price = '%f' WHERE symbol = '%s'", price, stock);
            statement.executeQuery(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> customer(String user) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            int id;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                result.add(String.format("$%.2f", resultSet.getFloat("balance")));
                id = resultSet.getInt("id");
            }
            query = String.format("SELECT * FROM STOCKACCOUNTS WHERE id = '%s'", id);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    result.add(String.format("%s: %.3f shares", resultSet.getString("ticker"), resultSet.getFloat("shares")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> getInfo(String user) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS WHERE username = '%s'", user);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                result.add(resultSet.getString("name"));
                result.add(resultSet.getString("email"));
            }
            query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            int id;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                result.add(String.format("$%.2f", resultSet.getFloat("initialbalance")));
                result.add(String.format("$%.2f", resultSet.getFloat("balance")));
                result.add(String.format("$%.2f", resultSet.getFloat("profit")));
                id = resultSet.getInt("id");
            }
            query = String.format("SELECT COUNT(DISTINCT TID) AS total FROM STOCKTRANSACTIONS WHERE marketid = '%d'", id);
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                result.add(String.format("$%d", resultSet.getInt("total") * 20));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> getTransactions(String user) {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM MARKETACCOUNTS WHERE username = '%s'", user);
            int id;
            try (ResultSet resultSet = statement.executeQuery(query)) {
                resultSet.next();
                id = resultSet.getInt("id");
            }

            Statement statementOne = connection.createStatement();
            Statement statementTwo = connection.createStatement();

            String queryOne = String.format("SELECT * FROM MARKETTRANSACTIONS WHERE accountid = '%d' ORDER BY tid", id);
            String queryTwo = String.format("SELECT * FROM STOCKTRANSACTIONS WHERE marketid = '%d' ORDER BY tid", id);

            ResultSet resultSetOne = statementOne.executeQuery(queryOne);
            ResultSet resultSetTwo = statementTwo.executeQuery(queryTwo);

            boolean one = resultSetOne.next();
            boolean two = resultSetTwo.next();

            int prev = 0;
            int curr = 1;
            while (true) {
                String temp;
                if (one && two) {
                    if (resultSetOne.getInt("tid") < resultSetTwo.getInt("tid")) {
                        curr = resultSetOne.getInt("tid");
                        String date = "";
                        date = date + resultSetOne.getDate("dot");
                        if (resultSetOne.getInt("interest") == 1) {
                            temp = String.format("%d - Manager deposited $%.2f for monthly interest on %s", curr, resultSetOne.getFloat("amount"), date);
                        }
                        else if (resultSetOne.getFloat("amount") > 0) {
                            temp = String.format("%d - Deposited $%.2f on %s", curr, resultSetOne.getFloat("amount"), date);
                        }
                        else {
                            temp = String.format("%d - Withdrew $%.2f on %s", curr, -1 * resultSetOne.getFloat("amount"), date);
                        }
                        one = resultSetOne.next();
                    }
                    else {
                        curr = resultSetTwo.getInt("tid");
                        String date = "";
                        date = date + resultSetTwo.getDate("dot");
                        if (prev == curr) {
                            temp = String.format("|----> %.3f shares of %s (bought at $%.2f) at $%.2f on %s", -1 * resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("priceprev"), resultSetTwo.getFloat("pricecurr"), date);
                        }
                        else if (resultSetTwo.getFloat("shareschanged") == 0) {
                            // query = String.format("SELECT MAX(TID) as maxTID FROM STOCKTRANSACTIONS WHERE marketid = '%d' AND tid < '%d' AND shareschanged != 0", resultSetTwo.getInt("marketid"), resultSetTwo.getInt("tid"));
                            // int target;
                            // try (ResultSet resultSet = statement.executeQuery(query)) {
                            //     resultSet.next();
                            //     target = resultSet.getInt("maxTID");
                            // }
                            int target = resultSetTwo.getInt("cancelled");
                            temp = String.format("%d - Cancelled transaction %d on %s", curr, target, date);
                        }
                        else if (resultSetTwo.getFloat("shareschanged") > 0) {
                            temp = String.format("%d - Bought %.3f shares of %s at $%.2f on %s", curr, resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("pricecurr"), date);
                        }
                        else {
                            temp = String.format("%d - Sold %.3f shares of %s (bought at $%.2f) at $%.2f on %s", curr, -1 * resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("priceprev"), resultSetTwo.getFloat("pricecurr"), date);
                        }
                        two = resultSetTwo.next();
                    }
                }
                else if (one) {
                    curr = resultSetOne.getInt("tid");
                    String date = "";
                    date = date + resultSetOne.getDate("dot");
                    if (resultSetOne.getInt("interest") == 1) {
                        temp = String.format("%d - Manager deposited $%.2f for monthly interest on %s", curr, resultSetOne.getFloat("amount"), date);
                    }
                    else if (resultSetOne.getFloat("amount") > 0) {
                        temp = String.format("%d - Deposited $%.2f on %s", curr, resultSetOne.getFloat("amount"), date);
                    }
                    else {
                        temp = String.format("%d - Withdrew $%.2f on %s", curr, -1 * resultSetOne.getFloat("amount"), date);
                    }
                    one = resultSetOne.next();
                }
                else if (two) {
                    curr = resultSetTwo.getInt("tid");
                    String date = "";
                    date = date + resultSetTwo.getDate("dot");
                    if (prev == curr) {
                        temp = String.format("|----> %.3f shares of %s (bought at $%.2f) at $%.2f on %s", -1 * resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("priceprev"), resultSetTwo.getFloat("pricecurr"), date);
                    }
                    else if (resultSetTwo.getFloat("shareschanged") == 0) {
                        // query = String.format("SELECT MAX(TID) as maxTID FROM STOCKTRANSACTIONS WHERE marketid = '%d' AND tid < '%d'", resultSetTwo.getInt("marketid"), resultSetTwo.getInt("tid"));
                        // int target;
                        // try (ResultSet resultSet = statement.executeQuery(query)) {
                        //     resultSet.next();
                        //     target = resultSet.getInt("maxTID");
                        // }
                        int target = resultSetTwo.getInt("cancelled");
                        temp = String.format("%d - Cancelled transaction %d on %s", curr, target, date);
                    }
                    else if (resultSetTwo.getFloat("shareschanged") > 0) {
                        temp = String.format("%d - Bought %.3f shares of %s at $%.2f on %s", curr, resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("pricecurr"), date);
                    }
                    else {
                        temp = String.format("%d - Sold %.3f shares of %s (bought at $%.2f) at $%.2f on %s", curr, -1 * resultSetTwo.getFloat("shareschanged"), resultSetTwo.getString("ticker"), resultSetTwo.getFloat("priceprev"), resultSetTwo.getFloat("pricecurr"), date);
                    }
                    two = resultSetTwo.next();
                }
                else {
                    break;
                }
                prev = curr;
                result.add(temp);
            }

            resultSetOne.close();
            resultSetTwo.close();

            statementOne.close();
            statementTwo.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean set(float rate) {
        try (Statement statement = connection.createStatement()) {
            String query = String.format("UPDATE MARKETSTATUS SET interestrate='%f'", rate);
            statement.executeQuery(query);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean endOfMonth() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT EXTRACT(DAY FROM RECENT) - EXTRACT(DAY FROM RECENT + 1) AS DIFF FROM MARKETSTATUS")) {
                if (resultSet.next()) {
                    if (resultSet.getInt("diff") > 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addedInterest() {
        try (Statement statement = connection.createStatement()) {
            int month; 
            int lastUpdated;

            try (ResultSet resultSet = statement.executeQuery("SELECT EXTRACT(MONTH FROM RECENT) AS month FROM MARKETSTATUS")) {
                resultSet.next();
                month = resultSet.getInt("month");
            }

            try (ResultSet resultSet = statement.executeQuery("SELECT LASTMONTHUPDATED AS month FROM MARKETSTATUS")) {
                resultSet.next();
                lastUpdated = resultSet.getInt("month");
            }
            return month == lastUpdated;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean interest() {
        try (Statement statement = connection.createStatement()) {
            int days;
            try (ResultSet resultSet = statement.executeQuery("SELECT EXTRACT(DAY FROM RECENT) AS days FROM MARKETSTATUS")) {
                resultSet.next();
                days = resultSet.getInt("days");
            }

            float rate;
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                rate = resultSet.getFloat("interestrate");
            }

            String date = "";
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETSTATUS")) {
                resultSet.next();
                date = date + resultSet.getDate("recent");
            }

            int id = 1;
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS maxTID FROM MARKETTRANSACTIONS")) {
                resultSet.next();
                id = id + resultSet.getInt("maxTID");
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT TID) AS maxTID FROM STOCKTRANSACTIONS")) {
                resultSet.next();
                id = id + resultSet.getInt("maxTID");
            }

            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM MARKETACCOUNTS")) {
                while (resultSet.next()) {
                    String query = String.format("INSERT INTO MARKETTRANSACTIONS VALUES ('%f', '%d', '%d', 1, ?)", resultSet.getFloat("totalbalance") * rate / days, resultSet.getInt("id"), id);
                    try (PreparedStatement prepState = connection.prepareStatement(query)) {
                        java.sql.Date temp = java.sql.Date.valueOf(date);
                        prepState.setDate(1, temp);
                        prepState.executeUpdate();
                    }
                    id = id + 1;
                }
            }
            
            String query = String.format("UPDATE MARKETACCOUNTS SET BALANCE = BALANCE + '%f' * TOTALBALANCE / ('%d'), PROFIT = PROFIT + '%f' * TOTALBALANCE / ('%d')", rate, days, rate, days);
            statement.executeQuery(query);

            query ="UPDATE MARKETSTATUS SET LASTMONTHUPDATED = EXTRACT(MONTH FROM RECENT)";
            statement.executeQuery(query);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> tax() {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS, MARKETACCOUNTS WHERE profit > 10000 AND CUSTOMERS.username = MARKETACCOUNTS.username");
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    String temp = String.format("User %s:\n    Name: %s\n    State: %s\n    Earnings: $%.2f", resultSet.getString("username").trim(), resultSet.getString("name").trim(), resultSet.getString("state"), resultSet.getFloat("profit"));
                    result.add(temp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<String> active() {
        ArrayList<String> result = new ArrayList<String>();
        try (Statement statement = connection.createStatement()) {
            String query = String.format("SELECT * FROM CUSTOMERS, MARKETACCOUNTS WHERE sharestraded >= 1000 AND CUSTOMERS.username = MARKETACCOUNTS.username");
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    result.add(String.format("%s: %.3f shares traded", resultSet.getString("username").trim(), resultSet.getFloat("sharestraded")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void quit() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}