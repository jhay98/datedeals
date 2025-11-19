package za.co.datedeals.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealRequestDto {
    private String code;
    private String title;
    private String htmlVoucherTemplate;
    private LocalDateTime expiryDate;
    private Integer lifetimeDays;
    private Double commissionPercentage;
    private Long businessId;
}
