package com.mes.controller;

import com.mes.entity.Permission;
import com.mes.service.AuthService;
import com.mes.service.PermissionService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PermissionManagementController {

    private final PermissionService permissionService;
    private final AuthService authService;

    @FXML
    private TableView<Permission> permissionTable;

    @FXML
    private TableColumn<Permission, Boolean> permActionColumn;

    @FXML
    private Button addPermBtn;

    public PermissionManagementController(PermissionService permissionService, AuthService authService) {
        this.permissionService = permissionService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        loadPermissions();
        setupActionColumn();
        setupButtonPermissions();
    }

    private void setupButtonPermissions() {
        if (addPermBtn != null) {
            addPermBtn.setVisible(authService.hasPermission("permission:create"));
            addPermBtn.setManaged(authService.hasPermission("permission:create"));
        }
    }

    private void loadPermissions() {
        permissionTable.setItems(FXCollections.observableArrayList(permissionService.findAll()));
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("permission:edit");
        boolean canDelete = authService.hasPermission("permission:delete");

        Callback<TableColumn<Permission, Boolean>, TableCell<Permission, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("编辑");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            Permission perm = getTableView().getItems().get(getIndex());
                            showEditDialog(perm);
                        });

                        deleteBtn.setOnAction(event -> {
                            Permission perm = getTableView().getItems().get(getIndex());
                            deletePermission(perm);
                        });

                        if (canEdit) pane.getChildren().add(editBtn);
                        if (canDelete) pane.getChildren().add(deleteBtn);
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };

        permActionColumn.setCellFactory(cellFactory);
        permActionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    @FXML
    public void showAddDialog() {
        Dialog<Permission> dialog = createPermissionDialog("添加权限", null);
        dialog.showAndWait().ifPresent(perm -> {
            permissionService.create(perm);
            loadPermissions();
        });
    }

    private void showEditDialog(Permission perm) {
        Dialog<Permission> dialog = createPermissionDialog("编辑权限", perm);
        dialog.showAndWait().ifPresent(updated -> {
            permissionService.update(updated);
            loadPermissions();
        });
    }

    private Dialog<Permission> createPermissionDialog(String title, Permission perm) {
        Dialog<Permission> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.getStyleClass().add("input-field");

        TextField descField = new TextField();
        descField.getStyleClass().add("input-field");

        if (perm != null) {
            nameField.setText(perm.getName());
            descField.setText(perm.getDescription());
        }

        grid.add(new Label("权限标识:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Permission result = perm != null ? perm : new Permission();
                result.setName(nameField.getText());
                result.setDescription(descField.getText());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void deletePermission(Permission perm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除权限 " + perm.getName() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                permissionService.delete(perm.getId());
                loadPermissions();
            }
        });
    }
}
