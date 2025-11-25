package za.co.datedeals.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ShopifyOrderWebhookDto {
    private Long id;
    private String email;
    
    @JsonProperty("line_items")
    private List<LineItemDto> lineItems;
}
