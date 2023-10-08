package seedu.financialplanner;

import seedu.financialplanner.commands.Command;
import seedu.financialplanner.commands.Exit;
import seedu.financialplanner.list.Cashflow;
import seedu.financialplanner.list.FinancialList;
import seedu.financialplanner.utils.Parser;
import seedu.financialplanner.utils.Ui;

import java.util.ArrayList;

public class FinancialPlanner {
    private Ui ui;
    private FinancialList financialList;

    public FinancialPlanner() {
        ui = new Ui();
        financialList = new FinancialList();
    }

    public void run() {
        ui.welcomeMessage();
        String input;
        Command command = null;

        while (!(command instanceof Exit)) {
            input = ui.input();
            command = Parser.parse(input, financialList);
            command.execute(ui);
        }
        ui.exitMessage();
    }

    public static void main(String[] args) {
        new FinancialPlanner().run();
    }
}
