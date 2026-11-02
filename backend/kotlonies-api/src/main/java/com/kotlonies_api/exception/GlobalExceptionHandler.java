package com.kotlonies_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(
            RecursoNoEncontradoException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Datos no válidos");
        respuesta.put("campos", errores);

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredenciales(
            CredencialesInvalidasException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Autenticación fallida");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta);
    }

    @ExceptionHandler({ AuthenticationException.class, AuthorizationDeniedException.class })
    public ResponseEntity<Map<String, String>> manejarAutenticacion(
            RuntimeException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Autenticación fallida");
        respuesta.put("mensaje", "Correo o contraseña incorrectos");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegado(
            AccessDeniedException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Acceso denegado");
        respuesta.put("mensaje", "No tienes permisos para esta operación");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarIlegal(
            IllegalArgumentException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Petición no válida");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridad(
            org.springframework.dao.DataIntegrityViolationException exception
    ) {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("error", "Petición no válida");
        respuesta.put("mensaje", "Los datos violan las restricciones de la base de datos");

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }
}