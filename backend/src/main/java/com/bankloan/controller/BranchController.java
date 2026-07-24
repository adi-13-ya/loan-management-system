package com.bankloan.controller;

import com.bankloan.dto.BranchDTO;
import com.bankloan.model.entity.Branch;
import com.bankloan.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        List<BranchDTO> branches = branchRepository.findAll().stream()
                .map(branch -> BranchDTO.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .code(branch.getCode())
                        .city(branch.getCity())
                        .managerId(branch.getManager() != null ? branch.getManager().getId() : null)
                        .managerName(branch.getManager() != null ? branch.getManager().getName() : null)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }
}
