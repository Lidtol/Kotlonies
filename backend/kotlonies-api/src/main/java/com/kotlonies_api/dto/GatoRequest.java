
package com.kotlonies_api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class GatoRequest {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(
            regexp = ".*\\S.*",
            message = "El nombre no puede estar vacío"
    )
    private String nombre;

    @Pattern(
            regexp = "(?i)^(MACHO|HEMBRA|DESCONOCIDO)$",
            message = "El sexo debe ser MACHO, HEMBRA o DESCONOCIDO"
    )
    private String sexo;

    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "La fecha de nacimiento debe tener el formato AAAA-MM-DD"
    )
    private String fechaNacimiento;

    @Size(max = 100, message = "El color no puede superar los 100 caracteres")
    private String color;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
    private String estado;

    private Boolean esterilizado;

    private Boolean identificado;

    @Positive(message = "El ID de la colonia debe ser positivo")
    private Long coloniaId;

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    public GatoRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(Boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public Boolean getIdentificado() {
        return identificado;
    }

    public void setIdentificado(Boolean identificado) {
        this.identificado = identificado;
    }

    public Long getColoniaId() {
        return coloniaId;
    }

    public void setColoniaId(Long coloniaId) {
        this.coloniaId = coloniaId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
