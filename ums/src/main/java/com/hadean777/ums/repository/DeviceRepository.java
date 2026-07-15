package com.hadean777.ums.repository;

import com.hadean777.ums.entity.Device;
import com.hadean777.ums.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserId(Long userId);
    Optional<Device> findByPublicKey(String publicKey);
}
