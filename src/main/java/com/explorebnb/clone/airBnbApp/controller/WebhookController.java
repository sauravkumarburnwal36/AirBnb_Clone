package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final BookingService bookingService;

    @Value("{stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/payments")
    @Operation(summary = "Capture the payments", tags = {"Webhook"})
    public ResponseEntity<Void> capturePayments(@RequestBody String payLoad, @RequestHeader("Stripe-Signature")String signHeader){
        try{
            Event event= Webhook.constructEvent(payLoad,signHeader,endpointSecret);
            bookingService.capturePayments(event);
            return ResponseEntity.noContent().build();
        }
        catch(SignatureVerificationException e){
            throw new RuntimeException(e);
        }
    }

}
