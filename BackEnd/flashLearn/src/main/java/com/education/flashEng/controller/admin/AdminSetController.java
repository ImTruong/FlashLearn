package com.education.flashEng.controller.admin;


import com.education.flashEng.payload.response.ApiResponse;
import com.education.flashEng.service.SetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/set")
public class AdminSetController {

    @Autowired
    private SetService setService;

    @GetMapping
    public ResponseEntity<?> getAllSets(@RequestParam(required = false) String name,
                                         @RequestParam (required = true) int page,
                                         @RequestParam (required = true) int size) {
        ApiResponse apiResponse = new ApiResponse(true, "Get All Sets Successfully", setService.findSetByName(name,page,size));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
