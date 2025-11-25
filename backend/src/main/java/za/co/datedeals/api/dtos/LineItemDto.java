package za.co.datedeals.api.dtos;

import lombok.Data;

@Data
public class LineItemDto {
    private Long id;
    private String sku;
    private Integer quantity;
    private String price;
}
