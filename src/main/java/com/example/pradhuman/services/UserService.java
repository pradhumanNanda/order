package com.example.pradhuman.services;

import com.example.pradhuman.entities.Payment;
import com.example.pradhuman.entities.User;
import com.example.pradhuman.entities.Wallet;
import com.example.pradhuman.utils.ValidationException;

import java.util.List;

public interface UserService {
    User getById(String id);
    List<User> getAllUsers();
    User addUser(User user) throws ValidationException;
    User updateUser(User user) throws ValidationException;
    boolean deleteUser(String id);
    void createDummyUsers(int number);
    Wallet getWallet(String userId);
    List<String> getLogs(String userId);
    Payment getPaymentById(Long id);


}
