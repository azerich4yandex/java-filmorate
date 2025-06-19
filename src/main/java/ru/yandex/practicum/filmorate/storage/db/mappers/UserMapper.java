package ru.yandex.practicum.filmorate.storage.db.mappers;

import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewUserRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.UserDto;
import ru.yandex.practicum.filmorate.storage.db.dto.read.UserShortDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateUserRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User mapToUser(NewUserRequest request) {

        return User.builder().email(request.getEmail()).login(request.getLogin()).name(request.getName())
                .birthday(request.getBirthday()).build();
    }

    public static User mapToUser(UserDto dto) {
        User user = User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .login(dto.getLogin())
                .name(dto.getName())
                .birthday(dto.getBirthday())
                .build();

        Set<Long> friends = new HashSet<>();
        if (!(dto.getFriends().isEmpty())) {
            for (UserShortDto friend : dto.getFriends()) {
                friends.add(friend.getId());
            }
        }
        user.setFriends(friends);

        return user;
    }

    public static UserDto mapToUserDto(User user) {

        return UserDto.builder().id(user.getId()).email(user.getEmail()).login(user.getLogin())
                .name(user.getName()).birthday(user.getBirthday()).build();
    }

    public static UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static NewUserRequest mapToNewUserRequest(User user) {

        return NewUserRequest.builder()
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public static UpdateUserRequest mapToUpdateUserRequest(User user) {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        Set<UserDto> friends = new HashSet<>();
        if (!(user.getFriends().isEmpty())) {
            for (Long friendId : user.getFriends()) {
                friends.add(UserDto.builder().id(friendId).build());
            }
        }

        request.setFriends(friends);

        return request;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasId()) {
            user.setId(request.getId());
        }

        if (request.hasEmail()) {
            user.setEmail(request.getEmail());
        }
        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }
        if (request.hasName()) {
            user.setName(request.getName());
        }
        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }
        if (request.hasFriends()) {
            user.setFriends(request.getFriends().stream().map(UserDto::getId).toList());
        }

        return user;
    }
}
