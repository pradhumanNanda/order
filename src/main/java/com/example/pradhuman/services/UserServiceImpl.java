package com.example.pradhuman.services;

import com.example.pradhuman.entities.User;
import com.example.pradhuman.repositories.UserRepository;
import com.example.pradhuman.utils.Jutil;
import com.example.pradhuman.utils.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;
    @Override
    public User getById(String id) {
        return userRepository.findById(id).orElse(User.builder().found(false).build());
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User addUser(User user) throws ValidationException {
        Jutil.validateUser(user);
        user.setUserId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        if(userRepository.findById(user.getUserId()).isPresent()){
            User oldUser = userRepository.findById(user.getUserId()).get();
            Jutil.getUpdatedUser(oldUser, user);
           return userRepository.save(oldUser);
        }
        return User.builder().build();

    }

    @Override
    public boolean deleteUser(String id) {
        if(userRepository.findById(id).isPresent()){
            User user = userRepository.findById(id).get();
            user.setDisabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
