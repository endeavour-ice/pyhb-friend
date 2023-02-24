package com.user.py.utils;

import com.user.py.designPatten.singleton.DataUtils;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @Author ice
 * @Date 2022/11/3 22:06
 * @PackageName:com.user.py.utils
 * @ClassName: EmailUtil
 * @Description: TODO
 * @Version 1.0
 */
@Slf4j
public class EmailUtil {

    /**
     * 发送邮件
     *
     * @param sendEmail 要发送人的邮件
     * @param theme     主题
     * @param template  要发送的内容
     * @return
     */
    public static boolean sendEmail(String sendEmail, String theme, String template) {
        String from_email = ConstantPropertiesUtils.EMAIL;
        String pwd = ConstantPropertiesUtils.EMAILPASSWORD;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");     //使用smpt的邮件传输协议
        props.setProperty("mail.smtp.host", "smtp.qq.com");       //主机地址
        props.setProperty("mail.smtp.auth", "true");      //授权通过
        Session session = Session.getInstance(props);     //通过我们的这些配置，得到一个会话程序
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from_email));     //设置发件人
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(sendEmail, "用户", "utf-8"));      //设置收件人
            message.setSubject(theme, "utf-8");      //设置主题
            message.setSentDate(new Date());
            Multipart mul = new MimeMultipart();  //新建一个MimeMultipart对象来存放多个BodyPart对象
            BodyPart mdp = new MimeBodyPart();  //新建一个存放信件内容的BodyPart对象
            mdp.setContent(template, "text/html;charset=utf-8");
            mul.addBodyPart(mdp);  //将含有信件内容的BodyPart加入到MimeMultipart对象中
            message.setContent(mul); //把mul作为消息内容
            message.saveChanges();
            //创建一个传输对象
            Transport transport = session.getTransport("smtp");
            //建立与服务器的链接  465端口是 SSL传输
            transport.connect("smtp.qq.com", 587, from_email, pwd);
            //发送邮件
            transport.sendMessage(message, message.getAllRecipients());
            //关闭邮件传输
            transport.close();

        } catch (UnsupportedEncodingException | MessagingException e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean sendAlarmEmail(String str) {
        SimpleDateFormat sdf = DataUtils.getFdt();
        String qq = "3521315291@qq.com";
        String theme = "警报！！";
        String message = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><p style='font-size: 20px;font-weight:bold;'>管理员 ，您好！</p>"
                + "<p style='text-indent:2em; font-size: 20px;'>你的伙伴匹配系统出现错误请及时处理,报错为:"
                + "<span style='font-size:30px;font-weight:bold;color:red'>" + str + "</span>请尽快处理！</p>"
                + "<p style='text-align:right; padding-right: 20px;'"
                + "<a href='' style='font-size: 18px'></a></p>"
                + "<span style='font-size: 18px; float:right; margin-right: 60px;'>" + sdf.format(new Date()) + "</span></body></html>";
       return sendEmail(qq, theme, message);
    }
}
