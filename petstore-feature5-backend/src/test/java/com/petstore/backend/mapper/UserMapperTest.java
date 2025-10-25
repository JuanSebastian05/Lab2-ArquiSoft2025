package com.petstore.backend.mapper;

import com.petstore.backend.dto.UserResponseDTO;
import com.petstore.backend.entity.Role;
import com.petstore.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = UserMapper.INSTANCE;

    private Role role(int id, String name) {
        Role r = new Role();
        r.setRoleId(id);
        r.setRoleName(name);
        return r;
    }

    private User user(int id, String name, String email, String password, Role role) {
        User u = new User();
        u.setUserId(id);
        u.setUserName(name);
        u.setEmail(email);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }

    @Test
    @DisplayName("toResponseDTO mapea campos básicos y role.roleName -> roleName")
    void toResponseDTO_mapsFields() {
        User entity = user(10, "Juan", "juan@example.com", "secret", role(3, "Marketing Admin"));

        UserResponseDTO dto = mapper.toResponseDTO(entity);

        assertThat(dto.getUserId()).isEqualTo(10);
        assertThat(dto.getUserName()).isEqualTo("Juan");
        assertThat(dto.getEmail()).isEqualTo("juan@example.com");
        assertThat(dto.getRoleName()).isEqualTo("Marketing Admin");
    }

    @Test
    @DisplayName("toResponseDTOList mapea listas de entidades a DTOs")
    void toResponseDTOList_mapsList() {
        List<User> users = List.of(
                user(1, "A", "a@a.com", "x", role(1, "User")),
                user(2, "B", "b@b.com", "y", role(2, "Admin"))
        );

        List<UserResponseDTO> dtos = mapper.toResponseDTOList(users);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getUserId()).isEqualTo(1);
        assertThat(dtos.get(0).getRoleName()).isEqualTo("User");
        assertThat(dtos.get(1).getEmail()).isEqualTo("b@b.com");
        assertThat(dtos.get(1).getRoleName()).isEqualTo("Admin");
    }
}
