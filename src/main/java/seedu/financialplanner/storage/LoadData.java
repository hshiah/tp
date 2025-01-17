package seedu.financialplanner.storage;

import seedu.financialplanner.enumerations.ExpenseType;
import seedu.financialplanner.enumerations.IncomeType;
import seedu.financialplanner.exceptions.FinancialPlannerException;
import seedu.financialplanner.investments.Stock;
import seedu.financialplanner.cashflow.Budget;
import seedu.financialplanner.cashflow.Cashflow;
import seedu.financialplanner.cashflow.CashflowList;
import seedu.financialplanner.cashflow.Income;
import seedu.financialplanner.cashflow.Expense;
import seedu.financialplanner.utils.Ui;

import java.io.StreamCorruptedException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public abstract class LoadData {
    private static final String FILE_PATH = "data/watchlist.txt";
    private static final CashflowList cashflowList = CashflowList.getInstance();
    private static final Ui ui = Ui.getInstance();


    public static void load(String filePath, LocalDate date) throws FinancialPlannerException {
        try {
            Scanner inputFile = new Scanner(new FileReader(filePath));
            String line;
            ui.showMessage("Loading existing file...");

            while(inputFile.hasNext()) {
                line = inputFile.nextLine();
                String[] split = line.split("\\|");
                String type = split[0].trim();
                switch (type) {
                case "I":
                    // Fallthrough
                case "E":
                    final Cashflow entry = getEntry(type, split);
                    cashflowList.load(entry);
                    break;
                case "B":
                    loadBudget(split);
                    break;
                default:
                    throw new FinancialPlannerException("Error loading file");
                }
            }
            inputFile.close();
            deleteFutureCashflows(date);
            addRecurringCashflows(date);
        } catch (IOException e) {
            ui.showMessage("File not found. Creating new file...");
        } catch (IndexOutOfBoundsException e) {
            handleCorruptedFile("Empty/Missing arguments detected");
        } catch (IllegalArgumentException | FinancialPlannerException e) {
            handleCorruptedFile(e.getMessage());
        } catch (DateTimeParseException e) {
            handleCorruptedFile("Erroneous date format or Wrong position of date detected");
        }
    }

    private static void deleteFutureCashflows(LocalDate currentDate) {
        ArrayList<Integer> tempCashflow = new ArrayList<>();
        int indexToDelete = 0;
        for (Cashflow cashflow : cashflowList.list) {
            int recur = cashflow.getRecur();
            LocalDate dateOfAddition = cashflow.getDate();
            if (recur > 0 && currentDate.isBefore(dateOfAddition)) {
                Integer integer = indexToDelete;
                tempCashflow.add(integer);
            }
            indexToDelete++;
        }
        if (!tempCashflow.isEmpty()) {
            ui.showMessage("Detected erroneous cashflow entries. Removing future cashflows...");
            for (int i = 0; i < tempCashflow.size(); i++) {
                indexToDelete = tempCashflow.get(i) - i;
                // deleteCashflowWithoutCategory takes in list index starting from 1, indexToDelete starts from 0
                int indexStartingFromOne = indexToDelete + 1;
                cashflowList.deleteCashflowWithoutCategory(indexStartingFromOne);
            }
        }
    }

    private static void addRecurringCashflows(LocalDate currentDate) throws FinancialPlannerException {
        ui.showMessage("Adding any recurring cashflows...");
        ArrayList<Cashflow> tempCashflow = new ArrayList<>();
        for (Cashflow cashflow : cashflowList.list) {
            int recur = cashflow.getRecur();
            LocalDate dateOfAddition = cashflow.getDate();
            boolean hasRecurred = cashflow.getHasRecurred();
            addRecurringCashflowToTempList(currentDate, cashflow, recur, dateOfAddition, tempCashflow, hasRecurred);
        }
        for (Cashflow cashflow : tempCashflow) {
            cashflowList.load(cashflow);
            ui.printAddedCashflow(cashflow);
        }
    }

    private static void addRecurringCashflowToTempList(LocalDate currentDate
            , Cashflow cashflow, int recur, LocalDate dateOfAddition
            , ArrayList<Cashflow> tempCashflow, boolean hasRecurred) throws FinancialPlannerException {
        if (recur > 0 && !hasRecurred) {
            dateOfAddition = dateOfAddition.plusDays(recur);
            identifyRecurredCashflows(currentDate, cashflow, recur, dateOfAddition, tempCashflow);
        }
    }

    private static void identifyRecurredCashflows(LocalDate currentDate
            , Cashflow cashflow, int recur, LocalDate dateOfAddition
            , ArrayList<Cashflow> tempCashflow) throws FinancialPlannerException {
        while (currentDate.isAfter(dateOfAddition) || currentDate.isEqual(dateOfAddition)) {
            cashflow.setHasRecurred(true);
            Cashflow toAdd;
            if (cashflow instanceof Income) {
                toAdd = new Income((Income) cashflow);
            } else if (cashflow instanceof Expense) {
                toAdd = new Expense((Expense) cashflow);
            } else {
                throw new FinancialPlannerException("Error adding recurring cashflows");
            }
            toAdd.setDate(dateOfAddition);
            tempCashflow.add(toAdd);
            cashflow = toAdd;
            dateOfAddition = dateOfAddition.plusDays(recur);
        }
    }

    private static void handleCorruptedFile(String message) throws FinancialPlannerException {
        ui.showMessage("File appears to be corrupted. Do you want to create a new file? (Y/N)");
        if (createNewFile()) {
            cashflowList.list.clear();
            Cashflow.clearBalance();
        } else {
            throw new FinancialPlannerException("Please fix the corrupted file, " +
                    "which can be found in data/data.txt.\nError message: " + message);
        }
    }

    private static void loadBudget(String[] split) throws IllegalArgumentException {
        double initial = Double.parseDouble(split[1].trim());
        double current = Double.parseDouble(split[2].trim());
        if (initial == 0 && current == 0) {
            return;
        }
        if (initial < 0 || current < 0) {
            throw new IllegalArgumentException("Negative values for budget");
        }
        if (initial > Cashflow.getBalance() || current > Cashflow.getBalance()) {
            throw new IllegalArgumentException("Budget exceeds balance");
        }
        if (initial < current) {
            throw new IllegalArgumentException("Current budget exceeds initial budget");
        }
        Budget.load(initial, current);
    }

    private static boolean createNewFile() {
        String line = ui.input();
        while (!line.equalsIgnoreCase("y") && !line.equalsIgnoreCase("n")) {
            ui.showMessage("Unknown input. Please enter Y or N only.");
            line = ui.input();
        }

        return line.equalsIgnoreCase("y");
    }

    private static Cashflow getEntry(String type, String[] split)
            throws FinancialPlannerException, IllegalArgumentException, DateTimeParseException
            , IndexOutOfBoundsException {
        try {
            Cashflow entry;
            double value = Double.parseDouble(split[1].trim());
            int recur = Integer.parseInt(split[3].trim());
            boolean hasRecurred = getHasRecurred(split, recur);
            LocalDate date = getDate(split, recur);
            int index = getIndex(recur);
            String description = getDescription(split, index);
            checkValidInput(value, recur);

            switch (type) {
            case "I":
                IncomeType incomeType;
                incomeType = IncomeType.valueOf(split[2].trim().toUpperCase());
                entry = new Income(value, incomeType, recur, description, date, hasRecurred);
                break;
            case "E":
                ExpenseType expenseType;
                expenseType = ExpenseType.valueOf(split[2].trim().toUpperCase());
                entry = new Expense(value, expenseType, recur, description, date, hasRecurred);
                break;
            default:
                throw new FinancialPlannerException("Error loading file");
            }
            return entry;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erroneous arguments detected");
        }
    }

    private static boolean getHasRecurred(String[] split, int recur) throws IllegalArgumentException {
        boolean hasRecurred;
        if (recur != 0) {
            String stringHasRecurred = split[4].trim();
            if (stringHasRecurred.equals("true") || stringHasRecurred.equals("false")) {
                hasRecurred = Boolean.parseBoolean(stringHasRecurred);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            hasRecurred = false;
        }
        return hasRecurred;
    }

    private static LocalDate getDate(String[] split, int recur) {
        LocalDate date;
        if (recur != 0) {
            date = LocalDate.parse(split[5].trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            date = null;
        }
        return date;
    }

    private static int getIndex(int recur) {
        int index;
        if (recur != 0) {
            index = 6;
        } else {
            index = 5;
        }
        return index;
    }

    private static String getDescription(String[] split, int index) {
        String description;
        if (split.length > index) {
            description = split[index].trim();
        } else {
            description = null;
        }
        return description;
    }

    public static HashMap<String, Stock> loadWatchList() {
        Ui ui = Ui.getInstance();
        HashMap<String, Stock> stocksData = new HashMap<>();
        try {
            ObjectInputStream watchListStocksInputStream
                    = new ObjectInputStream(
                        new FileInputStream(FILE_PATH)
            );
            stocksData = (HashMap<String, Stock>) watchListStocksInputStream.readObject();
            watchListStocksInputStream.close();
        } catch (StreamCorruptedException e) {
            ui.showMessage("Watchlist file corrupted.. Rebuilding");
        } catch (IOException e) {
            ui.showMessage("Watchlist file not found... Creating");
        } catch (ClassNotFoundException e) {
            ui.showMessage("FIle appears to be corrupted...");
        }
        return stocksData;
    }

    private static void checkValidInput(double value, int recur) throws FinancialPlannerException {
        if (value < 0 || recur < 0) {
            throw new FinancialPlannerException("Amount and number of days cannot be negative");
        }
    }
}
