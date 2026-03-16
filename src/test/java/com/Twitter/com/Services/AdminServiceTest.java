package com.Twitter.com.Services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Test
    void toggleBlueTickDelegatesToUserService() {
        UserService userService = mock(UserService.class);
        AdminService adminService = new AdminService(userService);
        when(userService.toggleBlueTick(5L, true)).thenReturn("ok");

        String result = adminService.toggleBlueTick(5L, true);

        assertEquals("ok", result);
        verify(userService).toggleBlueTick(5L, true);
    }
}
