package com.skynasa.tracking.users.cotroller;

import com.skynasa.tracking.commonpackage.utils.ApiResponseBuilder;
import com.skynasa.tracking.commonpackage.utils.components.Pagination;
import com.skynasa.tracking.commonpackage.utils.dto.ApiResponse;
import com.skynasa.tracking.users.model.dto.UserDto;
import com.skynasa.tracking.users.service.UserService;
import com.skynasa.tracking.users.utils.commands.GenerateUserDataCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.external-prefix}")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private GenerateUserDataCommand generateUserDataCommand;

    @GetMapping("/get-current-user-with-children")
    public ResponseEntity<ApiResponse<List<UserDto>>> getCurrentUserWithChildren() {

        List<UserDto> results = userService.getCurrentUserChildren();

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "Users fetched successfully"));

    }

    @GetMapping("/index")
    public ResponseEntity<ApiResponse<Page<UserDto>>> index(
            @RequestParam(required = false) String search,
            @ModelAttribute Pagination paginate) {

        Pageable pageable = paginate.toPageable();

        Page<UserDto> results = userService.getAll(pageable, search);

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "Users fetched successfully"));

    }

    @GetMapping("/get-by-parent-id/{parentId}")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getByParentId(@PathVariable UUID parentId,
            @RequestParam(required = false) String search,
            @ModelAttribute Pagination paginate) {

        Pageable pageable = paginate.toPageable();

        Page<UserDto> results = userService.getByParentId(parentId, pageable, search);

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "Users fetched successfully"));

    }

    @GetMapping("/find-current-user")
    public ResponseEntity<ApiResponse<UserDto>> findCurrentUser() {
        UserDto results = userService.findCurrentUser();

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "User fetched successfully"));

    }

    @GetMapping("/show/{id}")
    public ResponseEntity<ApiResponse<UserDto>> show(@PathVariable UUID id) {
        UserDto results = userService.findById(id);
        return ResponseEntity.ok(ApiResponseBuilder.success(results, "User fetched successfully"));

    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserDto>> create(@RequestBody UserDto user) {
        UserDto results = userService.create(user);

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "User created successfully"));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserDto>> update(@RequestBody UserDto user) {
        UserDto results = userService.update(user);

        return ResponseEntity.ok(ApiResponseBuilder.success(results, "User updated successfully"));
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {

        userService.delete(id);

        return ResponseEntity.ok(ApiResponseBuilder.success(null, "User deleted successfully"));

    }

    @GetMapping("/dummy")
    public void dummy() {
        generateUserDataCommand.generateReservationData(10);
    }
}
