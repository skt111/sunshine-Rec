package org.example.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录拦截器单测（standalone MockMvc，不依赖 Spring 上下文与数据库）。
 */
class LoginInterceptorTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .addInterceptors(new LoginInterceptor())
                .build();
    }

    @Test
    void noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/probe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("401"));
    }

    @Test
    void withToken_shouldPass() throws Exception {
        mockMvc.perform(get("/api/probe").header("token", "dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result").value("ok"));
    }
}
