package za.co.datedeals.api.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import java.util.List;

@Data
public class ShopifyCreateOrderDto {
    
    private Long id;
    
    @JsonProperty("admin_graphql_api_id")
    private String adminGraphqlApiId;
    
    @JsonProperty("app_id")
    private Long appId;
    
    @JsonProperty("browser_ip")
    private String browserIp;
    
    @JsonProperty("buyer_accepts_marketing")
    private Boolean buyerAcceptsMarketing;
    
    @JsonProperty("cancel_reason")
    private String cancelReason;
    
    @JsonProperty("cancelled_at")
    private OffsetDateTime cancelledAt;
    
    @JsonProperty("cart_token")
    private String cartToken;
    
    @JsonProperty("checkout_id")
    private Long checkoutId;
    
    @JsonProperty("checkout_token")
    private String checkoutToken;
    
    @JsonProperty("client_details")
    private ClientDetails clientDetails;
    
    @JsonProperty("closed_at")
    private OffsetDateTime closedAt;
    
    @JsonProperty("confirmation_number")
    private String confirmationNumber;
    
    private Boolean confirmed;
    
    @JsonProperty("contact_email")
    private String contactEmail;
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    private String currency;
    
    @JsonProperty("current_subtotal_price")
    private BigDecimal currentSubtotalPrice;
    
    @JsonProperty("current_total_discounts")
    private BigDecimal currentTotalDiscounts;
    
    @JsonProperty("current_total_price")
    private BigDecimal currentTotalPrice;
    
    @JsonProperty("current_total_tax")
    private BigDecimal currentTotalTax;
    
    @JsonProperty("customer_locale")
    private String customerLocale;
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("discount_codes")
    private List<String> discountCodes;
    
    private String email;
    
    @JsonProperty("financial_status")
    private String financialStatus;
    
    @JsonProperty("fulfillment_status")
    private String fulfillmentStatus;
    
    private String name;
    
    private String note;
    
    @JsonProperty("note_attributes")
    private List<NoteAttribute> noteAttributes;
    
    private Integer number;
    
    @JsonProperty("order_number")
    private Integer orderNumber;
    
    @JsonProperty("order_status_url")
    private String orderStatusUrl;
    
    @JsonProperty("payment_gateway_names")
    private List<String> paymentGatewayNames;
    
    private String phone;
    
    @JsonProperty("presentment_currency")
    private String presentmentCurrency;
    
    @JsonProperty("processed_at")
    private OffsetDateTime processedAt;
    
    @JsonProperty("source_name")
    private String sourceName;
    
    @JsonProperty("subtotal_price")
    private BigDecimal subtotalPrice;
    
    private String tags;
    
    @JsonProperty("tax_exempt")
    private Boolean taxExempt;
    
    @JsonProperty("tax_lines")
    private List<TaxLine> taxLines;
    
    @JsonProperty("taxes_included")
    private Boolean taxesIncluded;
    
    private Boolean test;
    
    private String token;
    
    @JsonProperty("total_discounts")
    private BigDecimal totalDiscounts;
    
    @JsonProperty("total_line_items_price")
    private BigDecimal totalLineItemsPrice;
    
    @JsonProperty("total_outstanding")
    private BigDecimal totalOutstanding;
    
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    
    @JsonProperty("total_tax")
    private BigDecimal totalTax;
    
    @JsonProperty("total_tip_received")
    private BigDecimal totalTipReceived;
    
    @JsonProperty("total_weight")
    private Integer totalWeight;
    
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    
    @JsonProperty("billing_address")
    private Address billingAddress;
    
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    
    private Customer customer;
    
    @JsonProperty("line_items")
    private List<LineItem> lineItems;
    
    @JsonProperty("shipping_lines")
    private List<ShippingLine> shippingLines;
    
    // Nested DTOs
    
    @Data
    public static class ClientDetails {
        @JsonProperty("accept_language")
        private String acceptLanguage;
        
        @JsonProperty("browser_ip")
        private String browserIp;
        
        @JsonProperty("user_agent")
        private String userAgent;
    }
    
    @Data
    public static class NoteAttribute {
        private String name;
        private String value;
    }
    
    @Data
    public static class TaxLine {
        private BigDecimal price;
        private BigDecimal rate;
        private String title;
        
        @JsonProperty("channel_liable")
        private Boolean channelLiable;
    }
    
    @Data
    public static class Address {
        private Long id;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        private String company;
        private String address1;
        private String address2;
        private String city;
        private String province;
        private String country;
        private String zip;
        private String phone;
        private String name;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("country_code")
        private String countryCode;
        
        private Double latitude;
        private Double longitude;
    }
    
    @Data
    public static class Customer {
        private Long id;
        
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
        
        @JsonProperty("updated_at")
        private OffsetDateTime updatedAt;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        private String state;
        private String email;
        private String phone;
        private String currency;
        
        @JsonProperty("verified_email")
        private Boolean verifiedEmail;
        
        @JsonProperty("tax_exempt")
        private Boolean taxExempt;
        
        @JsonProperty("default_address")
        private Address defaultAddress;
    }
    
    @Data
    public static class LineItem {
        private Long id;
        
        @JsonProperty("admin_graphql_api_id")
        private String adminGraphqlApiId;
        
        @JsonProperty("current_quantity")
        private Integer currentQuantity;
        
        @JsonProperty("fulfillable_quantity")
        private Integer fulfillableQuantity;
        
        @JsonProperty("fulfillment_service")
        private String fulfillmentService;
        
        @JsonProperty("fulfillment_status")
        private String fulfillmentStatus;
        
        @JsonProperty("gift_card")
        private Boolean giftCard;
        
        private Integer grams;
        private String name;
        private BigDecimal price;
        
        @JsonProperty("product_exists")
        private Boolean productExists;
        
        @JsonProperty("product_id")
        private Long productId;
        
        private Integer quantity;
        
        @JsonProperty("requires_shipping")
        private Boolean requiresShipping;
        
        private String sku;
        private Boolean taxable;
        private String title;
        
        @JsonProperty("total_discount")
        private BigDecimal totalDiscount;
        
        @JsonProperty("variant_id")
        private Long variantId;
        
        @JsonProperty("variant_title")
        private String variantTitle;
        
        private String vendor;
        
        @JsonProperty("tax_lines")
        private List<TaxLine> taxLines;
    }
    
    @Data
    public static class ShippingLine {
        private Long id;
        private String code;
        private BigDecimal price;
        private String title;
        private String source;
        
        @JsonProperty("carrier_identifier")
        private String carrierIdentifier;
        
        @JsonProperty("tax_lines")
        private List<TaxLine> taxLines;
    }
}
