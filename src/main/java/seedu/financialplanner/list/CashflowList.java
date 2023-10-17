package seedu.financialplanner.list;

import seedu.financialplanner.enumerations.CashflowCategory;
import seedu.financialplanner.utils.Ui;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CashflowList {
    public static Logger logger = Logger.getLogger("Financial Planner Logger");

    public static final CashflowList INSTANCE = new CashflowList();
    public final ArrayList<Cashflow> list = new ArrayList<>();

    private CashflowList() {
    }

    public void addIncome(double value, String type, int recur) {
        logger.log(Level.INFO, "Adding income");
        int existingListSize = list.size();

        Income toAdd = new Income(value, type, recur);
        list.add(toAdd);
        Ui.INSTANCE.printAddedCashflow(toAdd);

        int newListSize = list.size();
        assert newListSize == existingListSize + 1;
    }

    public void addExpense(double value, String type, int recur) {
        logger.log(Level.INFO, "Adding expense");
        int existingListSize = list.size();

        Expense toAdd = new Expense(value, type, recur);
        list.add(toAdd);
        Ui.INSTANCE.printAddedCashflow(toAdd);

        int newListSize = list.size();
        assert newListSize == existingListSize + 1;
    }

    public void delete(int index) {
        int existingListSize = list.size();
        int listIndex = index - 1;

        Cashflow toRemove = list.get(listIndex);
        list.remove(listIndex);
        toRemove.deleteCashflowvalue();
        Ui.INSTANCE.printDeletedCashflow(toRemove);

        int newListSize = list.size();
        assert newListSize == existingListSize - 1;
    }
    //helper method to find the index of a given cashflow in the overall list
    //given its index in its respective list. e.g. "income 3" is the third income
    //in the overall list
    private int cashflowIndexFinder(CashflowCategory category, int cashflowIndex) {
        assert category.equals(CashflowCategory.INCOME) || category.equals(CashflowCategory.EXPENSE);

        switch (category) {
        case INCOME:
            return findCashflowIndexFromIncomeIndex(cashflowIndex);
        case EXPENSE:
            return findCashflowIndexFromExpenseIndex(cashflowIndex);
        default:
            return -1;
        }
    }

    private int findCashflowIndexFromIncomeIndex(int cashflowIndex) {
        int cashflowCounter = 0;
        int overallCashflowIndex = 0;

        for (Cashflow entry : list) {
            if (entry instanceof Income) {
                cashflowCounter += 1;
            }
            if (cashflowCounter == cashflowIndex) {
                break;
            }
            overallCashflowIndex += 1;
        }
        return overallCashflowIndex;
    }

    private int findCashflowIndexFromExpenseIndex(int cashflowIndex) {
        int cashflowCounter = 0;
        int overallCashflowIndex = 0;

        for (Cashflow entry : list) {
            if (entry instanceof Expense) {
                cashflowCounter += 1;
            }
            if (cashflowCounter == cashflowIndex) {
                break;
            }
            overallCashflowIndex += 1;
        }
        return overallCashflowIndex;
    }

    public void deleteCashflow(CashflowCategory category, int index) {
        int existingListSize = list.size();
        int listIndex = cashflowIndexFinder(category, index);

        Cashflow toRemove = list.get(listIndex);
        list.remove(listIndex);
        toRemove.deleteCashflowvalue();
        Ui.INSTANCE.printDeletedCashflow(toRemove);

        int newListSize = list.size();
        assert newListSize == existingListSize - 1;
    }


    public void load(Cashflow entry) {
        list.add(entry);
    }

    //temp method
    public String getList() {
        String output = "";
        for (Cashflow entry : list) {
            output += entry.formatString() + "\n";
        }
        return output;
    }
}