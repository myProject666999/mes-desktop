package com.mes.controller;

import com.mes.dto.UserDTO;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.service.AuthService;
import com.mes.service.RoleService;
import com.mes.service.UserService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserManagementController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthService authService;

    @FXML
    private TableView<UserDTO> userTable;

    @FXML
    private TableColumn<UserDTO, Boolean> actionColumn;

    @FXML
    private Button addUserBtn;

    public UserManagementController(UserService userService, RoleService roleService, AuthService authService) {
        this.userService = userService;
        this.roleService = roleService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        loadUsers();
        setupActionColumn();
        setupButtonPermissions();
    }

    private void setupButtonPermissions() {
        addUserBtn.setVisible(authService.hasPermission("user:create"));
        addUserBtn.setManaged(authService.hasPermission("user:create"));
    }

    private void loadUsers() {
        List<UserDTO> users = userService.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void setupActionColumn() {
        Callback<TableColumn<UserDTO, Boolean>, TableCell<UserDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("编辑");
                    final Button resetBtn = new Button("重置密码");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        resetBtn.getStyleClass().addAll("action-button");
                        resetBtn.setStyle("-fx-background-color: #007acc; -fx-text-fill: white;");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        resetBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            showResetPasswordDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            deleteUser(dto);
                        });
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            pane.getChildren().clear();
                            if (authService.hasPermission("user:edit")) {
                                pane.getChildren().add(editBtn);
                            }
                            if (authService.hasPermission("user:resetPassword")) {
                                pane.getChildren().add(resetBtn);
                            }
                            if (authService.hasPermission("user:delete")) {
                                pane.getChildren().add(deleteBtn);
                            }
                            setGraphic(pane.getChildren().isEmpty() ? null : pane);
                        }
                    }
                };

        actionColumn.setCellFactory(cellFactory);
        actionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    @FXML
    public void showAddDialog() {
        Dialog<CreateUserResult> dialog = createAddUserDialog();
        dialog.showAndWait().ifPresent(result -> {
            userService.create(result.user, result.password, result.user.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList()));
            loadUsers();
            showAlert("成功", "用户创建成功，密码已设置");
        });
    }

    private void showEditDialog(UserDTO dto) {
        User user = userService.findById(dto.getId());
        Dialog<User> dialog = createEditUserDialog(user);
        dialog.showAndWait().ifPresent(updated -> {
            userService.update(updated, updated.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList()));
            loadUsers();
        });
    }

    private Dialog<CreateUserResult> createAddUserDialog() {
        Dialog<CreateUserResult> dialog = new Dialog<>();
        dialog.setTitle("添加用户");
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("input-field");
        usernameField.setPromptText("用户名");

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("input-field");
        passwordField.setPromptText("密码");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.getStyleClass().add("input-field");
        confirmPasswordField.setPromptText("确认密码");

        TextField realNameField = new TextField();
        realNameField.getStyleClass().add("input-field");
        realNameField.setPromptText("真实姓名");

        TextField emailField = new TextField();
        emailField.getStyleClass().add("input-field");
        emailField.setPromptText("邮箱");

        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("input-field");
        phoneField.setPromptText("电话");

        CheckBox enabledCheckBox = new CheckBox("启用");
        enabledCheckBox.setSelected(true);

        ListView<Role> roleListView = new ListView<>();
        roleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        roleListView.setItems(FXCollections.observableArrayList(roleService.findAll()));
        roleListView.setPrefHeight(120);
        roleListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setText(role.getName() + " - " + role.getDescription());
                    Platform.runLater(() -> checkBox.setSelected(getListView().getSelectionModel().getSelectedItems().contains(role)));
                    setGraphic(checkBox);
                }
            }
        });

        int row = 0;
        grid.add(new Label("用户名:"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("密码:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("确认密码:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);
        grid.add(new Label("真实姓名:"), 0, row);
        grid.add(realNameField, 1, row++);
        grid.add(new Label("邮箱:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("电话:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("角色:"), 0, row);
        grid.add(roleListView, 1, row++);
        grid.add(enabledCheckBox, 1, row);

        dialog.getDialogPane().setContent(grid);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateAddForm(usernameField, passwordField, confirmPasswordField, saveButton));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateAddForm(usernameField, passwordField, confirmPasswordField, saveButton));
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateAddForm(usernameField, passwordField, confirmPasswordField, saveButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("错误", "两次输入的密码不一致");
                    return null;
                }
                User user = new User();
                user.setUsername(usernameField.getText().trim());
                user.setRealName(realNameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                user.setEnabled(enabledCheckBox.isSelected());
                user.setRoles(new java.util.HashSet<>(roleListView.getSelectionModel().getSelectedItems()));
                return new CreateUserResult(user, passwordField.getText());
            }
            return null;
        });

        return dialog;
    }

    private void validateAddForm(TextField usernameField, PasswordField passwordField, PasswordField confirmPasswordField, Button saveButton) {
        boolean valid = !usernameField.getText().trim().isEmpty()
                && !passwordField.getText().isEmpty()
                && passwordField.getText().equals(confirmPasswordField.getText());
        saveButton.setDisable(!valid);
    }

    private Dialog<User> createEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("编辑用户");
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("input-field");
        usernameField.setPromptText("用户名");
        usernameField.setText(user.getUsername());
        usernameField.setDisable(true);

        TextField realNameField = new TextField();
        realNameField.getStyleClass().add("input-field");
        realNameField.setPromptText("真实姓名");
        realNameField.setText(user.getRealName());

        TextField emailField = new TextField();
        emailField.getStyleClass().add("input-field");
        emailField.setPromptText("邮箱");
        emailField.setText(user.getEmail());

        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("input-field");
        phoneField.setPromptText("电话");
        phoneField.setText(user.getPhone());

        CheckBox enabledCheckBox = new CheckBox("启用");
        enabledCheckBox.setSelected(user.isEnabled());

        ListView<Role> roleListView = new ListView<>();
        roleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        roleListView.setItems(FXCollections.observableArrayList(roleService.findAll()));
        roleListView.setPrefHeight(120);
        roleListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setText(role.getName() + " - " + role.getDescription());
                    Platform.runLater(() -> checkBox.setSelected(getListView().getSelectionModel().getSelectedItems().contains(role)));
                    setGraphic(checkBox);
                }
            }
        });
        user.getRoles().forEach(role -> roleListView.getSelectionModel().select(role));

        int row = 0;
        grid.add(new Label("用户名:"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("真实姓名:"), 0, row);
        grid.add(realNameField, 1, row++);
        grid.add(new Label("邮箱:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("电话:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("角色:"), 0, row);
        grid.add(roleListView, 1, row++);
        grid.add(enabledCheckBox, 1, row);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setRealName(realNameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                user.setEnabled(enabledCheckBox.isSelected());
                user.setRoles(new java.util.HashSet<>(roleListView.getSelectionModel().getSelectedItems()));
                return user;
            }
            return null;
        });

        return dialog;
    }

    private void showResetPasswordDialog(UserDTO dto) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("重置密码");
        dialog.setHeaderText("重置用户 " + dto.getUsername() + " 的密码");
        dialog.setContentText("请输入新密码:");
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (!newPassword.trim().isEmpty()) {
                userService.resetPassword(dto.getId(), newPassword.trim());
                showAlert("成功", "密码已重置");
            } else {
                showAlert("错误", "密码不能为空");
            }
        });
    }

    private void deleteUser(UserDTO dto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除用户 " + dto.getUsername() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userService.delete(dto.getId());
                loadUsers();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }

    private static class CreateUserResult {
        final User user;
        final String password;

        CreateUserResult(User user, String password) {
            this.user = user;
            this.password = password;
        }
    }
}
