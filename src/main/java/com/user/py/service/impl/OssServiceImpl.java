package com.user.py.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.DataUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mode.constant.RedisKey;
import com.user.py.mode.domain.Team;
import com.user.py.mode.domain.User;
import com.user.py.mode.utils.IpUtilSealUp;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IUserService;
import com.user.py.service.OssService;
import com.user.py.service.TeamService;
import com.user.py.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author ice
 * @date 2022/9/17 12:48
 */
@Service
@Slf4j
public class OssServiceImpl implements OssService {
    @Resource
    private RabbitService rabbitService;


    @Resource
    private IUserService userService;
    @Resource
    private TeamService teamService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisCache redisCache;

    /**
     * 用户头像的上传
     *
     * @param file      上传的文件
     * @param loginUser 登录的用户
     * @return 返回url
     */
    @Override
    public String upload(MultipartFile file, User loginUser) {

        // 判断用户是否上传过
        String userId = loginUser.getId();
        RLock lock = redissonClient.getLock(RedisKey.redisFileAvatarLock+userId.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                if (file == null) {
                    throw new GlobalException(ErrorCode.NULL_ERROR);
                }
                String redisKey = RedisKey.ossAvatarUserRedisKey + userId;
                String url = getUrl(redisKey, file);
                User user = new User();
                user.setId(userId);
                user.setAvatarUrl(url);
                rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.OSS_KEY, user);
                Integer integer = TimeUtils.getRemainSecondsOneDay(new Date());
                redisCache.setCacheObject(redisKey, new Date().toString(), integer, TimeUnit.SECONDS);
                // 删除掉主页的用户
                rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, RedisKey.redisIndexKey);
                return url;
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 队伍头像上传
     *
     * @param file      文件流
     * @param loginUser 登录的用户
     * @param teamID    队伍的id
     * @return 返回头像url
     */
    @Override
    public String upFileByTeam(MultipartFile file, User loginUser, String teamID) {

        String userId = loginUser.getId();
        RLock lock = redissonClient.getLock(RedisKey.redisFileByTeamAvatarLock + userId.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                if (file == null || !StringUtils.hasText(teamID)) {
                    throw new GlobalException(ErrorCode.NULL_ERROR);
                }
                Team team = teamService.getTeamByTeamUser(teamID, userId);
                if (team == null) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍不存在...");
                }
                String teamUserId = team.getUserId();
                if (!userId.equals(teamUserId)) {
                    throw new GlobalException(ErrorCode.NO_AUTH, "权限不足...");
                }
                String redisKey = RedisKey.ossAvatarTeamRedisKey + teamID;
                String url = getUrl(redisKey, file);
                team.setAvatarUrl(url);
                boolean teamByTeam = teamService.updateTeamByTeam(team);
                if (!teamByTeam) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "上传错误...");
                }
                Integer integer = TimeUtils.getRemainSecondsOneDay(new Date());
                redisCache.setCacheObject(redisKey, new Date().toString(), integer, TimeUnit.SECONDS);
                return url;
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return null;
    }

    public String getUrl(String redisKey, MultipartFile file) {
        String key = redisCache.getCacheObject(redisKey);
        if (StringUtils.hasText(key)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "今日上限...");
        }
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtils.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantPropertiesUtils.BUCKET_NAME;
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        // 返回客服端的原始名字
        String originalFilename = IdUtil.simpleUUID() + file.getOriginalFilename();
        String objectName = "user/" + new DateTime().toString("yyyy/MM/dd") + "/" + originalFilename;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } catch (Exception oe) {
            log.error(oe.getMessage());
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 忘记密码
     *
     * @param responseEmail responseEmail
     * @param request       用户信息
     * @return true
     */
    @Override
    public boolean sendForgetEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(RedisKey.redisFileByForgetLock+id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                ipEmailUtil(request);
                if (responseEmail == null) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
                }
                String email = responseEmail.getEmail();
                String userAccount = responseEmail.getUserAccount();
                if (!StringUtils.hasText(email)) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
                }
                if (!StringUtils.hasText(userAccount)) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请输入账号");
                }

                if (REUtils.isEmail(email)) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "请输入正确邮箱");
                }
                // 根据邮箱查找用户
                User user = userService.forgetUserEmail(email);

                if (user == null) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "该邮箱没有注册过");
                }
                String userUserAccount = user.getUserAccount();
                if (!userAccount.equals(userUserAccount)) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请输入该邮箱绑定的账号");
                }

                String code = RandomUtil.getRandomFour();
                String[] split = email.split("@");
                String name = split[0];
                boolean sendQQEmail = sendQQEmail(email, code, name);
                if (!sendQQEmail) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "发送失败请重试");
                }
                String redisKey = RedisKey.redisForgetCode + email;
                return redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 发送注册邮件
     *
     * @param responseEmail 接受的邮件
     * @return 返回Boolean
     */
    @Override
    public boolean sendRegisterEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(RedisKey.redisFileByRegisterLock+id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                ipEmailUtil(request);
                String email = getEmail(responseEmail);
                if (userService.seeUserEmail(email)) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册邮箱重复");
                }
                email = email.toLowerCase();
                String code = getCode(email);
                String redisKey = RedisKey.redisRegisterCode + email;
                return redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 发送绑定验证码
     *
     * @param responseEmail responseEmail
     * @param request       s
     * @return s
     */
    @Override
    public boolean sendBinDingEMail(ResponseEmail responseEmail, HttpServletRequest request) {
        String id = request.getSession().getId();
        RLock lock = redissonClient.getLock(RedisKey.redisFileByBingDingLock+id.intern());
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                // 获取真实ip
                if (responseEmail == null) {
                    throw new GlobalException(ErrorCode.NULL_ERROR);
                }

                ipEmailUtil(request);
                User user = UserUtils.getLoginUser(request);
                String email = getEmail(responseEmail);
                String userEmail = user.getEmail();
                if (StringUtils.hasText(userEmail)) {
                    if (!userEmail.equals(email)) {
                        throw new GlobalException(ErrorCode.PARAMS_ERROR, "");
                    }
                }
                if (userService.seeUserEmail(email)) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
                }
                email = email.toLowerCase();
                String code = getCode(email);
                String redisKey = RedisKey.redisFileByBingDingKey + email;
                return redisCache.setCacheObject(redisKey, code, 60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "系统错误");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }

    private String getCode(String email) {
        String code = RandomUtil.getRandomSix();
        String[] split = email.split("@");
        String name = split[0];
        boolean sendQQEmail = sendQQEmail(email, code, name);
        if (!sendQQEmail) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "发送失败请重试");
        }
        return code;
    }

    private String getEmail(ResponseEmail responseEmail) {
        if (responseEmail == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
        }
        String email = responseEmail.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入邮箱");
        }

        if (REUtils.isEmail(email)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请输入正确邮箱");
        }
        return email.toLowerCase();
    }

    /**
     * 发送邮件(参数自己根据自己的需求来修改，发送短信验证码)
     *
     * @param receives 接收人的邮箱
     * @param code     验证码
     * @param name     收件人的姓名
     * @return 是否成功
     */
    public boolean sendQQEmail(String receives, String code, String name) {

        SimpleDateFormat sdf = DataUtils.getFdt();
        // 模板
        String str = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><p style='font-size: 20px;font-weight:bold;'>尊敬的：" + name + "，您好！</p>"
                + "<p style='text-indent:2em; font-size: 20px;'>欢迎注册伙伴匹配系统，您本次的验证码是 "
                + "<span style='font-size:30px;font-weight:bold;color:red'>" + code + "</span>，1分钟之内有效，请尽快使用！</p>"
                + "<p style='text-align:right; padding-right: 20px;'"
                + "<a href='http://www.hyycinfo.com' style='font-size: 18px'></a></p>"
                + "<span style='font-size: 18px; float:right; margin-right: 60px;'>" + sdf.format(new Date()) + "</span></body></html>";
        String them = "验证码";
        return EmailUtil.sendEmail(receives, them, str);
    }

    private void ipEmailUtil(HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        Integer num = redisCache.getCacheObject(ipAddress);
        if (num != null) {
            // 一天的次数过多
            if (num >= 10 && num < 20) {
                redisCache.increment(ipAddress);
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求次数过多，今天还剩余" + (20 - num - 1) + "次");
            } else if (num >= 20) {
                IpUtilSealUp.addIpList(ipAddress);
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求次数过多,请明天再试");
            } else {
                redisCache.increment(ipAddress);
            }
        } else {
            redisCache.setCacheObject(ipAddress, 1, TimeUtils.getRemainSecondsOneDay(new Date()),
                    TimeUnit.SECONDS);
        }
    }


}
