package com.wised.auth.dtos;


import com.wised.auth.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @Getter
    @Setter
    private String fullName;
    private  String email;

    private String  password;

    private Role role;
}
