package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ElementNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    final long userId = 1L;
    final String userName = "new User";
    final String email = "newUser@mail.com";

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor;

    @Test
    void addUser_whenAddUser_thenUserAdded() {
        UserDto newUserDto = new UserDto(userId, userName, email);
        User newUser = new User(userId, userName, email);
        when(userMapper.toUser(newUserDto)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userMapper.toUserDto(newUser)).thenReturn(newUserDto);

        UserDto addedUserDto = userService.addUser(newUserDto);

        assertEquals(addedUserDto, newUserDto);
        verify(userMapper).toUser(newUserDto);
        verify(userRepository).save(newUser);
        verify(userMapper).toUserDto(newUser);
    }

    @Test
    void getUserById_whenUserFound_thenReturnedUser() {
        User expectedUser = new User();
        UserDto expectedUserDto = new UserDto();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(userMapper.toUserDto(expectedUser)).thenReturn(expectedUserDto);

        UserDto returnedUserDto = userService.getUserById(userId);

        assertEquals(expectedUserDto, returnedUserDto);
        verify(userRepository).findById(userId);
        verify(userMapper).toUserDto(expectedUser);
    }

    @Test
    void getUserById_whenUserNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> userService.getUserById(userId));
    }


    @Test
    void updateUser_whenUserFoundAndNewNameAndNewEmailNotNull_thenUpdateNameAndEmailOnly() {
        UserDto newUserDto = new UserDto(userId + 1, "new" + userName, "new" + email);
        User oldUser = new User(userId, userName, email);
        User newUser = new User(
                newUserDto.getId(),
                newUserDto.getName(),
                newUserDto.getEmail()
        );
        User userAfter = new User(
                oldUser.getId(),
                newUser.getName(),
                newUser.getEmail()
        );
        UserDto userDtoAfter = new UserDto(
                userAfter.getId(),
                userAfter.getName(),
                userAfter.getEmail()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userMapper.toUser(newUserDto)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(userAfter);
        when(userMapper.toUserDto(userAfter)).thenReturn(userDtoAfter);

        UserDto returnedUserDto = userService.updateUser(userId, newUserDto);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userForSave = userArgumentCaptor.getValue();

        assertEquals(userDtoAfter, returnedUserDto);
        assertNotEquals(newUserDto.getId(), userForSave.getId());
        assertEquals(oldUser.getId(), userForSave.getId());
        assertEquals(newUserDto.getName(), userForSave.getName());
        assertEquals(newUserDto.getEmail(), userForSave.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toUser(newUserDto);
        verify(userMapper).toUserDto(userAfter);
    }

    @Test
    void updateUser_whenUserFoundAndNewNameNullAndNewEmailNotNull_thenUpdateEmailOnly() {
        UserDto newUserDto = new UserDto();
        newUserDto.setId(userId + 1);
        newUserDto.setName(null);
        newUserDto.setEmail("new" + email);

        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName(userName);
        oldUser.setEmail(email);

        User newUser = new User();
        newUser.setId(userId + 1);
        newUser.setName(null);
        newUser.setEmail("new" + email);

        User userAfter = new User();
        userAfter.setId(userId);
        userAfter.setName(userName);
        userAfter.setEmail("new" + email);

        UserDto userDtoAfter = new UserDto();
        userDtoAfter.setId(userId);
        userDtoAfter.setName(userName);
        userDtoAfter.setEmail("new" + email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userMapper.toUser(newUserDto)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(userAfter);
        when(userMapper.toUserDto(userAfter)).thenReturn(userDtoAfter);

        UserDto returnedUserDto = userService.updateUser(userId, newUserDto);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userForSave = userArgumentCaptor.getValue();

        assertEquals(userDtoAfter, returnedUserDto);
        assertNotEquals(newUserDto.getId(), userForSave.getId());
        assertEquals(oldUser.getId(), userForSave.getId());
        assertEquals(oldUser.getName(), userForSave.getName());
        assertEquals(newUserDto.getEmail(), userForSave.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toUser(newUserDto);
        verify(userMapper).toUserDto(userAfter);
    }

    @Test
    void updateUser_whenUserFoundAndNewNameNullAndNewEmailNull_thenNotUpdateNameAndMail() {
        UserDto newUserDto = new UserDto();
        newUserDto.setId(userId + 1);
        newUserDto.setName(null);
        newUserDto.setEmail(null);

        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName(userName);
        oldUser.setEmail(email);

        User newUser = new User();
        newUser.setId(userId + 1);
        newUser.setName(null);
        newUser.setEmail(null);

        User userAfter = new User();
        userAfter.setId(userId);
        userAfter.setName(userName);
        userAfter.setEmail(email);

        UserDto userDtoAfter = new UserDto();
        userDtoAfter.setId(userId);
        userDtoAfter.setName(userName);
        userDtoAfter.setEmail(email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userMapper.toUser(newUserDto)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(userAfter);
        when(userMapper.toUserDto(userAfter)).thenReturn(userDtoAfter);

        UserDto returnedUserDto = userService.updateUser(userId, newUserDto);
        verify(userRepository).save(userArgumentCaptor.capture());
        User userForSave = userArgumentCaptor.getValue();

        assertEquals(userDtoAfter, returnedUserDto);
        assertNotEquals(newUserDto.getId(), userForSave.getId());
        assertEquals(oldUser.getId(), userForSave.getId());
        assertEquals(oldUser.getName(), userForSave.getName());
        assertEquals(oldUser.getEmail(), userForSave.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toUser(newUserDto);
        verify(userMapper).toUserDto(userAfter);
    }

    @Test
    void getAllUsers_whenNeedToReturnAllUsers_returnAllUsers() {
        User user1 = new User(userId + 1, "1" + userName, "1" + email);
        User user2 = new User(userId + 2, "2" + userName, "2" + email);
        User user3 = new User(userId + 3, "3" + userName, "3" + email);
        List<User> userList = List.of(user1, user2, user3);
        UserDto userDto1 = new UserDto(userId + 1, "1" + userName, "1" + email);
        UserDto userDto2 = new UserDto(userId + 2, "2" + userName, "2" + email);
        UserDto userDto3 = new UserDto(userId + 3, "3" + userName, "3" + email);
        List<UserDto> userDtoList = List.of(userDto1, userDto2, userDto3);
        when(userRepository.findAll()).thenReturn((userList));
        when(userMapper.map(userList)).thenReturn(userDtoList);

        List<UserDto> returnedUsersDto = userService.getAllUsers();

        assertEquals(userDtoList, returnedUsersDto);
        verify(userRepository).findAll();
        verify(userMapper).map(userList);
    }

    @Test
    void updateUser_whenUserNotFound_thenNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ElementNotFoundException.class,
                () -> userService.updateUser(userId, new UserDto()));
    }

    @Test
    void deleteUserById() {
        userService.deleteUserById(userId);

        verify(userRepository).deleteById(longArgumentCaptor.capture());
        Long idForDelete = longArgumentCaptor.getValue();
        assertEquals(userId, idForDelete);
    }
}
