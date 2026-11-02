package com.kotlonies_api.exception;

public class ColonyNotFoundException extends RuntimeException {

    public ColonyNotFoundException(Long id) {
        super("No existe la colonia con id " + id);
    }
}