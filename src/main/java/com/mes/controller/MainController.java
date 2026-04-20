package com.mes.controller;

import com.mes.entity.Menu;
import com.mes.entity.Permission;
import com.mes.service.AuthService;
import com.mes.service.MenuService;
import com.mes.service.PermissionService;
import com.mes.service.RoleService;
import com.mes.service.UserService;
import com.mes.view.StageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MainController {

    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final MenuService menuService;
    private final StageManager stageManager;
    private final ApplicationContext applicationContext;

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox dashboardView;

    @FXML
    private Label userCountLabel;

    @FXML
    private Label roleCountLabel;

    @FXML
    private Label permissionCountLabel;

    @FXML
    private ListView<String> permissionListView;

    @FXML
    private Label userInfoLabel;

    @FXML
    private VBox sidebarMenu;

    private Map<String, Button> menuButtonMap = new HashMap<>();

    public MainController(AuthService authService, UserService userService,
                          RoleService roleService, PermissionService permissionService,
                          MenuService menuService, StageManager stageManager, 
                          ApplicationContext applicationContext) {
        this.authService = authService;
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.menuService = menuService;
        this.stageManager = stageManager;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        String welcome = "欢迎, " + authService.getCurrentUser().getRealName();
        userInfoLabel.setText(welcome);

        updateDashboard();
        loadDynamicMenus();
    }

    private void loadDynamicMenus() {
        // 清空现有菜单（保留Region和底部元素）
        List<javafx.scene.Node> nodesToKeep = sidebarMenu.getChildren().stream()
                .filter(node -> node instanceof Region || node instanceof Separator || 
                       (node instanceof HBox) || 
                       (node instanceof Button && "🚪  退出登录".equals(((Button) node).getText())))
                .collect(Collectors.toList());
        
        sidebarMenu.getChildren().clear();
        menuButtonMap.clear();

        // 从数据库加载当前用户的菜单
        Long userId = authService.getCurrentUser().getId();
        List<Menu> menus = menuService.findByUserId(userId);

        // 创建控制台按钮（固定）
        Button dashboardBtn = createMenuButton("🏠  控制台", "showDashboard", "/fxml/dashboard.fxml");
        dashboardBtn.getStyleClass().add("active");
        sidebarMenu.getChildren().add(dashboardBtn);

        // 动态创建菜单按钮
        for (Menu menu : menus) {
            if (!"dashboard".equals(menu.getCode())) {
                String buttonText = menu.getIcon() + "  " + menu.getName();
                Button menuBtn = createMenuButton(buttonText, menu.getActionMethod(), menu.getFxmlPath());
                sidebarMenu.getChildren().add(menuBtn);
                menuButtonMap.put(menu.getCode(), menuBtn);
            }
        }

        // 添加底部元素
        sidebarMenu.getChildren().addAll(nodesToKeep);
    }

    private Button createMenuButton(String text, String actionMethod, String fxmlPath) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        button.setOnAction(event -> {
            handleMenuAction(actionMethod, fxmlPath);
            setActiveButton(button);
        });
        return button;
    }

    private void handleMenuAction(String actionMethod, String fxmlPath) {
        switch (actionMethod) {
            case "showDashboard":
                showDashboard();
                break;
            case "showUserManagement":
                loadView("/fxml/user-management.fxml");
                break;
            case "showRoleManagement":
                loadView("/fxml/role-management.fxml");
                break;
            case "showPermissionManagement":
                loadView("/fxml/permission-management.fxml");
                break;
            case "showUnitOfMeasure":
                loadView("/fxml/unit-of-measure.fxml");
                break;
            case "showChangePassword":
                loadView("/fxml/change-password.fxml");
                break;
            default:
                if (fxmlPath != null) {
                    loadView(fxmlPath);
                }
        }
    }

    private void updateDashboard() {
        userCountLabel.setText(String.valueOf(userService.findAll().size()));
        roleCountLabel.setText(String.valueOf(roleService.findAll().size()));
        permissionCountLabel.setText(String.valueOf(permissionService.findAll().size()));

        var permissions = authService.getCurrentUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getDescription)
                .distinct()
                .collect(Collectors.toList());
        permissionListView.setItems(FXCollections.observableArrayList(permissions));
    }

    private void setActiveButton(Button activeButton) {
        findAllButtonsWithStyle(activeButton.getScene().getRoot(), "sidebar-button")
                .forEach(btn -> btn.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }

    private Set<Button> findAllButtonsWithStyle(javafx.scene.Parent parent, String styleClass) {
        Set<Button> buttons = new HashSet<>();
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button && node.getStyleClass().contains(styleClass)) {
                buttons.add((Button) node);
            }
            if (node instanceof javafx.scene.Parent) {
                buttons.addAll(findAllButtonsWithStyle((javafx.scene.Parent) node, styleClass));
            }
        }
        return buttons;
    }

    @FXML
    public void showDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(dashboardView);
        updateDashboard();
        Button dashboardBtn = findDashboardButton();
        if (dashboardBtn != null) {
            setActiveButton(dashboardBtn);
        }
    }

    private Button findDashboardButton() {
        for (javafx.scene.Node node : sidebarMenu.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().contains("控制台")) {
                return (Button) node;
            }
        }
        return null;
    }

    @FXML
    public void showUserManagement() {
        loadView("/fxml/user-management.fxml");
        setActiveButton(menuButtonMap.get("user"));
    }

    @FXML
    public void showRoleManagement() {
        loadView("/fxml/role-management.fxml");
        setActiveButton(menuButtonMap.get("role"));
    }

    @FXML
    public void showPermissionManagement() {
        loadView("/fxml/permission-management.fxml");
        setActiveButton(menuButtonMap.get("permission"));
    }

    @FXML
    public void showUnitOfMeasure() {
        loadView("/fxml/unit-of-measure.fxml");
        setActiveButton(menuButtonMap.get("unitofmeasure"));
    }

    @FXML
    public void showChangePassword() {
        loadView("/fxml/change-password.fxml");
        Button changePasswordBtn = findButtonByText("🔐  修改密码");
        if (changePasswordBtn != null) {
            setActiveButton(changePasswordBtn);
        }
    }

    private Button findButtonByText(String text) {
        for (javafx.scene.Node node : sidebarMenu.getChildren()) {
            if (node instanceof Button && text.equals(((Button) node).getText())) {
                return (Button) node;
            }
        }
        return null;
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("确定要退出登录吗？");
            alert.getDialogPane().getStylesheets().add("/css/style.css");
            
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    authService.logout();
                    stageManager.showLogin();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            authService.logout();
            stageManager.showLogin();
        }
    }

    @FXML
    public void closeWindow() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void minimizeWindow() {
        stageManager.minimize();
    }

    @FXML
    public void maximizeWindow() {
        stageManager.maximize();
    }
}
