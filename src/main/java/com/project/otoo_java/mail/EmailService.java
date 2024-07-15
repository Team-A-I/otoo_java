package com.project.otoo_java.mail;

import com.project.otoo_java.users.model.repository.UsersRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    private final UsersRepository usersRepository;

    @Value("${SPRING_MAIL_USERNAME}")
    private String fromEmail;

    public String sendEmail(String sendEmail) {
        try {
            boolean existEmail = usersRepository.existsUsersByUsersEmail(sendEmail);
            if (existEmail) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 이메일 주소입니다");
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            String code = createCode();
            helper.setTo(sendEmail);
            helper.setSubject("[otoo] 이메일 인증");
            helper.setText("이메일 인증 코드 : " + code);
            helper.setFrom(fromEmail + "@naver.com");

            javaMailSender.send(message);

            return code;
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다.");
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "암호화 알고리즘을 찾을 수 없습니다.");
        }
    }

    public String forgotPwd(String sendEmail) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            String code = createCode();
            helper.setTo(sendEmail);
            helper.setSubject("[otoo] 이메일 인증");
            helper.setText("이메일 인증 코드 : " + code);
            helper.setFrom(fromEmail + "@naver.com");

            javaMailSender.send(message);

            return code;
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다.");
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "암호화 알고리즘을 찾을 수 없습니다.");
        }
    }

    private String createCode() throws NoSuchAlgorithmException {
        int length = 6;
        Random random = SecureRandom.getInstanceStrong();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}