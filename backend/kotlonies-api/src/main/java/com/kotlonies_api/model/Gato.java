
package com.kotlonies_api.model;

public class Gato {

    private Long id;
    private String nombre;
    private String sexo;
    private String fechaNacimiento;
    private String color;
    private String estado;
    private Boolean esterilizado;
    private Boolean identificado;
    private Long coloniaId;
    private String observaciones;

    public Gato() {
    }

    public Gato(
            Long id,
            String nombre,
            String sexo,
            String fechaNacimiento,
            String color,
            String estado,
            Boolean esterilizado,
            Boolean identificado,
            Long coloniaId,
            String observaciones
    ) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.color = color;
        this.estado = estado;
        this.esterilizado = esterilizado;
        this.identificado = identificado;
        this.coloniaId = coloniaId;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
