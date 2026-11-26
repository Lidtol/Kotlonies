package com.kotlonies_api.exception;

/**
 * Caso concreto de recurso no encontrado: la colonia no existe.
 * Al heredar de RecursoNoEncontradoException, el GlobalExceptionHandler
 * la convierte en un 404 igual que el resto.
 */
public class ColonyNotFoundException extends RecursoNoEncontradoException {

    public ColonyNotFoundException(Long id) {
        super("No existe la colonia con id " + id);
    }
}
