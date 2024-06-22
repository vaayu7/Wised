package com.wised.auth.controller;

import com.wised.auth.dtos.ApiResponse;
import com.wised.auth.dtos.SendTemplateEmailMessageResponse;
import com.wised.auth.dtos.ValidateOTPRequest;
import com.wised.auth.model.User;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.service.EmailService;
import com.wised.auth.service.OTPService;
import com.wised.auth.helper.EmailTemplate;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The `OTPController` class handles operations related to OTP (One-Time Password) generation and validation.
 *
 * This controller provides endpoints for generating OTP, sending it via email, and validating OTP entered by the user.
 */
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OTPController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository repository;

    /**
     * Generate and send OTP to the specified email address.
     *
     * @param email The email address to which OTP will be sent.
     * @return A response entity with the result of OTP generation and sending.
     */
    @GetMapping("/generateOtp/{email}")
    public ResponseEntity<?> generateOTP(@PathVariable String email) {
        if (email == null) {
            // If email is null, return a bad request response
            return ResponseEntity.badRequest().body(new SendTemplateEmailMessageResponse(false, null, "Invalid email"));
        }

        Optional<User> userOptional = repository.findByEmail(email);

        if (userOptional.isPresent()) {
            // If the email is already registered, return a conflict response
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SendTemplateEmailMessageResponse(false, null, "Email is already registered"));
        }

        int otp = otpService.generateOTP(email);

        EmailTemplate template = new EmailTemplate("templates/emails/SendOtp.html");
        Map<String, String> replacements = new HashMap<>();
        replacements.put("user", email);
        replacements.put("otpnum", String.valueOf(otp));
        String message = template.getTemplate(replacements);

        // Call the email service to send the OTP via email and handle its response
        SendTemplateEmailMessageResponse emailResponse = emailService.sendTemplateEmailMessage(email, "OTP - Email Verification", message);

        if (emailResponse.isSuccess()) {
            // If email is sent successfully, return a success response
            return ResponseEntity.ok(emailResponse);
        } else {
            // If there was an error sending the email, return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
        }
    }

    /**
     * Validate the OTP entered by the user.
     *
     * @param request The OTP validation request containing email and OTP entered by the user.
     * @return A response entity indicating whether the OTP is valid or not.
     * @throws MessagingException If an error occurs while sending the OTP via email.
     */
    @GetMapping("/validateOtp")
    public ResponseEntity<ApiResponse> validateOtp(@RequestBody ValidateOTPRequest request) throws MessagingException{
        String email = request.getEmail();
        int clientOtp = request.getOtp();

        // Get the OTP from your OTPService for the provided email
        int serverOtp = otpService.getOtp(email);

        // Check if the server OTP matches the client OTP
        if (serverOtp == clientOtp) {
            // OTP is valid, you can perform further actions here
            // Removing email and OTP from cache
            otpService.clearOTP(email);
            return ResponseEntity.ok(new ApiResponse("OTP is valid", null));
        } else {
            // OTP is invalid, return an error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(null, "Invalid OTP"));
        }
    }
}

