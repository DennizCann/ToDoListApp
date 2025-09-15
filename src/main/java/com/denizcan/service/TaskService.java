package com.denizcan.service;

import com.denizcan.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class TaskService {
    private final List<Task> tasks;

    public TaskService() {
        tasks = new ArrayList<>();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Task> getTasksSnapshot() {
        synchronized (tasks) {
            return new ArrayList<>(tasks);
        }
    }

    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public void deleteTask(int index) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.remove(index);
            }
        }
    }

    public void editTask(int index, String newName) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.get(index).setName(newName);
            }
        }
    }

    public void editTaskDueDate(int index, java.time.LocalDate newDueDate) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.get(index).setDueDate(newDueDate);
            }
        }
    }

    public void editTaskCompletion(int index, boolean completed) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.get(index).setCompleted(completed);
            }
        }
    }

    public void markTaskCompleted(int index) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.get(index).setCompleted(true);
            }
        }
    }

    public void sortTasks() {
        synchronized (tasks) {
            Collections.sort(tasks);
        }
    }

    public void sortTasksWith(Comparator<Task> comparator) {
        synchronized (tasks) {
            tasks.sort(comparator);
        }
    }
}
