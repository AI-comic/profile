package kopo.aisw.springboot_basic.service;

import  kopo.aisw.springboot_basic.domain.User;
import  kopo.aisw.springboot_basic.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        user.setUserAuth("USER");
        userMapper.save(user);
    }

    public User getUserById(String userId) {
        return userMapper.findById(userId);
    }

    public void updateUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        user.setUserAuth("USER");
        userMapper.update(user);
    }

    public void deleteUser(String userId) {
        userMapper.delete(userId);
    }

    public boolean isUserIdExists(String userId) {
        return userMapper.findById(userId) != null;
    }
}
