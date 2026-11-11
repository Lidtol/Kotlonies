package com.kotlonies_api.dto;

import com.kotlonies_api.model.Tarea;
import java.time.LocalDateTime;

public record TareaResponse(
    Long id,
    String titulo,
    String descripcion,
    String tipo,
    String estado,
    LocalDateTime fechaHora,
    Long catId,
    String catName,
    Long colonyId,
    String colonyName
) {
    public static TareaResponse from(Tarea tarea) {
        return new TareaResponse(
            tarea.getId(),
            tarea.getTitulo(),
            tarea.getDescripcion(),
            tarea.getTipo().name(),
            tarea.getEstado().name(),
            tarea.getFechaHora(),
            tarea.getCat() == null ? null : tarea.getCat().getId(),
            tarea.getCat() == null ? null : tarea.getCat().getName(),
            tarea.getColony() == null ? null : tarea.getColony().getId(),
            tarea.getColony() == null ? null : tarea.getColony().getName()
        );
    }
}
