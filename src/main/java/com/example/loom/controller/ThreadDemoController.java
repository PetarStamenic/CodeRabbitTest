package com.example.loom.controller;

import com.example.loom.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/threads")
public class ThreadDemoController {

    private final TaskService taskService;

    public ThreadDemoController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Runs tasks on platform (OS) threads using a fixed thread pool.
     * Each blocked thread occupies an OS thread for the full duration.
     */
    @GetMapping("/platform")
    public ResponseEntity<Map<String, Object>> platformThreads()
            throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        List<String> results = taskService.runWithPlatformThreads();
        long elapsed = System.currentTimeMillis() - start;

        return ResponseEntity.ok(buildResponse("platform", results, elapsed));
    }

    /**
     * Runs tasks on virtual threads (Project Loom).
     * Blocked virtual threads are unmounted from their carrier thread,
     * allowing the carrier to execute other virtual threads while waiting.
     */
    @GetMapping("/virtual")
    public ResponseEntity<Map<String, Object>> virtualThreads()
            throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        List<String> results = taskService.runWithVirtualThreads();
        long elapsed = System.currentTimeMillis() - start;

        return ResponseEntity.ok(buildResponse("virtual", results, elapsed));
    }

    /**
     * Returns information about the thread handling the current HTTP request.
     * With {@code spring.threads.virtual.enabled=true} this will be a virtual thread.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> threadInfo() {
        Thread current = Thread.currentThread();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("threadName", current.getName());
        info.put("isVirtual", current.isVirtual());
        info.put("threadId", current.threadId());
        return ResponseEntity.ok(info);
    }

    private Map<String, Object> buildResponse(String type, List<String> results, long elapsedMs) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("threadType", type);
        response.put("taskCount", results.size());
        response.put("elapsedMs", elapsedMs);
        response.put("results", results);
        return response;
    }
}
