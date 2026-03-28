package com.wanderlust.api.itinerary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateItineraryRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    private String[] countryCodes;

    @NotNull(message = "Duration in days is required")
    @Min(1)
    private Integer durationDays;

    private BigDecimal estimatedBudgetUsd;

    private String coverImageUrl;

    private Boolean isPublished = false;
}
