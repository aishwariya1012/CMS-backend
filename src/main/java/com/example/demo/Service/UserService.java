package com.example.demo.Service;



import com.example.demo.dto.*;
import com.example.demo.Exception.BadRequestException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final FileStorageService fileStorageService;
    private final UserMapper         userMapper;

    public UserResponse getProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(int userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.getFullName()    != null) user.setFullName(req.getFullName());
        if (req.getAddress()     != null) user.setAddress(req.getAddress());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadProfilePicture(int userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String path = fileStorageService.storeFile(file, "user-" + userId);
        user.setProfilePicture(path);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public ApiResponse<Void> changePassword(int userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash()))
            throw new BadRequestException("Old password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password changed successfully");
    }
}

