package com.example.loom.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TaskService {

    private static final int TASK_COUNT = 10;
    private static final long SIMULATED_IO_DELAY_MS = 100;

    /**
     * Runs tasks using a fixed-size platform thread pool, simulating
     * typical I/O-bound work (e.g. database calls, HTTP requests).
     */
    public List<String> runWithPlatformThreads() throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newFixedThreadPool(TASK_COUNT)) {
            return submitTasks(executor, "platform");
        }
    }

    /**
     * Runs the same tasks using virtual threads (Project Loom).
     * Each task gets its own virtual thread, which is parked during I/O
     * and unmounted from the carrier thread rather than blocking it.
     */
    public List<String> runWithVirtualThreads() throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            return submitTasks(executor, "virtual");
        }
    }

    private List<String> submitTasks(ExecutorService executor, String threadType)
            throws InterruptedException, ExecutionException {
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < TASK_COUNT; i++) {
            final int taskId = i + 1;
            futures.add(executor.submit(() -> simulateIoTask(taskId, threadType)));
        }

        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }
        return results;
    }

    private String simulateIoTask(int taskId, String threadType) throws InterruptedException {
        Thread current = Thread.currentThread();
        String threadInfo = String.format("virtual=%s, name=%s",
                current.isVirtual(), current.getName());
        Thread.sleep(SIMULATED_IO_DELAY_MS);
        return String.format("Task %d completed on %s thread [%s]", taskId, threadType, threadInfo);
    }
}
