package com.hairbooking.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentHistoryDTO {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private SalonSummaryDTO salon;
    private HairdresserSummaryDTO hairdresser;
    private ServiceSummaryDTO service;
    private CustomerSummaryDTO customer;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    public AppointmentHistoryDTO(Appointment appointment) {
        this.id = appointment.getId();
        this.date = appointment.getDate();
        this.startTime = appointment.getStartTime();
        this.endTime = appointment.getEndTime();
        this.status = appointment.getStatus();
        this.createdAt = appointment.getCreatedAt();
        this.cancelledAt = appointment.getCancelledAt();
        this.cancellationReason = appointment.getCancellationReason();

        if (appointment.getService() != null) {
            this.service = new ServiceSummaryDTO(appointment.getService());
            if (appointment.getService().getSalon() != null) {
                this.salon = new SalonSummaryDTO(appointment.getService().getSalon());
            }
        }

        if (appointment.getCalendar() != null && appointment.getCalendar().getHairdresser() != null) {
            this.hairdresser = new HairdresserSummaryDTO(appointment.getCalendar().getHairdresser());
        }

        if (appointment.getCustomer() != null) {
            this.customer = new CustomerSummaryDTO(appointment.getCustomer());
        }
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public AppointmentStatus getStatus() { return status; }
    public SalonSummaryDTO getSalon() { return salon; }
    public HairdresserSummaryDTO getHairdresser() { return hairdresser; }
    public ServiceSummaryDTO getService() { return service; }
    public CustomerSummaryDTO getCustomer() { return customer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SalonSummaryDTO {
        private Long id;
        private String name;
        private String address;
        private String phone;

        public SalonSummaryDTO(com.hairbooking.reservation.model.Salon salon) {
            this.id = salon.getId();
            this.name = salon.getName();
            this.address = salon.getAddress();
            this.phone = salon.getPhoneNumber();
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HairdresserSummaryDTO {
        private Long id;
        private String firstName;
        private String lastName;

        public HairdresserSummaryDTO(com.hairbooking.reservation.model.User user) {
            this.id = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
        }

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceSummaryDTO {
        private Long id;
        private String name;
        private int duration;
        private BigDecimal price;

        public ServiceSummaryDTO(com.hairbooking.reservation.model.ServiceInSalon service) {
            this.id = service.getId();
            this.name = service.getNazivUsluge();
            this.duration = service.getTrajanjeUsluge();
            this.price = service.getCijenaUsluge();
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public int getDuration() { return duration; }
        public BigDecimal getPrice() { return price; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerSummaryDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;

        public CustomerSummaryDTO(com.hairbooking.reservation.model.User user) {
            this.id = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhoneNumber();
        }

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
    }
}
