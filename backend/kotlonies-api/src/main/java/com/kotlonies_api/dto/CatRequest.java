
package com.kotlonies_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CatRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Pattern(
        regexp = ".*\\S.*",
        message = "Name cannot be blank"
    )
    private String name;

    @Pattern(
        regexp = "(?i)^(MALE|FEMALE|UNKNOWN)$",
        message = "Sex must be MALE, FEMALE or UNKNOWN"
    )
    private String sex;

    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Birth date must use the format YYYY-MM-DD"
    )
    private String birthDate;

    @Size(max = 100, message = "Color cannot exceed 100 characters")
    private String color;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status cannot exceed 30 characters")
    private String status;

    private Boolean neutered;

    private Boolean identified;

    @Positive(message = "Colony ID must be positive")
    private Long colonyId;

    @Size(max = 1000, message = "Observations cannot exceed 1000 characters")
    private String observations;

    public CatRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getNeutered() {
        return neutered;
    }

    public void setNeutered(Boolean neutered) {
        this.neutered = neutered;
    }

    public Boolean getIdentified() {
        return identified;
    }

    public void setIdentified(Boolean identified) {
        this.identified = identified;
    }

    public Long getColonyId() {
        return colonyId;
    }

    public void setColonyId(Long colonyId) {
        this.colonyId = colonyId;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
