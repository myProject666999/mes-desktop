package com.mes.config;

import com.mes.entity.Menu;
import com.mes.entity.Permission;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.repository.MenuRepository;
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
    private final MenuRepository menuRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PermissionRepository permissionRepository, MenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public void run(String... args) {
        // 创建权限
        Permission userManage = createPermissionIfNotExists("user:manage", "用户管理");
        Permission roleManage = createPermissionIfNotExists("role:manage", "角色管理");
        Permission permissionManage = createPermissionIfNotExists("permission:manage", "权限管理");
        Permission passwordChange = createPermissionIfNotExists("password:change", "修改密码");
        Permission mesView = createPermissionIfNotExists("mes:view", "查看MES数据");
        Permission unitOfMeasureManage = createPermissionIfNotExists("unitofmeasure:manage", "计量单位管理");

        // 创建角色
        Role adminRole = createRoleIfNotExists("ADMIN", "系统管理员",
                userManage, roleManage, permissionManage, passwordChange, mesView, unitOfMeasureManage);

        Role userRole = createRoleIfNotExists("USER", "普通用户",
                passwordChange, mesView);

        // 创建用户
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

        // 创建菜单
        Menu dashboardMenu = createMenuIfNotExists("控制台", "dashboard", "🏠", null, "showDashboard", 1, adminRole, userRole);
        Menu userMenu = createMenuIfNotExists("用户管理", "user", "👥", "/fxml/user-management.fxml", "showUserManagement", 2, adminRole);
        Menu roleMenu = createMenuIfNotExists("角色管理", "role", "🎭", "/fxml/role-management.fxml", "showRoleManagement", 3, adminRole);
        Menu permissionMenu = createMenuIfNotExists("权限管理", "permission", "🔑", "/fxml/permission-management.fxml", "showPermissionManagement", 4, adminRole);
        Menu unitOfMeasureMenu = createMenuIfNotExists("计量单位", "unitofmeasure", "📏", "/fxml/unit-of-measure.fxml", "showUnitOfMeasure", 5, adminRole);
        Menu changePasswordMenu = createMenuIfNotExists("修改密码", "changepassword", "🔐", "/fxml/change-password.fxml", "showChangePassword", 6, adminRole, userRole);
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

    private Role createRoleIfNotExists(String name, String description, Permission... permissions) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                    return roleRepository.save(role);
                });
    }

    private Menu createMenuIfNotExists(String name, String code, String icon, String fxmlPath,
                                       String actionMethod, Integer sortOrder, Role... roles) {
        return menuRepository.findByCode(code)
                .orElseGet(() -> {
                    Menu menu = new Menu();
                    menu.setName(name);
                    menu.setCode(code);
                    menu.setIcon(icon);
                    menu.setFxmlPath(fxmlPath);
                    menu.setActionMethod(actionMethod);
                    menu.setSortOrder(sortOrder);
                    menu.setEnabled(true);
                    menu.setRoles(new HashSet<>(Arrays.asList(roles)));
                    return menuRepository.save(menu);
                });
    }
}
