package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListUsersUseCase {

    private final UserRepository repository;

    public ListUsersUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Page<User> execute(Integer page, Integer size, UUID companyId, Boolean isActive, String type, String search) {
        List<User> allUsers = repository.findAll();

        List<User> filtered = allUsers;

        if (companyId != null) {
            filtered = filtered.stream()
                    .filter(user -> user.getCompany() != null && companyId.equals(user.getCompany().getId()))
                    .toList();
        }

        if (isActive != null) {
            filtered = filtered.stream()
                    .filter(user -> isActive.equals(user.getIsActive()))
                    .toList();
        }

        if (type != null && !type.isBlank()) {
            filtered = filtered.stream()
                    .filter(user -> type.equals(user.getType()))
                    .toList();
        }

        if (search != null && !search.isBlank()) {
            List<User> searchResults = repository.searchByNameOrEmail(search);
            filtered = filtered.stream()
                    .filter(searchResults::contains)
                    .toList();
        }

        List<User> mutableFiltered = new java.util.ArrayList<>(filtered);
        mutableFiltered.sort((u1, u2) -> u1.getName().compareToIgnoreCase(u2.getName()));
        filtered = mutableFiltered;

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<User> pageContent = start < filtered.size() 
                ? filtered.subList(start, end) 
                : List.of();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name").ascending());
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }
}
