package seedu.financialplanner.investments;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import seedu.financialplanner.exceptions.FinancialPlannerException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stock implements Serializable {
    private static final Logger logger = Logger.getLogger("Financial Planner Logger");
    private static final String API_ENDPOINT = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=";
    private static final String API_KEY = "LNKL0548PHY2F0QU";
    private String symbol;
    private String exchange;
    private String stockName;
    private String price;
    private String dayHigh;
    private String dayLow;
    private Date lastUpdated = null;
    private long lastFetched = 0;

    public Stock(String symbol) throws FinancialPlannerException {
        this.symbol = symbol;
        this.stockName = getStockNameFromAPI(symbol);
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockNameFromAPI(String symbol) throws FinancialPlannerException {
        String requestURI = String.format("%s%s&apikey=%s", API_ENDPOINT,symbol,API_KEY);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestURI))
                .header("accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        logger.log(Level.INFO, "Requesting API for stock info");
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Object obj = new JSONParser().parse(response.body());

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray ja = (JSONArray) jsonObject.get("bestMatches");
            if (ja == null) {
                throw new FinancialPlannerException("API limit Reached");
            }
            if (ja.isEmpty()) {
                throw new FinancialPlannerException("Stock not found");
            }
            JSONObject stock = (JSONObject) ja.get(0);
            String symbolFound = (String) stock.get("1. symbol");
            // TODO: Separate based on market
            // TODO: testing
            if (!symbolFound.equals(symbol)) {
                throw new FinancialPlannerException("Stock not found");
            }

            assert stock.get("2. name") != null;
            return (String) stock.get("2. name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol + ",";
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(String dayHigh) {
        this.dayHigh = dayHigh;
    }

    public String getDayLow() {
        return dayLow;
    }

    public void setDayLow(String dayLow) {
        this.dayLow = dayLow;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(long lastFetched) {
        this.lastFetched = lastFetched;
    }
}
