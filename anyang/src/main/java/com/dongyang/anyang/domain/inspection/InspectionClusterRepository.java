package com.dongyang.anyang.domain.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InspectionClusterRepository
        extends JpaRepository<InspectionCluster, Long> {

    Optional<InspectionCluster> findByCluster(Integer cluster);
}