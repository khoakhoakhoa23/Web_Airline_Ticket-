package com.flightbooking.repository;

import com.flightbooking.entity.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, String> {
    List<AdminAction> findByBookingId(String bookingId);
    List<AdminAction> findByAdminId(String adminId);
}

