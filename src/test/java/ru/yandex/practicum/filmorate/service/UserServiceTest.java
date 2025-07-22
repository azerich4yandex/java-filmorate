package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Работа с хранилищем пользователей")
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserServiceTest {

    private final UserService userService;

    private User user1 = User.builder()
            .email("1@ya.ru")
            .login("login1")
            .name("Name1")
            .birthday(LocalDate.now().minusYears(20))
            .build();

    private User user2 = User.builder()
            .email("2@ya.ru")
            .login("login2")
            .name("Name2")
            .birthday(LocalDate.now().minusYears(21))
            .build();

    private User user3 = User.builder()
            .email("3@ya.ru")
            .login("login3")
            .name("Name3")
            .birthday(LocalDate.now().minusYears(22))
            .build();

    @DisplayName("Создание пользователя")
    @Test
    public void createUserTest() {
        UserDto addedDto = userService.create(UserMapper.mapToNewUserRequest(user1));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user1 = UserMapper.mapToUser(addedDto);
        assertNotNull(user1);
        assertNotNull(user1.getId());
        assertEquals(user1.getId(), addedDto.getId());
    }

    @DisplayName("Получение списка пользователей")
    @Test
    public void getAllUsersTest() {
        UserDto addedDto = userService.create(UserMapper.mapToNewUserRequest(user2));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user2 = UserMapper.mapToUser(addedDto);
        assertNotNull(user2);
        assertNotNull(user2.getId());
        assertEquals(user2.getId(), addedDto.getId());

        addedDto = userService.create(UserMapper.mapToNewUserRequest(user3));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user3 = UserMapper.mapToUser(addedDto);
        assertNotNull(user3);
        assertNotNull(user3.getId());
        assertNotEquals(user2.getId(), user3.getId());

        Collection<UserDto> allUsers = userService.findAll(10, 0);
        assertEquals(2, allUsers.size());
    }

    @DisplayName("Удаление пользователя")
    @Test
    public void deleteUserTest() {
        UserDto addedDto = userService.create(UserMapper.mapToNewUserRequest(user1));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user1 = UserMapper.mapToUser(addedDto);
        assertNotNull(user1);
        assertNotNull(user1.getId());

        Collection<UserDto> beforeDelete = userService.findAll(10, 0);
        userService.deleteUser(user1.getId());
        Collection<UserDto> afterDelete = userService.findAll(10, 0);
        assertNotEquals(beforeDelete.size(), afterDelete.size());
    }

    @DisplayName("Операции с дружбой пользователей")
    @Test
    public void friendshipTest() {
        UserDto addedDto = userService.create(UserMapper.mapToNewUserRequest(user1));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user1 = UserMapper.mapToUser(addedDto);
        assertNotNull(user1);
        assertNotNull(user1.getId());

        addedDto = userService.create(UserMapper.mapToNewUserRequest(user2));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        user2 = UserMapper.mapToUser(addedDto);
        assertNotNull(user2);
        assertNotNull(user2.getId());
        assertNotEquals(user1.getId(), user2.getId());

        userService.addFriend(user1.getId(), user2.getId());

        Collection<UserDto> friendsBeforeDelete = userService.findFriends(user1.getId());

        userService.removeFriend(user1.getId(), user2.getId());

        Collection<UserDto> friendsAfterDelete = userService.findFriends(user1.getId());

        assertNotEquals(friendsBeforeDelete, friendsAfterDelete);
    }
}
