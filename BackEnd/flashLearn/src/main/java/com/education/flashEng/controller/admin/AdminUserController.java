package com.education.flashEng.controller.admin;

import com.education.flashEng.payload.response.ApiResponse;
import com.education.flashEng.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")

public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam (required = false) String userName,
                                             @RequestParam (required = false) String email,
                                             @RequestParam (required = true) int page,
                                             @RequestParam (required = true) int size) {
        ApiResponse apiResponse = new ApiResponse(true, "Get All Users Successfully", userService.getAllUserDetailResponse(page, size, email, userName));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestParam @NotNull(message = "User id is required") Long userId) {
        ApiResponse apiResponse = new ApiResponse(true, "Delete User Successfully", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/update/role")
    public ResponseEntity<?> updateUserRole(@RequestParam @NotNull(message = "User id is required") Long userId,
                                            @RequestParam @NotNull(message = "Role is required") Long roleId) {
        ApiResponse apiResponse = new ApiResponse(true, "Update User Role Successfully", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/update/password")
    public ResponseEntity<?> updateUserPassword(@RequestParam @NotNull(message = "User id is required") Long userId,
                                                @RequestParam @NotNull(message = "Password is required") String password) {
        ApiResponse apiResponse = new ApiResponse(true, "Update User Password Successfully", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


}
