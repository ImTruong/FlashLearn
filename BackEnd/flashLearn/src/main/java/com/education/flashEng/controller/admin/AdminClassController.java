package com.education.flashEng.controller.admin;

import com.education.flashEng.payload.response.ApiResponse;
import com.education.flashEng.service.ClassService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/class")
public class AdminClassController {

    @Autowired
    private ClassService classService;

    @GetMapping
    public ResponseEntity<?> getAllClasses(@RequestParam(required = false) String name,
                                         @RequestParam (required = true) int page,
                                         @RequestParam (required = true) int size) {
        ApiResponse apiResponse = new ApiResponse(true, "Get All Classes Successfully", classService.findClassByName(name,page,size));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }



}
