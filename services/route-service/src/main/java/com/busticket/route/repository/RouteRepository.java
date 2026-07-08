package com.xekhach.routeservice.repository;

import com.xekhach.routeservice.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    Page<Route> findByDepartureContainingIgnoreCaseAndDestinationContainingIgnoreCase(
            String departure, String destination, Pageable pageable);

    boolean existsByDepartureIgnoreCaseAndDestinationIgnoreCase(String departure, String destination);
}