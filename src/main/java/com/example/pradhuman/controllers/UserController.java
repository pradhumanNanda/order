package com.example.pradhuman.controllers;

import com.example.pradhuman.entities.User;
import com.example.pradhuman.services.UserService;
import com.example.pradhuman.utils.BaseEntityResponse;
import com.example.pradhuman.utils.UserResponse;
import com.example.pradhuman.utils.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    public static int DEFAULT_DUMMY_NUMBER = 10;

    @PostMapping("/add")
    public BaseEntityResponse addUser(@RequestBody User user){
        BaseEntityResponse<User> userResponse = new UserResponse(user);
        try {
            userService.addUser(user);
            userResponse.setStatusCode(BaseEntityResponse.SUCCESS);
            userResponse.setStatusReason("User added successfully");
        } catch (ValidationException exception){
            userResponse.setStatusCode(BaseEntityResponse.FAILED);
            userResponse.setStatusReason(exception.getMessage());
        }
        return userResponse;
    }

    @GetMapping("/get-all")
    public BaseEntityResponse getAll(){
        List<User> users = userService.getAllUsers();
        BaseEntityResponse<User> response = new UserResponse(users);
        response.setStatusCode(BaseEntityResponse.SUCCESS);
        if(users.size() > 0){
            response.setStatusReason(String.format("Found %s users", users.size()));
        }else {
            response.setStatusReason("No user Found");
        }
        return response;
    }

    @GetMapping("/")
    public BaseEntityResponse getById(@RequestParam(name = "userId") String userId){
        BaseEntityResponse<User> response;
        User user = userService.getById(userId);
        if(user.isFound()){
            response = UserResponse.getSuccessResponse(String.format("user found for id : %s", userId));
            response.setEntity(user);
        }else {
            response = UserResponse.getFailedResponse(String.format("No user found for id : %s", userId));
        }
        return response;
    }

    @PutMapping("/update")
    public BaseEntityResponse updateUser(@RequestBody User user){
        BaseEntityResponse<User> response;
        User updatedUser = null;
        try {
            updatedUser = userService.updateUser(user);
        } catch (ValidationException e) {
            response = UserResponse.getFailedResponse(e.getMessage());
            return response;
        }
        if(updatedUser.isFound()){
            response = UserResponse.getSuccessResponse(String.format("user updated for id : %s", user.getUserId()));
            response.setEntity(updatedUser);
        }else {
            response = UserResponse.getFailedResponse(String.format("No user found for id : %s", user.getUserId()));
        }
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public BaseEntityResponse deleteUser(@PathVariable String id){
        return userService.deleteUser(id) ? UserResponse.getSuccessResponse(String.format("user deleted successfully " +
                "for id : %s", id)) : UserResponse.getFailedResponse(String.format("No user found for id : %s", id));
    }

    @PostMapping("/create-dummy")
    public String createDummy(@RequestParam(name = "number", required = false) Integer number){
        number = number != null ? number : DEFAULT_DUMMY_NUMBER;
       userService.createDummyUsers(number);
       return String.format("Thread to create %s dummy users started", number);
    }


    @ExceptionHandler({DataIntegrityViolationException.class})
    public BaseEntityResponse uniqueKeyVoilation(){
        return BaseEntityResponse.getFailedResponse("Email should be unique");
    }

    @ExceptionHandler({RuntimeException.class})
    public BaseEntityResponse generalSecurityExHandler(RuntimeException e){
        return BaseEntityResponse.getFailedResponse(e.getMessage());
    }

}
