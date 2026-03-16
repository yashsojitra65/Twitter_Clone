package com.Twitter.com.Controller;

import com.Twitter.com.Services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Manage administrative tasks and operations securely, including toggling the blue tick status for users.")
public class AdminController {

    private final AdminService adminService;

    @PutMapping("user/{id}/{blueTick}")
    @Operation(
            summary = "Toggle Blue Tick Status",
            description = "Toggle the blue tick status for a specific user by providing the user ID and the new blue tick status.",
            tags = {"Admin Management"}
    )
    public ResponseEntity<String> toggleBlueTick(@PathVariable Long id, @PathVariable boolean blueTick) {
        String result = adminService.toggleBlueTick(id, blueTick);
        HttpStatus status = "user doesn't exist".equalsIgnoreCase(result) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }
}
