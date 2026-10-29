package com.kotlonies_api.dto;

public record ColonyResponse(
        Long id,
        String name,
        String description,
        String location,
        boolean active
) {
}