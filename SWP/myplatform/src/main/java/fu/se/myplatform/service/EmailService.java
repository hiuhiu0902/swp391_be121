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

}
