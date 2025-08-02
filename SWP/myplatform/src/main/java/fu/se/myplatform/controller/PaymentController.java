package fu.se.myplatform.controller;


import fu.se.myplatform.dto.MoMoResponseDTO;
import fu.se.myplatform.dto.PurchasePaymentRequestDTO;
import fu.se.myplatform.dto.VNPayResponseDTO;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.enums.TransactionMethod;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.MemberRepository;
import fu.se.myplatform.service.MemberService;
import fu.se.myplatform.service.PaymentFactory;
import fu.se.myplatform.service.PaymentService;
import fu.se.myplatform.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    // --- CÁC GIÁ TRỊ MẶC ĐỊNH CHO GÓI VIP ---
    private static final long VIP_PRICE = 69000L;
    private static final int VIP_DURATION_DAYS = 30;

    // --- INJECT CÁC SERVICE VÀ REPOSITORY CẦN THIẾT ---
    @Autowired
    private PaymentFactory paymentFactory;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/payment")
    public ResponseEntity<String> createPayment(HttpServletRequest request, @RequestBody PurchasePaymentRequestDTO requestDTO) {
        PaymentService paymentService = paymentFactory.getPaymentService(requestDTO.getPaymentMethod());
        String paymentUrl = paymentService.createPaymentUrl(request, requestDTO);
        return ResponseEntity.ok("redirect:" + paymentUrl);
    }

    @GetMapping("/return")
    public ResponseEntity<?> paymentReturn(@RequestParam Map<String, String> params) {
        TransactionMethod method = params.containsKey("vnp_TxnRef") ? TransactionMethod.VNPAY : TransactionMethod.MOMO;
        PaymentService paymentService = paymentFactory.getPaymentService(method);
        Object response = paymentService.processPaymentReturn(params);
        if (response instanceof VNPayResponseDTO) {
            VNPayResponseDTO vnPayResponse = (VNPayResponseDTO) response;
            // --- BẮT ĐẦU LOGIC "ĂN GIAN" TẠM THỜI ---
            // Chỉ xử lý khi giao dịch thành công và checksum hợp lệ
            if ("SUCCESS".equals(vnPayResponse.getStatus())) {
                try {
                    // Copy y hệt logic từ hàm ipn() vào đây
                    // 1. Phân tích `orderInfo` để lấy `userId`
                    Long userId = Long.parseLong(vnPayResponse.getOrderInfo());

                    // 2. Tìm các đối tượng trong database
                    Account account = accountRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Account với ID: " + userId));
                    Member member = memberRepository.findByUser(account);
                    // 3. Tạo và lưu lại lịch sử giao dịch
                    // (Thêm kiểm tra để tránh lưu trùng lặp nếu IPN vẫn chạy)
                    transactionService.createAndSaveTransaction(account, VIP_PRICE, vnPayResponse.getTxnRef(), method);

                    // 4. Gọi phương thức nâng cấp VIP
                    memberService.upgradeMemberToVip(member.getMemberId());

                } catch (Exception e) {
                    System.err.println("Lỗi khi xử lý 'return' URL: " + e.getMessage());
                    // Không cần trả về lỗi cho VNPay vì đây là luồng của user
                }
            }
            // --- KẾT THÚC LOGIC "ĂN GIAN" ---

            return ResponseEntity.status(vnPayResponse.getStatus().equals("SUCCESS") ? 200 : 400).body(vnPayResponse);
        } else {
            MoMoResponseDTO moMoResponse = (MoMoResponseDTO) response;
            return ResponseEntity.status(moMoResponse.getStatus().equals("SUCCESS") ? 200 : 400).body(moMoResponse);
        }
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> ipn(@RequestParam Map<String, String> params) {
        TransactionMethod method = params.containsKey("vnp_TxnRef") ? TransactionMethod.VNPAY : TransactionMethod.MOMO;
        PaymentService paymentService = paymentFactory.getPaymentService(method);
        Object response = paymentService.processIPN(params);

        // --- BẮT ĐẦU LOGIC NGHIỆP VỤ KHI CÓ IPN ---
        if (response instanceof VNPayResponseDTO) {
            VNPayResponseDTO vnPayResponse = (VNPayResponseDTO) response;
            // Chỉ xử lý khi giao dịch thành công và checksum hợp lệ
            if ("SUCCESS".equals(vnPayResponse.getStatus())) {
                try {
                    // 1. Phân tích `orderInfo` để lấy `userId`
                    Long userId = Long.parseLong(vnPayResponse.getOrderInfo());

                    // 2. Tìm các đối tượng trong database
                    Account account = accountRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Account với ID: " + userId));
                    // ... (lấy thông tin user và member)
                    Member member = memberRepository.findByUser(account);
                    // 3. Tạo và lưu lại lịch sử giao dịch (giữ nguyên)
                    transactionService.createAndSaveTransaction(account, VIP_PRICE, vnPayResponse.getTxnRef(), method);
                    // 4. Gọi phương thức nâng cấp VIP mới - Rất gọn gàng!
                    memberService.upgradeMemberToVip(member.getMemberId());


                } catch (Exception e) {
                    // Nếu có lỗi trong quá trình xử lý, trả về mã lỗi để VNPay gửi lại IPN
                    // Ghi log lỗi để debug
                    System.err.println("Lỗi khi xử lý IPN VNPay: " + e.getMessage());
                    return ResponseEntity.ok("RspCode=99&Message=Update failed");
                }
            }
        }
        return ResponseEntity.ok("RspCode=00&Message=Confirm Success");

    }
}
