package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.Service;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("naziv_usluge") // 🔹 Definišemo JSON ključ
    private String nazivUsluge;

    @JsonProperty("trajanje_usluge")
    private int trajanjeUsluge;

    @JsonProperty("cijena_usluge")
    private BigDecimal cijenaUsluge;

    @JsonProperty("salon_id")
    private Long salonId; // Umjesto cijelog salona, čuvamo samo ID

    public ServiceDTO() {
        // Prazan konstruktor potreban za Jackson
    }

    @JsonCreator
    public ServiceDTO(@JsonProperty("nazivUsluge") String nazivUsluge,
                      @JsonProperty("trajanjeUsluge") int trajanjeUsluge,
                      @JsonProperty("cijenaUsluge") BigDecimal cijenaUsluge) {
        this.nazivUsluge = nazivUsluge;
        this.trajanjeUsluge = trajanjeUsluge;
        this.cijenaUsluge = cijenaUsluge;
    }

    public ServiceDTO(Service service) {
        this.id = service.getId();
        this.nazivUsluge = service.getNazivUsluge();
        this.trajanjeUsluge = service.getTrajanjeUsluge();
        this.cijenaUsluge = service.getCijenaUsluge();
        this.salonId = service.getSalon().getId();
    }

    public Service toEntity(Salon salon) {
        Service service = new Service();
        service.setNazivUsluge(this.nazivUsluge);
        service.setTrajanjeUsluge(this.trajanjeUsluge);
        service.setCijenaUsluge(this.cijenaUsluge);
        service.setSalon(salon);
        return service;
    }
}

