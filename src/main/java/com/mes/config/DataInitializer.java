package com.mes.config;

import com.mes.entity.Permission;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.repository.PermissionRepository;
import com.mes.repository.RoleRepository;
import com.mes.repository.UserRepository;
import com.mes.util.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        Permission userManage = createPermissionIfNotExists("user:manage", "用户管理");
        Permission userAdd = createPermissionIfNotExists("user:add", "添加用户");
        Permission userEdit = createPermissionIfNotExists("user:edit", "编辑用户");
        Permission userDelete = createPermissionIfNotExists("user:delete", "删除用户");
        Permission userResetPassword = createPermissionIfNotExists("user:reset-password", "重置用户密码");
        Permission roleManage = createPermissionIfNotExists("role:manage", "角色管理");
        Permission roleAdd = createPermissionIfNotExists("role:add", "添加角色");
        Permission roleEdit = createPermissionIfNotExists("role:edit", "编辑角色");
        Permission roleDelete = createPermissionIfNotExists("role:delete", "删除角色");
        Permission permissionManage = createPermissionIfNotExists("permission:manage", "权限管理");
        Permission passwordChange = createPermissionIfNotExists("password:change", "修改密码");
        Permission mesView = createPermissionIfNotExists("mes:view", "查看MES数据");

        Permission[] allAdminPermissions = {
                userManage, userAdd, userEdit, userDelete, userResetPassword,
                roleManage, roleAdd, roleEdit, roleDelete,
                permissionManage, passwordChange, mesView
        };

        Role adminRole = createRoleWithPermissions("ADMIN", "系统管理员", allAdminPermissions);
        Role userRole = createRoleWithPermissions("USER", "普通用户", passwordChange, mesView);

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(PasswordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(PasswordEncoder.encode("user123"));
            user.setRealName("普通用户");
            user.setRoles(new HashSet<>(Arrays.asList(userRole)));
            userRepository.save(user);
        }
    }

    private Permission createPermissionIfNotExists(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    return permissionRepository.save(permission);
                });
    }

    private Role createRoleWithPermissions(String name, String description, Permission... permissions) {
        return roleRepository.findByName(name)
                .map(role -> {
                    role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                    return roleRepository.save(role);
                })
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                    return roleRepository.save(role);
                });
    }
}
