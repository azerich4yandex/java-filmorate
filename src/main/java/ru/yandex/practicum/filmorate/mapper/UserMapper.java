package ru.yandex.practicum.filmorate.mapper;

import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserShortDto;
import ru.yandex.practicum.filmorate.model.User;

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

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail().trim())
                .login(user.getLogin().trim())
                .name(user.getName().trim())
                .birthday(user.getBirthday())
                .build();
    }

    public static UserShortDto mapToUserShortDto(User user) {
        int mark = user.getMark() > 0 ? user.getMark() : 0;

        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName().trim())
                .mark(mark)
                .build();
    }

    public static NewUserRequest mapToNewUserRequest(User user) {

        return NewUserRequest.builder()
                .email(user.getEmail().trim())
                .login(user.getLogin().trim())
                .name(user.getName().trim())
                .birthday(user.getBirthday())
                .build();
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasId()) {
            user.setId(request.getId());
        }

        if (request.hasEmail()) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.hasLogin()) {
            user.setLogin(request.getLogin().trim());
        }
        if (request.hasName()) {
            user.setName(request.getName().trim());
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
