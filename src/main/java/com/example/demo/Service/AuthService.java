package com.example.demo.Service;


import com.example.demo.dto.*;
import com.example.demo.Exception.BadRequestException;
import com.example.demo.Exception.DuplicateResourceException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Exception.UnauthorizedException;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Officer;
import com.example.demo.Model.StationHead;
import com.example.demo.Model.User;
import com.example.demo.Repository.OfficerRepository;
import com.example.demo.Repository.StationHeadRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final OfficerRepository    officerRepository;
    private final StationHeadRepository stationHeadRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final NotificationService  notificationService;
    private final UserMapper           userMapper;


    @Transactional
    public AuthResponse register(RegisterRequest req) {
       
        if (userRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Email is already registered");
        if (userRepository.existsByAadhaarNumber(req.getAadhaarNumber()))
            throw new DuplicateResourceException("Aadhaar number is already registered");
        if (userRepository.existsByPanNumber(req.getPanNumber()))
            throw new DuplicateResourceException("PAN number is already registered");

        User user = userMapper.toEntity(req, passwordEncoder.encode(req.getPassword()));

        User saved = userRepository.save(user);
        log.info("Citizen registered — userId={}, email={}", saved.getUserId(), saved.getEmail());

       
        notificationService.send(
                saved.getUserId(), "CITIZEN", null,
                "ACCOUNT_CREATED",
                "Welcome to the Crime Management System, " + saved.getFullName() + "! Your account has been created successfully.",
                saved.getEmail()
        );

        return AuthResponse.builder()
                .status("success")
                .message("Account created successfully")
                .actorId(saved.getUserId())
                .role("CITIZEN")
                .name(saved.getFullName())
                .build();
    }


    public AuthResponse login(LoginRequest req) {
        String role = req.getRole().toUpperCase();

        return switch (role) {
            case "CITIZEN"      -> loginCitizen(req);
            case "OFFICER"      -> loginOfficer(req);
            case "STATION_HEAD" -> loginStationHead(req);
            default -> throw new BadRequestException("Invalid role: " + req.getRole());
        };
    }

    private AuthResponse loginCitizen(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        String token = jwtUtil.generateToken(user.getUserId(), "CITIZEN");

        notificationService.send(user.getUserId(), "CITIZEN", null,
                "LOGIN_ALERT",
                "You logged in to CMS on " + java.time.LocalDateTime.now(),
                user.getEmail());

        log.info("Citizen login — userId={}", user.getUserId());
        return AuthResponse.builder()
                .status("success").token(token)
                .role("CITIZEN").name(user.getFullName())
                .actorId(user.getUserId()).build();
    }

    private AuthResponse loginOfficer(LoginRequest req) {
        Officer officer = officerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), officer.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        String token = jwtUtil.generateToken(officer.getOfficerId(), "OFFICER");

        notificationService.send(officer.getOfficerId(), "OFFICER", null,
                "LOGIN_ALERT",
                "You logged in to CMS on " + java.time.LocalDateTime.now(),
                officer.getEmail());

        log.info("Officer login — officerId={}", officer.getOfficerId());
        return AuthResponse.builder()
                .status("success").token(token)
                .role("OFFICER").name(officer.getFullName())
                .actorId(officer.getOfficerId()).build();
    }

    private AuthResponse loginStationHead(LoginRequest req) {
        StationHead head = stationHeadRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), head.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        String token = jwtUtil.generateToken(head.getHeadId(), "STATION_HEAD");

        notificationService.send(head.getHeadId(), "STATION_HEAD", null,
                "LOGIN_ALERT",
                "You logged in to CMS on " + java.time.LocalDateTime.now(),
                head.getEmail());

        log.info("StationHead login — headId={}", head.getHeadId());
        return AuthResponse.builder()
                .status("success").token(token)
                .role("STATION_HEAD").name(head.getFullName())
                .actorId(head.getHeadId()).build();
    }
}
