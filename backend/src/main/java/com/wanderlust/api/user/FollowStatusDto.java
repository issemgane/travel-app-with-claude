package com.wanderlust.api.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowStatusDto {
    private boolean following;
}
