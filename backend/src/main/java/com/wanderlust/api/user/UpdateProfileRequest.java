package com.wanderlust.api.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 100)
    private String displayName;

    @Size(max = 1000)
    private String bio;

    @Size(max = 500)
    private String avatarUrl;

    private TravelStyle travelStyle;
}
