package com.kotlonies_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cats")
public class Cat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String sex;

    private LocalDate birthDate;

    @Column(length = 100)
    private String color;

    @Column(nullable = false, length = 30)
    private String status;

    private Boolean neutered;
    private Boolean identified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colony_id")
    private Colony colony;

    @Column(length = 1000)
    private String observations;

    public Cat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getNeutered() { return neutered; }
    public void setNeutered(Boolean neutered) { this.neutered = neutered; }
    public Boolean getIdentified() { return identified; }
    public void setIdentified(Boolean identified) { this.identified = identified; }
    public Colony getColony() { return colony; }
    public void setColony(Colony colony) { this.colony = colony; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}