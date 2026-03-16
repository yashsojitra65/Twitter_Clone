package com.Twitter.com.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;

    public String toggleBlueTick(Long id, boolean blueTick) {
        return userService.toggleBlueTick(id, blueTick);
    }
}
