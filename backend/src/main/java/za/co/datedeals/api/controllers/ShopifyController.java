package za.co.datedeals.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.ShopifyOrderWebhookDto;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.services.ShopifyService;
import za.co.datedeals.api.utils.ShopifyWebhookVerifier;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shopify")
@Tag(name = "Shopify", description = "Shopify webhook integration APIs")
public class ShopifyController {
    private static final Logger logger = LoggerFactory.getLogger(ShopifyController.class);
    private static final String HMAC_HEADER = "X-Shopify-Hmac-Sha256";

    @Autowired
    private ShopifyService shopifyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${shopify.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/orders")
    @Operation(summary = "Create order webhook", description = "Receives Shopify order webhook and creates coupons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid HMAC signature"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<?> createOrder(
            @RequestBody String rawBody,
            @RequestHeader(value = HMAC_HEADER, required = false) String hmacHeader) {
        
        logger.info("Received Shopify order webhook");

        // Verify HMAC signature
        if (!ShopifyWebhookVerifier.verifyWebhook(rawBody, hmacHeader, webhookSecret)) {
            logger.error("HMAC verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook signature"));
        }

        logger.info("HMAC verification successful");

        try {
            // Parse the JSON after HMAC verification
            ShopifyOrderWebhookDto orderDto = objectMapper.readValue(rawBody, ShopifyOrderWebhookDto.class);
            logger.info("Processing order ID: {}", orderDto.getId());
            
            List<Coupon> coupons = shopifyService.createCouponsFromOrder(orderDto);
            
            logger.info("Successfully created {} coupons for order ID: {}", coupons.size(), orderDto.getId());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Order processed successfully",
                    "couponsCreated", coupons.size(),
                    "orderId", orderDto.getId()
            ));
        } catch (Exception e) {
            logger.error("Error processing order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process order: " + e.getMessage()));
        }
    }
}