package com.denizcan.service;

import com.denizcan.model.Task;

import java.util.ArrayList;
import java.util.List;

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

    public void markTaskCompleted(int index) {
        synchronized (tasks) {
            if (index >= 0 && index < tasks.size()) {
                tasks.get(index).setCompleted(true);
            }
        }
    }
}
