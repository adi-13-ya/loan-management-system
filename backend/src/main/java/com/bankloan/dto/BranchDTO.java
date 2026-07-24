package com.bankloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private Long id;
    private String name;
    private String code;
    private String city;
    private Long managerId;
    private String managerName;
}
