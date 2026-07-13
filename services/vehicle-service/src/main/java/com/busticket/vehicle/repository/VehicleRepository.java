package com.xekhach.vehicleservice.repository;

import com.xekhach.vehicleservice.entity.Vehicle;
import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("""
           SELECT v FROM Vehicle v
           WHERE (:status IS NULL OR v.status = :status)
             AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
             AND (:keyword IS NULL OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Vehicle> search(
            @Param("status") VehicleStatus status,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}