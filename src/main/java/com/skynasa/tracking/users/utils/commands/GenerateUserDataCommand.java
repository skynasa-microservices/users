package com.skynasa.tracking.users.utils.commands;

import com.skynasa.tracking.users.model.dto.UserDto;
import com.skynasa.tracking.users.model.entity.User;
import com.skynasa.tracking.users.repository.UserRepository;
import com.skynasa.tracking.users.service.UserService;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
// import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ShellComponent
@AllArgsConstructor
@NoArgsConstructor
public class GenerateUserDataCommand {

    @Autowired
    private UserService userService;

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

    @ShellMethod(key = "dummy", value = "Generate dummy users transactions")
    public String generateReservationData(@ShellOption(defaultValue = "10") int count) {

        Faker faker = new Faker();
        // List<UserDto> parents = new ArrayList<>();

        Optional<User> user = userRepository.findByUsername("admin");
        UserDto userDto = user.map(UserDto::toDto).orElse(null);
        if (userDto != null) {
            return "Done";
        }

        UserDto admin = new UserDto();
        admin.setName("admin");
        admin.setIdentifyNumber(faker.idNumber().valid());
        admin.setUsername("admin");
        admin.setEmail(faker.internet().emailAddress());
        admin.setPhoneCode(faker.phoneNumber().phoneNumber().substring(0, 4));
        admin.setPhone(faker.phoneNumber().phoneNumber());
        admin.setRole("admin"); // Set role as "company"
        admin.setStatus(faker.bool().bool());
        admin.setPassword("skynasa159");
        admin.setAddress(faker.address().fullAddress());
        admin.setSubscriptionStartDate(LocalDate.now().minusDays(faker.number().numberBetween(1, 365)));
        admin.setTimezone(faker.address().timeZone());
        admin.setLocale("en");
        admin.setParentId(null); // No companyId for company role

        try {

            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakAuthServerUrl)
                    .realm(keycloakRealm)
                    .clientId(keycloakClientId)
                    .username(adminUsername)
                    .password(adminPassword)
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            // Fetch users resource
            UsersResource usersResource = keycloak.realm(keycloakRealm).users();

            // Search for user by username
            List<UserRepresentation> users = usersResource.search("admin", 0, 1);
            if (users.isEmpty()) {
                System.out.println("User not found.");
            } else {
                UserRepresentation keycloakUser = users.get(0);
                System.out.println("Keycloak User ID: " + keycloakUser.getId());

                admin.setKeycloakUserId(keycloakUser.getId());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save the company user and add to the list
        userService.update(admin);
        // userRepository.save(User.toEntity(admin));

        // // Step 1: Create 10 Company users
        // for (int i = 0; i < 10; i++) {
        //     UserDto parentUser = new UserDto();
        //     parentUser.setName(faker.name().fullName());
        //     parentUser.setIdentifyNumber(faker.idNumber().valid());
        //     parentUser.setUsername(faker.name().username());
        //     parentUser.setEmail(faker.internet().emailAddress());
        //     parentUser.setPhoneCode(faker.phoneNumber().phoneNumber().substring(0, 4));
        //     parentUser.setPhone(faker.phoneNumber().phoneNumber());
        //     parentUser.setRole("user"); // Set role as "user"
        //     parentUser.setStatus(faker.bool().bool());
        //     parentUser.setPassword(faker.internet().password());
        //     parentUser.setAddress(faker.address().fullAddress());
        //     parentUser.setSubscriptionStartDate(LocalDate.now().minusDays(faker.number().numberBetween(1, 365)));
        //     parentUser.setTimezone(faker.address().timeZone());
        //     parentUser.setLocale("en");
        //     parentUser.setParentId(null); // No userId for user role
        //     // parentUser.setKeycloakUserId(faker.internet().uuid());

        //     // Save the company user and add to the list
        //     parentUser = userService.create(parentUser);
        //     // parentUser = UserDto.toDto(userRepository.save(User.toEntity(parentUser)));

        //     parents.add(parentUser);
        // }

        // // Step 2: Create 10 Branch users for each Company user
        // for (UserDto parent : parents) {
        //     for (int i = 0; i < 5; i++) {
        //         UserDto childUser = new UserDto();
        //         childUser.setName(faker.name().fullName());
        //         childUser.setIdentifyNumber(faker.idNumber().valid());
        //         childUser.setUsername(faker.name().username());
        //         childUser.setEmail(faker.internet().emailAddress());
        //         childUser.setPhoneCode(faker.phoneNumber().phoneNumber().substring(0, 4));
        //         childUser.setPhone(faker.phoneNumber().phoneNumber());
        //         childUser.setRole("user"); // Set role as "user"
        //         childUser.setStatus(faker.bool().bool());
        //         childUser.setPassword(faker.internet().password());
        //         childUser.setAddress(faker.address().fullAddress());
        //         childUser.setSubscriptionStartDate(LocalDate.now().minusDays(faker.number().numberBetween(1, 365)));
        //         childUser.setTimezone(faker.address().timeZone());
        //         childUser.setLocale("en");
        //         childUser.setParentId(parent.getId()); // Set the companyId to the corresponding company user
        //         // childUser.setKeycloakUserId(faker.internet().uuid());

        //         // Save the branch user and add to the list
        //         childUser = userService.create(childUser);
        //         // childUser = UserDto.toDto(userRepository.save(User.toEntity(childUser)));

        //     }
        // }

        return count + " users transactions generated successfully!";
    }
}
