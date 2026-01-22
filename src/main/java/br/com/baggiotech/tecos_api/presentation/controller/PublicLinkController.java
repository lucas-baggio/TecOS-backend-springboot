package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.publiclink.*;
import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.*;
import br.com.baggiotech.tecos_api.presentation.mapper.publiclink.PublicLinkMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public-links")
public class PublicLinkController {

    private final ListPublicLinksUseCase listPublicLinksUseCase;
    private final CreatePublicLinkUseCase createPublicLinkUseCase;
    private final GetPublicLinkByIdUseCase getPublicLinkByIdUseCase;
    private final DeletePublicLinkUseCase deletePublicLinkUseCase;
    private final PublicLinkMapper mapper;

    public PublicLinkController(
            ListPublicLinksUseCase listPublicLinksUseCase,
            CreatePublicLinkUseCase createPublicLinkUseCase,
            GetPublicLinkByIdUseCase getPublicLinkByIdUseCase,
            DeletePublicLinkUseCase deletePublicLinkUseCase,
            PublicLinkMapper mapper) {
        this.listPublicLinksUseCase = listPublicLinksUseCase;
        this.createPublicLinkUseCase = createPublicLinkUseCase;
        this.getPublicLinkByIdUseCase = getPublicLinkByIdUseCase;
        this.deletePublicLinkUseCase = deletePublicLinkUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<PublicLinkResponse>> index(
            @RequestParam(required = false) UUID workOrderId) {
        
        List<PublicLink> links = listPublicLinksUseCase.execute(workOrderId);
        List<PublicLinkResponse> responses = links.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<PublicLinkResponse> store(
            @Valid @RequestBody PublicLinkRequest request,
            @RequestParam UUID companyId) {
        
        PublicLink publicLink = createPublicLinkUseCase.execute(
                request.workOrderId(),
                companyId
        );
        
        PublicLinkResponse response = mapper.toResponse(publicLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicLinkResponse> show(@PathVariable UUID id) {
        PublicLink publicLink = getPublicLinkByIdUseCase.execute(id);
        PublicLinkResponse response = mapper.toResponse(publicLink);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(
            @PathVariable UUID id,
            @RequestParam UUID companyId) {
        deletePublicLinkUseCase.execute(id, companyId);
        return ResponseEntity.ok(Map.of("message", "Link público excluído com sucesso."));
    }
}
