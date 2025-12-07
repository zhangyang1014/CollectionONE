package com.cco.security;

import com.cco.common.constant.Constants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 * TODO: 从数据库加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: 从数据库查询用户信息
        // 临时实现：硬编码用户信息
        
        // 添加调试日志
        System.out.println("[UserDetailsServiceImpl] loadUserByUsername called with username: [" + username + "]");
        System.out.println("[UserDetailsServiceImpl] username is null: " + (username == null));
        System.out.println("[UserDetailsServiceImpl] username is empty: " + (username != null && username.isEmpty()));
        
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        String password;
        
        if ("superadmin".equalsIgnoreCase(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Constants.Role.SUPER_ADMIN));
            password = passwordEncoder.encode("123456");
        } else if ("tenantadmin".equalsIgnoreCase(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Constants.Role.TENANT_ADMIN));
            password = passwordEncoder.encode("admin123");
        } else {
            // 支持催员用户（IM端登录时使用催员ID作为username）
            // 催员ID格式通常是：BTQ001, BTSK001 等
            // 为催员分配基本权限
            authorities.add(new SimpleGrantedAuthority("ROLE_" + Constants.Role.COLLECTOR));
            password = passwordEncoder.encode("123456"); // Mock密码，实际不会使用
        }

        return new User(username, password, true, true, true, true, authorities);
    }

}

