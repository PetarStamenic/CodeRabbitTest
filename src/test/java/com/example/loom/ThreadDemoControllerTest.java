package com.example.loom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ThreadDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void platformEndpoint_returnsResultsForAllTasks() throws Exception {
        mockMvc.perform(get("/api/threads/platform"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.threadType").value("platform"))
                .andExpect(jsonPath("$.taskCount").value(10))
                .andExpect(jsonPath("$.results", hasSize(10)))
                .andExpect(jsonPath("$.results[0]", containsString("platform")))
                .andExpect(jsonPath("$.results[0]", containsString("virtual=false")));
    }

    @Test
    void virtualEndpoint_returnsResultsForAllTasks() throws Exception {
        mockMvc.perform(get("/api/threads/virtual"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.threadType").value("virtual"))
                .andExpect(jsonPath("$.taskCount").value(10))
                .andExpect(jsonPath("$.results", hasSize(10)))
                .andExpect(jsonPath("$.results[0]", containsString("virtual")))
                .andExpect(jsonPath("$.results[0]", containsString("virtual=true")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void infoEndpoint_withVirtualThreadsEnabled_returnsVirtualThread() {
        // Use the real embedded server (not MockMvc) so the request is handled
        // by the Tomcat virtual-thread executor configured via
        // spring.threads.virtual.enabled=true
        Map<String, Object> response = restTemplate.getForObject(
                "/api/threads/info", Map.class);
        assertThat(response).containsKey("isVirtual");
        assertThat(response.get("isVirtual")).isEqualTo(true);
        assertThat(response.get("threadId")).isNotNull();
    }
}
