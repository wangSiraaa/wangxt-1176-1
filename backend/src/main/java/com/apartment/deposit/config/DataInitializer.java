package com.apartment.deposit.config;

import com.apartment.deposit.entity.SysUser;
import com.apartment.deposit.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1);
        List<SysUser> users = sysUserMapper.selectList(wrapper);

        String correctHashPrefix = "$2a$10$";
        String testPassword = "123456";

        for (SysUser user : users) {
            String currentPwd = user.getPassword();
            boolean needsUpdate = false;

            if (currentPwd == null || currentPwd.isEmpty()) {
                needsUpdate = true;
            } else if (!currentPwd.startsWith(correctHashPrefix)) {
                needsUpdate = true;
            } else {
                try {
                    if (!passwordEncoder.matches(testPassword, currentPwd)) {
                        needsUpdate = true;
                    }
                } catch (Exception e) {
                    needsUpdate = true;
                }
            }

            if (needsUpdate) {
                user.setPassword(passwordEncoder.encode(testPassword));
                sysUserMapper.updateById(user);
                System.out.println("[DataInitializer] 重置用户 " + user.getUsername() + " 的密码为: 123456");
            }
        }
    }
}
