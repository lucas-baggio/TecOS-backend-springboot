package br.com.baggiotech.tecos_api.presentation.mapper.user;

import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getCompany() != null ? user.getCompany().getId() : null,
                user.getCompany() != null ? user.getCompany().getName() : null,
                user.getName(),
                user.getEmail(),
                user.getType(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
