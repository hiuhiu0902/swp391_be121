package fu.se.myplatform.service;


import fu.se.myplatform.config.MoMoConfig;
import fu.se.myplatform.dto.MoMoResponseDTO;
import fu.se.myplatform.dto.PurchasePaymentRequestDTO;
import fu.se.myplatform.utils.MoMoUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class MoMoService implements PaymentService {

    @Autowired
    private MoMoConfig moMoConfig;

    @Autowired
    private RestTemplate restTemplate;
    private static final long VIP_PRICE = 69000L;


    @Override
    public String createPaymentUrl(HttpServletRequest request, PurchasePaymentRequestDTO requestDTO) {
        String requestId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        String ipAddress = request.getRemoteAddr();

        // --- THAY ĐỔI Ở ĐÂY ---
        // 1. Sử dụng giá tiền mặc định
        long amount = VIP_PRICE;
        // 2. OrderInfo có thể là một mô tả chung
        String orderInfo = "Thanh toan nang cap VIP Member";
        // 3. Đưa userId vào extraData, MoMo sẽ trả về trường này trong IPN
        String extraData = "userId=" + requestDTO.getUserId();


        // Tạo chuỗi rawData để ký
        String rawData = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=captureWallet",
                moMoConfig.getAccessKey(), amount, extraData, moMoConfig.getIpnUrl(), orderId, orderInfo,
                moMoConfig.getPartnerCode(), moMoConfig.getReturnUrl(), requestId);
        String signature = MoMoUtils.hmacSHA256(moMoConfig.getSecretKey(), rawData);

        // Tạo request body gửi đến MoMo
        Map<String, Object> requestBody = new TreeMap<>();
        requestBody.put("partnerCode", moMoConfig.getPartnerCode());
        requestBody.put("accessKey", moMoConfig.getAccessKey());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", String.valueOf(amount));
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", moMoConfig.getReturnUrl());
        requestBody.put("ipnUrl", moMoConfig.getIpnUrl());
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", "captureWallet");
        requestBody.put("signature", signature);

        // Gửi POST đến MoMo
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(moMoConfig.getPaymentUrl(), httpEntity, Map.class);

        // In toàn bộ response để debug
        System.out.println("MoMo API response: " + response);

        // Kiểm tra kết quả
        Object resultCodeObj = response != null ? response.get("resultCode") : null;
        String resultCode = resultCodeObj != null ? String.valueOf(resultCodeObj) : null;

        if (!"0".equals(resultCode)) {
            throw new RuntimeException("Failed to create MoMo payment URL. resultCode=" + resultCode + ", message=" + response.get("message") + ", fullResponse=" + response);
        }

        return (String) response.get("payUrl");
    }

    @Override
    public MoMoResponseDTO processPaymentReturn(Map<String, String> params) {
        MoMoResponseDTO response = new MoMoResponseDTO();
        String signature = params.get("signature");
        String requestId = params.get("requestId");
        String orderId = params.get("orderId");
        long amount = Long.parseLong(params.get("amount"));
        String orderInfo = params.get("orderInfo");
        String resultCode = params.get("resultCode");

        String rawData = String.format("accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                moMoConfig.getAccessKey(), params.get("amount"), params.get("extraData"), params.get("message"), orderId, orderInfo,
                params.get("orderType"), moMoConfig.getPartnerCode(), params.get("payType"), requestId, params.get("responseTime"), resultCode, params.get("transId"));
        String calculatedSignature = MoMoUtils.hmacSHA256(moMoConfig.getSecretKey(), rawData);

        boolean isValid = calculatedSignature.equals(signature);

        response.setStatus(isValid ? ("0".equals(resultCode) ? "SUCCESS" : "FAILED") : "INVALID_SIGNATURE");
        response.setRequestId(requestId);
        response.setAmount(amount);
        response.setOrderInfo(orderInfo);
        response.setMessage(isValid ? ("0".equals(resultCode) ? "Payment successful" : "Payment failed: " + params.get("message")) : "Invalid signature");

        return response;
    }

    @Override
    public MoMoResponseDTO processIPN(Map<String, String> params) {
        MoMoResponseDTO response = new MoMoResponseDTO();
        String signature = params.get("signature");
        String requestId = params.get("requestId");
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");

        String rawData = String.format("accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                moMoConfig.getAccessKey(), params.get("amount"), params.get("extraData"), params.get("message"), orderId, params.get("orderInfo"),
                params.get("orderType"), moMoConfig.getPartnerCode(), params.get("payType"), requestId, params.get("responseTime"), resultCode, params.get("transId"));
        String calculatedSignature = MoMoUtils.hmacSHA256(moMoConfig.getSecretKey(), rawData);

        boolean isValid = calculatedSignature.equals(signature);

        response.setStatus(isValid ? ("0".equals(resultCode) ? "SUCCESS" : "FAILED") : "INVALID_SIGNATURE");
        response.setMessage(isValid ? "Confirm Success" : "Invalid Signature");
        if (isValid && "0".equals(resultCode)) {
            response.setRequestId(requestId);
            response.setAmount(Long.parseLong(params.get("amount")));
            response.setOrderInfo(params.get("orderInfo"));
        }

        return response;
    }
}
