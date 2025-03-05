package com.skynasa.tracking.users.service;

import com.skynasa.tracking.commonpackage.utils.components.GenericSpecification;
import com.skynasa.tracking.commonpackage.utils.components.PaginationUtils;
import com.skynasa.tracking.users.model.dto.UserDto;
import com.skynasa.tracking.users.model.entity.User;
import com.skynasa.tracking.users.repository.UserRepository;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.client-id}")
    private String keycloakClientId;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    public List<UserDto> getCurrentUserChildren() {
        User currentUser = User.toEntity(findCurrentUser());

        List<User> users = List.of();
        if (Objects.equals(currentUser.getRole(), "admin")) {
            users = userRepository.getByParentId(currentUser.getId());
        }

        if (!users.contains(currentUser)) {
            users.add(currentUser);
        }

        return users.stream()
                .map(UserDto::toDto)
                .collect(Collectors.toList());
    }

    public Page<UserDto> getAll(Pageable pageable, String search) {
        UserDto user = findCurrentUser();
        Page<User> userPage;

        Specification<User> specification = GenericSpecification.searchAcrossAllFields(search, User.class);

        if (!Objects.equals(user.getRole(), "admin")) {
            specification = specification
                    .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("parentId"), user.getId()));
        }

        userPage = userRepository.findAll(specification, pageable);

        return PaginationUtils.paginate(pageable, userPage, UserDto::toDto);
    }

    public Page<UserDto> getByParentId(UUID parentId, Pageable pageable, String search) {

        Specification<User> specification = GenericSpecification.searchAcrossAllFields(search, User.class);

        specification = specification
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("parentId"), parentId));

        Page<User> userPage = userRepository.findAll(specification, pageable);

        // Page<User> userPage = userRepository.getByParentId(parentId, pageable);
        return PaginationUtils.paginate(pageable, userPage, UserDto::toDto);

    }

    public List<UserDto> getUserWithChildren(UUID id) {
        UserDto userDto = findById(id);
        if (userDto == null) {
            return null;
        }

        User user = User.toEntity(userDto);

        List<User> users = List.of();
        if (Objects.equals(user.getRole(), "admin")) {
            return null;
        }

        users = userRepository.getByParentId(user.getId());
        if (!users.contains(user)) {
            users.add(user);
        }

        return users.stream()
                .map(UserDto::toDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(UUID id) {
        Optional<User> user = userRepository.findById(id);

        return user.map(UserDto::toDto).orElse(null);
    }

    @Transactional
    public UserDto create(UserDto userDto) {

        // Create user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getName());
        user.setLastName(userDto.getName());
        user.setEmailVerified(true);
        user.setEnabled(true);

        // Create password credentials
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        // credential.setValue(userDto.getPassword());

        // Set credentials to the user
        user.setCredentials(Collections.singletonList(credential));

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakAuthServerUrl)
                .realm(keycloakRealm)
                .clientId(keycloakClientId)
                .username(adminUsername)
                .password(adminPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        // Create user in Keycloak
        Response response = keycloak.realm(keycloakRealm).users().create(user);

        if (response.getStatus() == 201) {
            // User created successfully
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            userDto.setKeycloakUserId(userId);

            // Step 2: Fetch the role representation for the single role
            String roleName = userDto.getRole(); // Assuming role is passed in userDto
            RoleRepresentation roleRepresentation = keycloak.realm(keycloakRealm)
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            // Step 3: Assign the single role to the user
            keycloak.realm(keycloakRealm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(roleRepresentation));
            return UserDto.toDto(userRepository.save(User.toEntity(userDto)));
        }

        return null;
    }

    @Transactional
    public UserDto update(UserDto userDto) {

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakAuthServerUrl)
                .realm(keycloakRealm)
                .clientId(keycloakClientId)
                .username(adminUsername)
                .password(adminPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        // Step 1: Get the user by Keycloak User ID
        UserResource userResource = keycloak.realm(keycloakRealm).users().get(userDto.getKeycloakUserId());
        UserRepresentation keycloakUser = userResource.toRepresentation();
        keycloakUser.setUsername(userDto.getUsername());
        keycloakUser.setEmail(userDto.getEmail());
        keycloakUser.setFirstName(userDto.getName());
        keycloakUser.setLastName(userDto.getName());
        keycloakUser.setEmailVerified(true);
        keycloakUser.setEnabled(true);

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userDto.getPassword());
            userResource.resetPassword(credential);
        }

        String oldRole = "";
        // Step 4: Update the user’s role
        // Fetch the current role(s) assigned to the user
        List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();
        if (!currentRoles.isEmpty()) {
            oldRole = currentRoles.getFirst().getName();
        }

        if (userDto.getRole() != null && !userDto.getRole().isEmpty() && !userDto.getRole().equals(oldRole)) {
            // Remove the current role(s)
            userResource.roles().realmLevel().remove(currentRoles);

            // Assign the new role
            RoleRepresentation newRole = keycloak.realm(keycloakRealm)
                    .roles()
                    .get(userDto.getRole()) // Assuming `getRole()` returns the role name as a String
                    .toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(newRole));
        }

        userResource.update(keycloakUser);

        return UserDto.toDto(userRepository.save(User.toEntity(userDto)));

    }

    public void delete(UUID id) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakAuthServerUrl)
                    .realm(keycloakRealm)
                    .clientId(keycloakClientId)
                    .username(adminUsername)
                    .password(adminPassword)
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            // Get the UserResource for the user by ID
            UserResource userResource = keycloak.realm(keycloakRealm).users().get(user.getKeycloakUserId());

            // Delete the user
            userResource.remove();
        }

        userRepository.deleteById(id);
    }

    public UserDto findCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakUserId = jwt.getClaim("sub");

        Optional<User> user = userRepository.findByKeycloakUserId(keycloakUserId);

        return UserDto.toDto(user.get());
    }


    public UserDto findUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(UserDto::toDto).orElse(null);
    }

    public UserDto findByKeycloakUserId(String keycloakUserId) {
        Optional<User> user = userRepository.findByKeycloakUserId(keycloakUserId);

        return user.map(UserDto::toDto).orElse(null);
    }
}
