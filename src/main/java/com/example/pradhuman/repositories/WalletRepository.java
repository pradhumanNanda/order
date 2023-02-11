package com.example.pradhuman.repositories;

import com.example.pradhuman.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query(value = "select * from wallet w where w.user_id = :userId limit 1",nativeQuery = true)
    Wallet getUserWallet(@Param("userId")String userId);

}
