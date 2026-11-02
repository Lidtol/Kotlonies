package com.kotlonies_api.repository;

import com.kotlonies_api.model.Colony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColonyRepository extends JpaRepository<Colony, Long> {

    boolean existsByNameIgnoreCase(String name);
}
