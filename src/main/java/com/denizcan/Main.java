package com.denizcan;

import com.denizcan.model.Task;
import com.denizcan.service.TaskService;
import com.denizcan.util.FileManager;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TaskService taskService = new TaskService();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Liste seçimi/oluşturma
        String currentListName = selectOrCreateList(sc);

        // Seçilen listedeki görevleri yükle
        taskService.clearAll();
        FileManager.loadTasks(taskService, currentListName);

        // Autosave: her 10 saniyede bir seçili listeyi kaydet (sessiz)
        final String[] autosaveListRef = { currentListName };
        scheduler.scheduleAtFixedRate(() -> FileManager.saveTasks(taskService, autosaveListRef[0], true), 10, 10, TimeUnit.SECONDS);

        // Program beklenmedik kapanırsa düzgün kapatmak için shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduler.shutdownNow();
            } catch (Exception ignored) {}
            FileManager.saveTasks(taskService, autosaveListRef[0], true);
        }));

        int choice = 0;

        while (choice != 7) {
            showMenu(currentListName);
            choice = readIntFromOneTo(sc);

            switch (choice) {
                case 1:
                    System.out.print("Enter task name (or 'b' to go back): ");
                    String name = sc.nextLine();
                    if (name.equalsIgnoreCase("b")) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }

                    while (true) {
                        System.out.print("Enter due date (YYYY-MM-DD), leave empty for none, or 'b' to cancel: ");
                        String input = sc.nextLine().trim();
                        if (input.equalsIgnoreCase("b")) {
                            System.out.println("Cancelled. Returning to menu.");
                            break;
                        }
                        if (input.isEmpty()) {
                            Task newTask = new Task(name);
                            taskService.addTask(newTask);
                            System.out.println("Task added successfully!");
                            break;
                        }
                        try {
                            LocalDate dueDate = LocalDate.parse(input);
                            Task newTask = new Task(name);
                            newTask.setDueDate(dueDate);
                            taskService.addTask(newTask);
                            System.out.println("Task added successfully!");
                            break;
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Expected YYYY-MM-DD.");
                        }
                    }
                    break;

                case 2:
                    showTasks(taskService);
                    break;

                case 3:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to delete.");
                        break;
                    }
                    int delIndex = readIntFromZeroTo(sc, "Enter task number to delete (0 to go back): ", taskService.getTasks().size());
                    if (delIndex == 0) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }
                    taskService.deleteTask(delIndex - 1);
                    System.out.println("Task deleted if number was valid.");
                    break;

                case 4:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to edit.");
                        break;
                    }
                    int editIndexInput = readIntFromZeroTo(sc, "Enter task number to edit (0 to go back): ", taskService.getTasks().size());
                    if (editIndexInput == 0) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }
                    int editIndex = editIndexInput - 1;

                    System.out.println("What do you want to edit?");
                    System.out.println("1. Name");
                    System.out.println("2. Due date");
                    System.out.println("3. Completion status");
                    int editChoice = readIntFromZeroTo(sc, "Enter a choice (0-3): ", 3);
                    if (editChoice == 0) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }

                    switch (editChoice) {
                        case 1:
                            System.out.print("Enter new task name (or 'b' to cancel): ");
                            String newName = sc.nextLine();
                            if (newName.equalsIgnoreCase("b")) {
                                System.out.println("Cancelled. Returning to menu.");
                                break;
                            }
                            taskService.editTask(editIndex, newName);
                            System.out.println("Task name updated.");
                            break;
                        case 2:
                            while (true) {
                                System.out.print("Enter new due date (YYYY-MM-DD), leave empty to clear, or 'b' to cancel: ");
                                String in = sc.nextLine().trim();
                                if (in.equalsIgnoreCase("b")) {
                                    System.out.println("Cancelled. Returning to menu.");
                                    break;
                                }
                                if (in.isEmpty()) {
                                    taskService.editTaskDueDate(editIndex, null);
                                    System.out.println("Task due date cleared.");
                                    break;
                                }
                                try {
                                    LocalDate newDue = LocalDate.parse(in);
                                    taskService.editTaskDueDate(editIndex, newDue);
                                    System.out.println("Task due date updated.");
                                    break;
                                } catch (DateTimeParseException e) {
                                    System.out.println("Invalid date format. Expected YYYY-MM-DD.");
                                }
                            }
                            break;
                        case 3:
                            System.out.println("1. Mark as completed");
                            System.out.println("2. Mark as not completed");
                            int compSel = readIntFromZeroTo(sc, "Enter a choice (0-2): ", 2);
                            if (compSel == 0) {
                                System.out.println("Cancelled. Returning to menu.");
                                break;
                            }
                            taskService.editTaskCompletion(editIndex, compSel == 1);
                            System.out.println("Task completion status updated.");
                            break;
                        default:
                            System.out.println("Invalid choice!");
                    }
                    break;

                case 5:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to mark as completed.");
                        break;
                    }
                    int compIndexInput = readIntFromZeroTo(sc, "Enter task number to mark as completed (0 to go back): ", taskService.getTasks().size());
                    if (compIndexInput == 0) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }
                    int compIndex = compIndexInput - 1;
                    taskService.markTaskCompleted(compIndex);
                    System.out.println("Task marked completed if number was valid.");
                    break;

                case 6:
                    System.out.println("Sort by:");
                    System.out.println("1. Default (natural order)");
                    System.out.println("2. Name (A-Z)");
                    System.out.println("3. Due date (nearest first, empty last)");
                    System.out.println("4. Completed first");
                    int sortChoice = readIntFromZeroTo(sc, "Enter a choice (0-4): ", 4);
                    if (sortChoice == 0) {
                        System.out.println("Cancelled. Returning to menu.");
                        break;
                    }

                    switch (sortChoice) {
                        case 1:
                            taskService.sortTasks();
                            break;
                        case 2: {
                            Comparator<Task> byName = Comparator.comparing(t -> t.getName() == null ? "" : t.getName().toLowerCase());
                            taskService.sortTasksWith(byName);
                            break;
                        }
                        case 3: {
                            Comparator<Task> byDueAscNullsLast =
                                    Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
                            taskService.sortTasksWith(byDueAscNullsLast);
                            break;
                        }
                        case 4: {
                            Comparator<Task> completedFirst =
                                    Comparator.comparing(Task::isCompleted).reversed()
                                            .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                            .thenComparing(Task::getCreationDate)
                                            .thenComparing(t -> t.getName() == null ? "" : t.getName().toLowerCase());
                            taskService.sortTasksWith(completedFirst);
                            break;
                        }
                        default:
                            System.out.println("Invalid choice!");
                            break;
                    }
                    System.out.println("Tasks sorted.");
                    break;

                case 7:
                    // Çıkış: autosave'i kapat ve son bir kaydetme yap
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    FileManager.saveTasks(taskService, autosaveListRef[0]);
                    System.out.println("Exiting program...");
                    break;

                case 8:
                    // Liste değiştir/oluştur
                    FileManager.saveTasks(taskService, autosaveListRef[0]);
                    String newList = selectOrCreateList(sc);
                    if (newList != null && !newList.equals(autosaveListRef[0])) {
                        taskService.clearAll();
                        FileManager.loadTasks(taskService, newList);
                        autosaveListRef[0] = newList;
                        System.out.println("Switched to list: " + newList);
                    }
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }

        sc.close();
    }

    public static void showMenu(String currentListName) {
        System.out.println("\n===== TASK MENU =====");
        System.out.println("1. Create a new task");
        System.out.println("2. Show all tasks");
        System.out.println("3. Delete a task");
        System.out.println("4. Edit a task");
        System.out.println("5. Mark task as completed");
        System.out.println("6. Sort tasks");
        System.out.println("7. Save and Exit");
        System.out.println("8. Switch/Create list (current: " + currentListName + ")");
    }

    public static void showTasks(TaskService taskService) {
        if (taskService.getTasks().isEmpty()) {
            System.out.println("No tasks found!");
        } else {
            System.out.println("\nAll Tasks:");
            int i = 1;
            for (Task task : taskService.getTasks()) {
                System.out.println(i + ". " + task);
                i++;
            }
        }
    }

    private static int readIntFromOneTo(Scanner sc) {
        while (true) {
            System.out.print("Enter a choice (1-8): ");
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < 1 || value > 8) {
                    System.out.println("Please enter a number between 1 and " + 8 + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static int readIntFromZeroTo(Scanner sc, String prompt, int max) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < 0 || value > max) {
                    System.out.println("Please enter a number between 0 and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static String selectOrCreateList(Scanner sc) {
        while (true) {
            System.out.println("\n===== LISTS =====");
            String[] lists = FileManager.listAllLists();
            if (lists.length == 0) {
                System.out.println("No lists found.");
            } else {
                System.out.println("Available lists:");
                for (int i = 0; i < lists.length; i++) {
                    System.out.println((i + 1) + ". " + lists[i]);
                }
            }
            System.out.println("0. Create new list");
            int sel = readIntFromZeroTo(sc, "Select a list (0 to create new): ", lists.length);
            if (sel == 0) {
                System.out.print("Enter new list name (letters, digits, - _ allowed): ");
                String name = sc.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty.");
                    continue;
                }
                String safe = name.replaceAll("[^A-Za-z0-9_-]", "_");
                if (safe.isEmpty()) {
                    System.out.println("Invalid name.");
                    continue;
                }
                // Dokunarak dosyayı oluştur
                java.io.File f = FileManager.resolveListFile(safe);
                try {
                    if (!f.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        f.getParentFile().mkdirs();
                        //noinspection ResultOfMethodCallIgnored
                        f.createNewFile(); // dosya mevcutsa false döner, önemli değil
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Could not create list: " + e.getMessage());
                    continue;
                }
                return safe;
            } else {
                return lists[sel - 1];
            }
        }
    }
}
