package com.bankloan.dto;

import com.bankloan.model.enums.DocType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
    private Long id;
    private DocType docType;
    private String fileName;
    private LocalDateTime uploadedAt;
}
