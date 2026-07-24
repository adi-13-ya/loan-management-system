package com.bankloan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotBlank
    private String city;
    private Long managerId;
}
