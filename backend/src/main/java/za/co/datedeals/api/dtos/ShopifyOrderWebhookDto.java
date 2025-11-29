package za.co.datedeals.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ShopifyOrderWebhookDto {
    private Long id;
    private String email;
    
    @JsonProperty("contact_email")
    private String contactEmail;
    
    @JsonProperty("line_items")
    private List<LineItemDto> lineItems;
    
    private CustomerDto customer;
    
    @Data
    public static class CustomerDto {
        private Long id;
        private String email;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
    }
}
