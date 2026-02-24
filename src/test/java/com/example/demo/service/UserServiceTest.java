package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- findAll ---

    @Test
    void findAll_returnsAllUsers() {
        User u = new User("Alice", "alice@example.com");
        when(userRepository.findAll()).thenReturn(List.of(u));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice");
    }

    // --- findById ---

    @Test
    void findById_existingId_returnsUser() {
        User u = new User("Alice", "alice@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserResponse result = userService.findById(1L);

        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void findById_missingId_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    void create_newEmail_savesAndReturnsUser() {
        UserRequest request = new UserRequest("Bob", "bob@example.com");
        User saved = new User("Bob", "bob@example.com");
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = userService.create(request);

        assertThat(result.name()).isEqualTo("Bob");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        UserRequest request = new UserRequest("Bob", "bob@example.com");
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // --- update ---

    @Test
    void update_existingUser_updatesFields() {
        User existing = new User("Alice", "alice@example.com");
        UserRequest request = new UserRequest("Alice New", "alice@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse result = userService.update(1L, request);

        assertThat(result.name()).isEqualTo("Alice New");
    }

    @Test
    void update_missingUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UserRequest("X", "x@example.com")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- delete ---

    @Test
    void delete_existingUser_deletesSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertThatCode(() -> userService.delete(1L)).doesNotThrowAnyException();
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_missingUser_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
