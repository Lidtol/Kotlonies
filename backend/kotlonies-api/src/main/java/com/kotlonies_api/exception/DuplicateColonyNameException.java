package com.kotlonies_api.exception;

public class DuplicateColonyNameException extends RuntimeException {

    public DuplicateColonyNameException(String name) {
        super("Ya existe una colonia con el nombre \"" + name + "\"");
    }
}