package flycompare.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailOtpService {

    private final JavaMailSender mailSender;

    private final Map<String, String> otpStorage = new HashMap<>();

    public EmailOtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {

        email = email.trim().toLowerCase();

        String otp = String.format(
                "%06d",
                new Random().nextInt(1000000)
        );

        otpStorage.put(email, otp);

        System.out.println(
                "OTP GENERATED: [" + otp + "]"
        );

        System.out.println(
                "OTP STORED FOR EMAIL: [" + email + "]"
        );

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "FlyCompare Login OTP"
        );

        message.setText(
                "Your FlyCompare OTP is: "
                + otp
                + "\n\nThis OTP is used to complete your login."
        );

        mailSender.send(message);
    }

    public boolean verifyOtp(
            String email,
            String otp
    ) {

        email = email.trim().toLowerCase();
        otp = otp.trim();

        String savedOtp =
                otpStorage.get(email);

        System.out.println(
                "VERIFY EMAIL: [" + email + "]"
        );

        System.out.println(
                "OTP FROM WEBSITE: [" + otp + "]"
        );

        System.out.println(
                "OTP STORED IN BACKEND: [" + savedOtp + "]"
        );

        if (savedOtp != null &&
            savedOtp.equals(otp)) {

            System.out.println(
                    "OTP MATCH: YES"
            );

            otpStorage.remove(email);

            return true;
        }

        System.out.println(
                "OTP MATCH: NO"
        );

        return false;
    }
}