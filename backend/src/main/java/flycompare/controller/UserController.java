package flycompare.controller;

import flycompare.entity.User;
import flycompare.repository.UserRepository;
import flycompare.service.EmailOtpService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final EmailOtpService emailOtpService;
    private final JavaMailSender mailSender;

    public UserController(
            UserRepository userRepository,
            EmailOtpService emailOtpService,
            JavaMailSender mailSender) {

        this.userRepository = userRepository;
        this.emailOtpService = emailOtpService;
        this.mailSender = mailSender;
    }

    // =========================================
    // REGISTER USER
    // =========================================

    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {

        try {

            // Check whether email already exists
            User existingEmail =
                    userRepository.findByEmail(
                            user.getEmail().trim().toLowerCase()
                    );

            if (existingEmail != null) {
                return "EMAIL_EXISTS";
            }

            // Convert email to lowercase
            user.setEmail(
                    user.getEmail().trim().toLowerCase()
            );

            // Save user in MySQL
            User savedUser =
                    userRepository.save(user);

            // Send registration success email
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(
                    savedUser.getEmail()
            );

            message.setSubject(
                    "FlyCompare - Registration Successfully Completed"
            );

            message.setText(
                    "Hello "
                    + savedUser.getFirstName()
                    + ",\n\n"
                    + "Registration Successfully Completed!\n\n"
                    + "Your FlyCompare account has been created successfully.\n\n"
                    + "You can now login to your FlyCompare account using your registered email.\n\n"
                    + "Thank you,\n"
                    + "FlyCompare Team"
            );

            mailSender.send(message);

            System.out.println(
                    "REGISTRATION SUCCESS EMAIL SENT TO: "
                    + savedUser.getEmail()
            );

            return "REGISTRATION_SUCCESS";

        } catch (Exception e) {

            e.printStackTrace();

            return "REGISTRATION_FAILED";
        }
    }

    // =========================================
    // LOGIN AND SEND OTP
    // =========================================

    @PostMapping("/login")
    public String loginUser(
            @RequestBody User user) {

        User existingUser =
                userRepository.findByEmail(
                        user.getEmail()
                );

        if (existingUser != null &&
            existingUser.getPassword()
                    .equals(user.getPassword())) {

            emailOtpService.sendOtp(
                    existingUser.getEmail()
            );

            return "OTP_SENT";
        }

        return "INVALID_LOGIN";
    }

    // =========================================
    // VERIFY LOGIN OTP
    // =========================================

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        System.out.println(
                "VERIFY EMAIL: [" + email + "]"
        );

        System.out.println(
                "VERIFY OTP: [" + otp + "]"
        );

        boolean verified =
                emailOtpService.verifyOtp(
                        email,
                        otp
                );

        if (verified) {

            System.out.println(
                    "OTP VERIFICATION: SUCCESS"
            );

            return "LOGIN_SUCCESS";
        }

        System.out.println(
                "OTP VERIFICATION: FAILED"
        );

        return "INVALID_OTP";
    }

    // =========================================
    // RESEND LOGIN OTP
    // =========================================

    @PostMapping("/resend-login-otp")
    public String resendLoginOtp(
            @RequestParam String email) {

        User existingUser =
                userRepository.findByEmail(
                        email.trim().toLowerCase()
                );

        if (existingUser != null) {

            emailOtpService.sendOtp(
                    existingUser.getEmail()
            );

            return "OTP_SENT";
        }

        return "USER_NOT_FOUND";
    }

    // =========================================
    // GET USER PROFILE
    // =========================================

    @GetMapping("/profile")
    public User getUserProfile(
            @RequestParam String email) {

        User existingUser =
                userRepository.findByEmail(
                        email.trim().toLowerCase()
                );

        return existingUser;
    }
}