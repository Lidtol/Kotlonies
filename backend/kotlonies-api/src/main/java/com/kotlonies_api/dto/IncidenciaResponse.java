package com.kotlonies_api.dto;

import com.kotlonies_api.model.Incidencia;
import java.time.LocalDateTime;

public record IncidenciaResponse(
    Long id,
    String titulo,
    String descripcion,
    String tipo,
    String prioridad,
    String estado,
    LocalDateTime fechaCreacion,
    Long catId,
    String catName,
    Long colonyId,
    String colonyName
) {

    public static IncidenciaResponse from(Incidencia incidencia) {

        return new IncidenciaResponse(
            incidencia.getId(),
            incidencia.getTitulo(),
            incidencia.getDescripcion(),
            incidencia.getTipo().name(),
            incidencia.getPrioridad().name(),
            incidencia.getEstado().name(),
            incidencia.getFechaCreacion(),

            incidencia.getCat() == null
                ? null
                : incidencia.getCat().getId(),

            incidencia.getCat() == null
                ? null
                : incidencia.getCat().getName(),

            incidencia.getColony() == null
                ? null
                : incidencia.getColony().getId(),

            incidencia.getColony() == null
                ? null
                : incidencia.getColony().getName()
        );
    }
}