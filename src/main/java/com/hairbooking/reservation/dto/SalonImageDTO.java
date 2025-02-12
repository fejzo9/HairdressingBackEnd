package com.hairbooking.reservation.dto;

public class SalonImageDTO {
    private final byte[] imageData;
    private final String imageType;

    public SalonImageDTO(byte[] imageData, String imageType) {
        this.imageData = imageData;
        this.imageType = imageType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getImageType() {
        return imageType;
    }
}

