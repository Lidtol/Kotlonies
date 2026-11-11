package com.kotlonies_api.repository;

import com.kotlonies_api.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findAllByOrderByFechaHoraAsc();
}
