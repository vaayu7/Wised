package com.wised.people.dtos;

import com.wised.helpandsettings.enums.ReasonEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@AllArgsConstructor
public class UserBlockResponse {

    private String message;
    private String error;

}
