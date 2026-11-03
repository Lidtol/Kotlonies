package com.kotlonies_api.dto;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import java.time.LocalDate;

public record CatResponse(
        Long id, String name, String sex, LocalDate birthDate, String color,
        String status, Boolean neutered, Boolean identified,
        Long colonyId, String colonyName, String observations
) {
    public static CatResponse from(Cat cat) {
        Colony colony = cat.getColony();
        return new CatResponse(
                cat.getId(), cat.getName(), cat.getSex(), cat.getBirthDate(), cat.getColor(),
                cat.getStatus(), cat.getNeutered(), cat.getIdentified(),
                colony == null ? null : colony.getId(),
                colony == null ? null : colony.getName(),
                cat.getObservations());
    }
}