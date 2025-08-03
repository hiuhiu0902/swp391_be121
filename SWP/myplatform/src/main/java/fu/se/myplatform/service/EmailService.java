package fu.se.myplatform.service;

import fu.se.myplatform.dto.EmailDetail;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendMail(EmailDetail emailDetail){

        try{

            Context context = new Context();

            context.setVariable("name", emailDetail.getRecipient());
            context.setVariable("button", "Reset Password");
            context.setVariable("link", emailDetail.getLink());

            String html = templateEngine.process("emailtemplate", context);

            // Creating a simple mail message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            // Setting up necessary details
            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getRecipient());
            mimeMessageHelper.setText(html, true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void sendCoachDeletedNotification(String recipientEmail, String memberName, String coachName){
        try{
            Context context = new Context();
            context.setVariable("name", memberName);
            context.setVariable("coachName", coachName);
            context.setVariable("message", "Your coach has been deleted. Please contact support for further assistance.");

            String html = templateEngine.process("coach-deleted-notification", context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(recipientEmail);
            mimeMessageHelper.setText(html, true);
            mimeMessageHelper.setSubject("Coach Deleted Notification");

            javaMailSender.send(mimeMessage);
        } catch (Exception e){
            System.out.println("Error sending coach deleted notification: " + e.getMessage());
        }
    }
    public void sendAccountCreationNotification(String recipientEmail, String userName, String password) {
        try {
            // Chuẩn bị context cho template
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("password", password);

            // Tạo nội dung HTML từ template
            String html = templateEngine.process("account-creation-notification", context);

            // Tạo message email
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true); // Bật chế độ HTML

            // Thiết lập các thông tin cần thiết
            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(recipientEmail);
            mimeMessageHelper.setSubject("Thông Báo: Tài Khoản Của Bạn Đã Được Tạo");
            mimeMessageHelper.setText(html, true); // true để chỉ định nội dung là HTML

            // Gửi email
            javaMailSender.send(mimeMessage);
            System.out.println("Mail Sent Successfully...");

        } catch (Exception e) {
            System.out.println("Error sending account creation notification: " + e.getMessage());
        }
    }
    // Thêm hàm này vào trong class EmailService của bạn

    /**
     * Gửi email chào mừng cho người dùng mới đăng ký thành công.
     * Sử dụng một template riêng biệt là "welcome-email".
     * @param emailDetail Đối tượng chứa thông tin người nhận, chủ đề, và link.
     */
    public void sendWelcomeEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();

            // Lấy tên người nhận từ email để cá nhân hóa (ví dụ: "example@gmail.com" -> "example")
            // Ghi chú: Để cá nhân hóa tốt hơn, bạn nên thêm trường "name" vào EmailDetail
            String recipientName = emailDetail.getRecipient().split("@")[0];
            context.setVariable("recipientName", recipientName);
            context.setVariable("link", emailDetail.getLink());

            // Xử lý template
            String html = templateEngine.process("welcome-email", context);

            // Tạo và cấu hình message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true); // true = multipart

            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getRecipient());
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            mimeMessageHelper.setText(html, true); // true = nội dung là HTML

            // Gửi email
            javaMailSender.send(mimeMessage);
            System.out.println("Welcome Email Sent Successfully...");

        } catch (Exception e) {
            System.out.println("Error sending welcome email: " + e.getMessage());
        }
    }

}
