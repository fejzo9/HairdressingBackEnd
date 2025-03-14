package com.hairbooking.reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class ServiceInSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nazivUsluge;

    @Column(nullable = false)
    private int trajanjeUsluge;  // Vrijeme trajanja u minutama

    @Column(nullable = false)
    private BigDecimal cijenaUsluge;

    @ManyToOne
    @JoinColumn(name = "salon_id", nullable = false)
    @JsonIgnore // Spriječava vraćanje cijelog objekta Salon unutar ServiceInSalon
    private Salon salon;

    // GETTERI & SETTERI
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazivUsluge() {
        return nazivUsluge;
    }

    public void setNazivUsluge(String nazivUsluge) {
        this.nazivUsluge = nazivUsluge;
    }

    public int getTrajanjeUsluge() {
        return trajanjeUsluge;
    }

    public void setTrajanjeUsluge(int trajanjeUsluge) {
        this.trajanjeUsluge = trajanjeUsluge;
    }

    public BigDecimal getCijenaUsluge() {
        return cijenaUsluge;
    }

    public void setCijenaUsluge(BigDecimal cijenaUsluge) {
        this.cijenaUsluge = cijenaUsluge;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }
}

