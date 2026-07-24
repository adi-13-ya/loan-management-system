package com.bankloan.controller;

import com.bankloan.dto.DocumentDTO;
import com.bankloan.model.enums.DocType;
import com.bankloan.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/loans/{loanId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDTO> uploadDocument(
            @PathVariable Long loanId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") DocType docType) {
        return ResponseEntity.ok(documentService.uploadDocument(loanId, file, docType));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable Long loanId) {
        return ResponseEntity.ok(documentService.getDocumentsForLoan(loanId));
    }
}
