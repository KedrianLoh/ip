import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Duke {

    private static Database database = null;
    private static Ui ui;
    private static Parser parser;

    public Duke () {
        ui = new Ui();
        ui.showGreetingMessage();
        try {
            database = new Database();
            parser = new Parser();
            readAndRespond(database);
        } catch (DukeException e) {
            ui.showDukeInputError();
        } catch (IOException e) {
            ui.showDatabaseLoadingError();
        }
        ui.exitMessage();
    }


    public static void main(String[] args) {
        new Duke();
    }

    public void readAndRespond(Database database) throws DukeException {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        while (!input.equals("bye")) {
            String[] words = input.split(" ");
            if (input.equals("list")) {
                printList(database.listOfTasks);
            } else if (input.isBlank()) {
                throw new DukeException();
            } else if (isValidUnmark(input, words)) {
                unmarkTask(database.listOfTasks, words);
            } else if (isValidMark(input, words)) {
                markTask(database.listOfTasks, words);
            } else if (IsValidDelete(input, words)) {
                removeTask(database.listOfTasks, words);
            } else if (input.startsWith("find") && words.length != 1) {
                ArrayList<Task> filteredList = (ArrayList<Task>)database.listOfTasks.stream()
                        .filter(t -> t.description.contains(words[1]))
                        .collect(toList());
                System.out.println(" Here are the matching tasks in your list:");
                int index = 0;
                for (Task x : filteredList) {
                    System.out.println(" " + (index + 1) + "." + x.statusMessage());
                    ++index;
                }
            }
            else if (parser.parseTask(input) != null) {
                Task task = parser.parseTask(input);
                database.listOfTasks.add(task);
                String[] taskDetail = getNameAndType(task,input).split("###");
                System.out.println(task.addedMessage());
                addTaskToDatabase(database, taskDetail[0], taskDetail[1] );
            } else {
                System.out.println("Invalid command! Start with todo/event/deadline to add!!");
            }
            input = sc.nextLine();
        }
    }
    private static String getNameAndType (Task task, String input) {
        String type = null;
        String name = null;
        if (task instanceof ToDo) {
            type = "T";
            name = input.substring(5);
        } else if (task instanceof Deadline) {
            type = "D";
            name = input.substring(9);
        } else {
            type = "E";
            name = input.substring(6);
        }
        return name + "###" + type;
    }

    private static void addTaskToDatabase(Database database, String name, String type) {
        try {
            database.appendToFile(name +  " --- " + type + " |[ ]| " + "\n");
        } catch (IOException e) {
            ui.showAddingTaskToDatabaseError();
        }
    }

    private static void removeTask(ArrayList<Task> listOfTasks, String[] words) {
        int number = Integer.parseInt(words[1]);
        if (number <= 0 || words.length == 1 || !isInt(words[1]) || listOfTasks.size() < number) {
            ui.showDeletingInvalidTaskError();
        } else {
            listOfTasks.get(number - 1).deleteTask();
            listOfTasks.remove(number - 1);
            --Task.taskCount;
            String addedString = getUpdatedFileContents(listOfTasks);
            try {
                Database.writeToFile(addedString);
            } catch (IOException e) {
                ui.showDeletingTaskFromDatabaseError();
            }
        }
    }
    private static boolean IsValidDelete(String input, String[] words) {
        return input.startsWith("delete") && words.length == 2 && isInt(words[1]);
    }

    private static boolean isValidMark(String input, String[] words) {
        return input.contains("mark") && words.length == 2 && isInt(words[1]);
    }

    private static boolean isValidUnmark(String input, String[] words) {
        return input.contains("unmark") && words.length == 2 && isInt(words[1]);
    }

    /**
     * Determines if the string is an integer
     *
     * @param str Str is the string we are checking
     * @return True if str is an integer, false otherwise
     * @catch NumberFormatException If str cannot be converted into integer
     */
    private static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static void markTask(ArrayList<Task> listOfTasks, String[] words) {
        int number = Integer.parseInt(words[1]);
        if (number <= 0 || words.length == 1 || !isInt(words[1]) || listOfTasks.size() < number) {
            ui.showMarkingInvalidTaskError();
        } else {
            listOfTasks.get(number - 1).markAsDone();
            System.out.println("  " + listOfTasks.get(number - 1).getStatusIcon() + " " + listOfTasks.get(number - 1).description);
            String addedString = getUpdatedFileContents(listOfTasks);
            try {
                Database.writeToFile(addedString);
            } catch (IOException e) {
                ui.showMarkingTaskInDatabaseError();
            }
        }
    }

    private static String getUpdatedFileContents(ArrayList<Task> listOfTasks) {
        String addedString = "";
        for (Task curr : listOfTasks) {
            String type = null;
            if (curr instanceof ToDo) {
                type = "T";
            } else if (curr instanceof Event) {
                type = "E";
            } else if (curr instanceof Deadline) {
                type = "D";
            }
            String currStr = curr.description + " --- " + type + " |" + curr.getStatusIcon() + "|\n";
            addedString += currStr;
        }
        return addedString;
    }

    private static void unmarkTask(ArrayList<Task> listOfTasks, String[] words) throws DukeException {
        int number = Integer.parseInt(words[1]);
        if (number <= 0 || words.length == 1 || !isInt(words[1]) || listOfTasks.size() < number) {
            ui.showUnmarkingInvalidTaskError();
        } else {
            listOfTasks.get(number - 1).unmarkDone();
            System.out.println(" " + listOfTasks.get(number - 1).getStatusIcon() + " " + listOfTasks.get(number - 1).description);
            String addedString = getUpdatedFileContents(listOfTasks);
            try {
                Database.writeToFile(addedString);
            } catch (IOException e) {
                ui.showUnmarkingTaskInDatabaseError();
            }
        }
    }

    public static void printList(ArrayList<Task> listOfTasks) {
        ui.showListHeaderMessage();
        if (listOfTasks.size() == 0) {
            ui.showEmptyListMessage();
            return;
        }
        int index = 0;
        for (Task x : listOfTasks) {
            System.out.println(" " + (index + 1) + "." + x.statusMessage());
            ++index;
        }
    }

}
