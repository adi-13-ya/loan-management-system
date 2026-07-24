package com.bankloan.service;

import com.bankloan.dto.DocumentDTO;
import com.bankloan.exception.ResourceNotFoundException;
import com.bankloan.model.entity.Document;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.enums.DocType;
import com.bankloan.repository.DocumentRepository;
import com.bankloan.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/jpg"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public DocumentDTO uploadDocument(Long loanId, MultipartFile file, DocType docType) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Allowed: PDF, JPG, PNG");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        try {
            Path uploadPath = Paths.get(uploadDir, String.valueOf(loanId));
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            Document doc = Document.builder()
                    .loanApplication(loan)
                    .docType(docType)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .build();

            doc = documentRepository.save(doc);
            return toDTO(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public List<DocumentDTO> getDocumentsForLoan(Long loanId) {
        return documentRepository.findByLoanApplicationId(loanId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private DocumentDTO toDTO(Document doc) {
        return DocumentDTO.builder()
                .id(doc.getId())
                .docType(doc.getDocType())
                .fileName(doc.getFileName())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
