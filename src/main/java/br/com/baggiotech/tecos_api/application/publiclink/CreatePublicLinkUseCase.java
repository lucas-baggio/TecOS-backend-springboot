package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreatePublicLinkUseCase {

    private final PublicLinkRepository repository;
    private final WorkOrderRepository workOrderRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CreatePublicLinkUseCase(PublicLinkRepository repository,
                                  WorkOrderRepository workOrderRepository) {
        this.repository = repository;
        this.workOrderRepository = workOrderRepository;
    }

    public PublicLink execute(UUID workOrderId, UUID companyId) {
        // Buscar work order
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder", workOrderId));

        // Verificar se work_order pertence à mesma company
        if (workOrder.getCompany() == null || !workOrder.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("A ordem de serviço não pertence à sua empresa.");
        }

        // Gerar token único e seguro (RB-06)
        String token = generateUniqueToken();

        // Criar link público
        PublicLink publicLink = new PublicLink();
        publicLink.setId(UUID.randomUUID());
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken(token);
        publicLink.setCreatedAt(LocalDateTime.now());
        publicLink.setUpdatedAt(LocalDateTime.now());

        return repository.save(publicLink);
    }

    private String generateUniqueToken() {
        String token;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            // Combina UUID + timestamp + random string para garantir unicidade
            String input = UUID.randomUUID().toString() +
                    System.currentTimeMillis() +
                    generateRandomString(32);
            
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                token = hexString.toString();
            } catch (Exception e) {
                // Fallback para UUID simples se SHA-256 falhar
                token = UUID.randomUUID().toString().replace("-", "") +
                        System.currentTimeMillis() +
                        generateRandomString(16);
            }

            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Não foi possível gerar um token único após " + maxAttempts + " tentativas.");
            }
        } while (repository.existsByToken(token));

        return token;
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
