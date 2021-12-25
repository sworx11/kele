package me.wemeet.kele.controller;

import me.wemeet.kele.common.response.KeleResponseEntity;
import me.wemeet.kele.common.response.KeleResponseStatus;
import me.wemeet.kele.entity.User;
import me.wemeet.kele.service.CommonService;
import me.wemeet.kele.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Quino Wu
 * @since 2021-12-04
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private CommonService commonService;

    @GetMapping("verify/email")
    public KeleResponseEntity<Long> verifyEmail(String email) {
        long count = userService.countUserByEmail(email);
        return KeleResponseEntity.ok(count);
    }

    @GetMapping("verify/phone")
    public KeleResponseEntity<Long> verifyPhone(String phone) {
        long count = userService.countUserByPhone(phone);
        return KeleResponseEntity.ok(count);
    }

    @PostMapping("register")
    public KeleResponseEntity<User> register(User user, @RequestParam(name = "signCode") String signCode, @RequestParam(name = "lang", defaultValue = "zh") String lang) {
        if (commonService.testSignCode(user.getEmail(), signCode)) {
            userService.createUser(user.getEmail(), user.getName(), user.getPassword(), lang);

            commonService.deleteSignCode(user.getEmail());
            return KeleResponseEntity.ok(user);
        } else {
            return new KeleResponseEntity<>(KeleResponseStatus.INSUFFICIENT_PERMISSION);
        }
    }

    @PostMapping("login")
    public KeleResponseEntity<User> login(User user) {
        user = userService.login(user);
        if (user != null) {
            String token = commonService.generateAccessToken(user.getId());
            Map<String, Object> ext = new HashMap<>();
            ext.put("token", token);
            return new KeleResponseEntity<>(KeleResponseStatus.SUCCESS, user, ext);
        } else {
            return new KeleResponseEntity<>(KeleResponseStatus.LOGIN_ERROR);
        }
    }

    @PostMapping("logout")
    public KeleResponseEntity<String> logout(User user) {
        commonService.deleteAccessToken(user.getId());
        return KeleResponseEntity.ok();
    }
}
