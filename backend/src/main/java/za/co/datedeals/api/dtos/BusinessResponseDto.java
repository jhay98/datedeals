package za.co.datedeals.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponseDto {
    private Long businessId;
    private String businessName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String description;
}
