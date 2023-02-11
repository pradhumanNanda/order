package com.example.pradhuman.services;

import com.example.pradhuman.entities.User;
import com.example.pradhuman.entities.Wallet;
import com.example.pradhuman.repositories.UserRepository;
import com.example.pradhuman.repositories.WalletRepository;
import com.example.pradhuman.utils.Jutil;
import com.example.pradhuman.utils.PasswordManager;
import com.example.pradhuman.utils.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Autowired
    UserRepository userRepository;

    @Autowired
    WalletRepository walletRepository;


    @Override
    public User getById(String id) {
        User user = userRepository.findById(id).orElse(User.builder().found(false).build());
        if (!user.isFound()) {
            return user;
        }
        try {
            user.setPassword(PasswordManager.decrypt(user.getPassword(), PasswordManager.getSecretKey()));
            user.setPassword(Jutil.maskString(user.getPassword(),
                    2, user.getPassword().length() - 2, '*'));

        } catch (Exception e) {
            log.error("password decryption failed");
            throw new RuntimeException("error in password decryption");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        users.stream().forEach((u) -> {
            try {
                u.setPassword(PasswordManager.decrypt(u.getPassword(), PasswordManager.getSecretKey()));
                u.setPassword(Jutil.maskString(u.getPassword(), 2, u.getPassword().length() - 2, '*'));
            } catch (Exception e) {
                throw new RuntimeException("Error in decrypting password", e);
            }
        });
        return users;
    }

    @Override
    public User addUser(User user) throws ValidationException {
        Jutil.validateUser(user);
        user.setUserId(UUID.randomUUID().toString());
        try {
            user.setPassword(PasswordManager.encrypt(user.getPassword(), PasswordManager.getSecretKey()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error in password encryption");
        }
        synchronized (this) {
            walletRepository.save(Jutil.createWallet(user));
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        if (userRepository.findById(user.getUserId()).isPresent()) {
            User oldUser = userRepository.findById(user.getUserId()).get();
            Jutil.getUpdatedUser(oldUser, user);
            Jutil.validateUser(oldUser);
            try {
                oldUser.setPassword(PasswordManager.encrypt(oldUser.getPassword(), PasswordManager.getSecretKey()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("Error in password Encryption");
            }
            return userRepository.save(oldUser);
        }
        user.setFound(false);
        return user;

    }

    @Override
    public boolean deleteUser(String id) {
        if (userRepository.findById(id).isPresent()) {
            User user = userRepository.findById(id).get();
            user.setDisabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void createDummyUsers(int number) {
        new Thread() {
            @Override
            public void run() {
                log.info("creating dummy users");
                List<User> users = Jutil.createDummyUserList(number);
                userRepository.saveAll(users);
                log.debug("users saved in Db ;", users);
                log.info("Creating wallets for dummy users");
                List<Wallet> walletList = Jutil.createWalletsForDummyUsers(users);
                walletRepository.saveAll(walletList);
                log.debug("Wallets saved in Db :" ,walletList);
            }
        }.start();
    }

    @Override
    public Wallet getWallet(String userId) {
       return walletRepository.getUserWallet(userId);
    }

    @Override
    public List<String> getLogs(String userId) {
        Wallet wallet = walletRepository.getUserWallet(userId);
        if(wallet != null){
           return Arrays.asList(wallet.getAuditLogs().split("#"));
        }
        return null;
    }
}
