package com.Twitter.com.Controller;

import com.Twitter.com.Services.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService)).build();
    }

    @Test
    void toggleBlueTickDelegatesToService() throws Exception {
        Mockito.when(adminService.toggleBlueTick(1L, true)).thenReturn("Blue tick was set..");

        mockMvc.perform(put("/admin/user/1/true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Blue tick was set.."));
    }

    @Test
    void toggleBlueTickReturns404WhenUserMissing() throws Exception {
        Mockito.when(adminService.toggleBlueTick(99L, true)).thenReturn("user doesn't exist");

        mockMvc.perform(put("/admin/user/99/true"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user doesn't exist"));
    }
}
