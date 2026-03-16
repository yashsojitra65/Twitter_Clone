package com.Twitter.com.Controller;

import com.Twitter.com.Services.AdminService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void toggleBlueTickDelegatesToService() throws Exception {
        Mockito.when(adminService.toggleBlueTick(1L, true)).thenReturn("Blue tick was set..");

        mockMvc.perform(put("/admin/user/1/true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Blue tick was set.."));
    }
}
