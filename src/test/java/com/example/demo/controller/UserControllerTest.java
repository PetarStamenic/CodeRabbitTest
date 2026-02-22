package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final UserResponse SAMPLE = new UserResponse(1L, "Alice", "alice@example.com", Instant.now());

    // --- GET /api/users ---

    @Test
    void listAll_returnsOkWithUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(SAMPLE));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    // --- GET /api/users/{id} ---

    @Test
    void getById_existingUser_returnsOk() throws Exception {
        when(userService.findById(1L)).thenReturn(SAMPLE);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getById_missingUser_returnsNotFound() throws Exception {
        when(userService.findById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // --- GET /api/users/{id}/async ---

    @Test
    void getByIdAsync_returnsOk() throws Exception {
        when(userService.findByIdAsync(1L)).thenReturn(CompletableFuture.completedFuture(SAMPLE));

        MvcResult mvcResult = mockMvc.perform(get("/api/users/1/async"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- POST /api/users ---

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com");
        when(userService.create(any())).thenReturn(SAMPLE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_blankName_returnsBadRequest() throws Exception {
        UserRequest request = new UserRequest("", "alice@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicateEmail_returnsConflict() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com");
        when(userService.create(any())).thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --- PUT /api/users/{id} ---

    @Test
    void update_existingUser_returnsOk() throws Exception {
        UserRequest request = new UserRequest("Alice Updated", "alice@example.com");
        UserResponse updated = new UserResponse(1L, "Alice Updated", "alice@example.com", Instant.now());
        when(userService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    // --- DELETE /api/users/{id} ---

    @Test
    void delete_existingUser_returnsNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missingUser_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 99")).when(userService).delete(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
