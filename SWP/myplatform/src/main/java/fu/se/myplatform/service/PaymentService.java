package fu.se.myplatform.service;

import fu.se.myplatform.dto.PurchasePaymentRequestDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    String createPaymentUrl(HttpServletRequest request, PurchasePaymentRequestDTO requestDTO);
    Object processPaymentReturn(Map<String, String> params);
    Object processIPN(Map<String, String> params);
}
