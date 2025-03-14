package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.ServiceInSalon;

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

    public ServiceDTO(ServiceInSalon serviceInSalon) {
        this.id = serviceInSalon.getId();
        this.nazivUsluge = serviceInSalon.getNazivUsluge();
        this.trajanjeUsluge = serviceInSalon.getTrajanjeUsluge();
        this.cijenaUsluge = serviceInSalon.getCijenaUsluge();
        this.salonId = serviceInSalon.getSalon().getId();
    }

    public ServiceInSalon toEntity(Salon salon) {
        ServiceInSalon serviceInSalon = new ServiceInSalon();
        serviceInSalon.setNazivUsluge(this.nazivUsluge);
        serviceInSalon.setTrajanjeUsluge(this.trajanjeUsluge);
        serviceInSalon.setCijenaUsluge(this.cijenaUsluge);
        serviceInSalon.setSalon(salon);
        return serviceInSalon;
    }
}

