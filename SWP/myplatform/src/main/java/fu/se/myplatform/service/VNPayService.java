package fu.se.myplatform.service;


import fu.se.myplatform.config.VNPayConfig;
import fu.se.myplatform.dto.PurchasePaymentRequestDTO;
import fu.se.myplatform.dto.VNPayResponseDTO;
import fu.se.myplatform.utils.VNPayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService implements PaymentService {

    @Autowired
    private VNPayConfig vnPayConfig;
    private static final long VIP_PRICE = 69000L;


    @Override
    public String createPaymentUrl(HttpServletRequest request, PurchasePaymentRequestDTO requestDTO) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "topup";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
        String vnp_ExpireDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        vnp_Params.put("vnp_Amount", String.valueOf(VIP_PRICE * 100));
        // 2. OrderInfo giờ chỉ cần chứa userId để xử lý ở IPN
        vnp_Params.put("vnp_OrderInfo", requestDTO.getUserId().toString());

        StringBuilder hashData = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()))
                        .append("&");
            }
            hashData.deleteCharAt(hashData.length() - 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        StringBuilder paymentUrl = new StringBuilder(vnPayConfig.getPaymentUrl()).append("?");
        try {
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                paymentUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()))
                        .append("&");
            }
            paymentUrl.deleteCharAt(paymentUrl.length() - 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        return paymentUrl.toString();
    }

    @Override
    public VNPayResponseDTO processPaymentReturn(Map<String, String> vnp_Params) {
        VNPayResponseDTO response = new VNPayResponseDTO();
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHash");

        StringBuilder hashData = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : new TreeMap<>(vnp_Params).entrySet()) {
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()))
                        .append("&");
            }
            hashData.deleteCharAt(hashData.length() - 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        String calculatedHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        boolean isValid = calculatedHash.equals(vnp_SecureHash);

        response.setStatus(isValid ? ("00".equals(vnp_Params.get("vnp_ResponseCode")) ? "SUCCESS" : "FAILED") : "INVALID_CHECKSUM");
        response.setTxnRef(vnp_Params.get("vnp_TxnRef"));
        response.setAmount(Long.parseLong(vnp_Params.get("vnp_Amount")) / 100);
        response.setOrderInfo(vnp_Params.get("vnp_OrderInfo"));
        response.setMessage(isValid ? ("00".equals(vnp_Params.get("vnp_ResponseCode")) ? "Payment successful" : "Payment failed: ResponseCode " + vnp_Params.get("vnp_ResponseCode")) : "Invalid checksum");

        return response;
    }

    @Override
    public VNPayResponseDTO processIPN(Map<String, String> vnp_Params) {
        VNPayResponseDTO response = new VNPayResponseDTO();
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHash");

        StringBuilder hashData = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : new TreeMap<>(vnp_Params).entrySet()) {
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()))
                        .append("&");
            }
            hashData.deleteCharAt(hashData.length() - 1);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        String calculatedHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        boolean isValid = calculatedHash.equals(vnp_SecureHash);

        response.setStatus(isValid ? ("00".equals(vnp_Params.get("vnp_ResponseCode")) ? "SUCCESS" : "FAILED") : "INVALID_CHECKSUM");
        response.setMessage(isValid ? "Confirm Success" : "Invalid Checksum");
        if (isValid && "00".equals(vnp_Params.get("vnp_ResponseCode"))) {
            response.setTxnRef(vnp_Params.get("vnp_TxnRef"));
            response.setAmount(Long.parseLong(vnp_Params.get("vnp_Amount")) / 100);
            response.setOrderInfo(vnp_Params.get("vnp_OrderInfo"));
        }

        return response;
    }
}
