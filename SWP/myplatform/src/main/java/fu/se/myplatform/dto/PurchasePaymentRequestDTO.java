package fu.se.myplatform.dto;

import fu.se.myplatform.enums.TransactionMethod;
import lombok.Data;
//import org.example.smartlawgt.command.entities.TransactionMethod;

@Data
public class PurchasePaymentRequestDTO {
    private Long userId;
    private TransactionMethod paymentMethod;
}