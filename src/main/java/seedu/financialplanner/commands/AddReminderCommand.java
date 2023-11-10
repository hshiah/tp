package seedu.financialplanner.commands;


import seedu.financialplanner.commands.utils.Command;
import seedu.financialplanner.commands.utils.RawCommand;
import seedu.financialplanner.reminder.Reminder;
import seedu.financialplanner.reminder.ReminderList;
import seedu.financialplanner.utils.Ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SuppressWarnings("unused")
public class AddReminderCommand extends Command {
    public static final String NAME = "addreminder";

    public static final String USAGE =
            "addreminder </t TYPE> </d DATE>";

    public static final String EXAMPLE =
            "addreminder /t debt /d 2023.12.11";
    private final String type;
    private final LocalDate date;

    public AddReminderCommand(RawCommand rawCommand) throws IllegalArgumentException {
        String typeString = String.join(" ", rawCommand.args);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (!rawCommand.extraArgs.containsKey("t")) {
            throw new IllegalArgumentException("Reminder must have a type");
        }
        type = rawCommand.extraArgs.get("t");
        if(type.isEmpty()){
            throw new IllegalArgumentException("Reminder type cannot be empty");
        }
        rawCommand.extraArgs.remove("t");
        if (!rawCommand.extraArgs.containsKey("d")) {
            throw new IllegalArgumentException("Reminder must have a date");
        }

        String dateString = rawCommand.extraArgs.get("d");
        if(dateString.isEmpty()){
            throw new IllegalArgumentException("Reminder date cannot be empty");
        }

        try {
            date = LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Reminder date must be in the format dd/MM/yyyy");
        }

        LocalDate currentTime = LocalDate.now();
        if(date.isBefore(currentTime)){
            throw new IllegalArgumentException("Reminder date cannot be in the past");
        }

        rawCommand.extraArgs.remove("d");
        if (!rawCommand.extraArgs.isEmpty()) {
            String unknownExtraArgument = new java.util.ArrayList<>(rawCommand.extraArgs.keySet()).get(0);
            throw new IllegalArgumentException(String.format("Unknown extra argument: %s", unknownExtraArgument));
        }
    }

    @Override
    public void execute() {
        Reminder reminder = new Reminder(type, date);
        ReminderList.getInstance().list.add(reminder);
        Ui.getInstance().showMessage("You have added " + reminder);
    }
}
