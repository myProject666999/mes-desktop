package com.mes.controller;

import com.mes.entity.Unit;
import com.mes.service.AuthService;
import com.mes.service.UnitService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UnitManagementController {

    private final UnitService unitService;
    private final AuthService authService;

    @FXML
    private TextField codeSearchField;

    @FXML
    private TextField nameSearchField;

    @FXML
    private TableView<Unit> unitTable;

    @FXML
    private TableColumn<Unit, Boolean> selectColumn;

    @FXML
    private TableColumn<Unit, String> mainUnit;

    @FXML
    private TableColumn<Unit, String> enabled;

    @FXML
    private TableColumn<Unit, Boolean> unitActionColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    public UnitManagementController(UnitService unitService, AuthService authService) {
        this.unitService = unitService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadUnits();
        setupActionColumn();
        setupButtonPermissions();
    }

    private void setupButtonPermissions() {
        boolean hasManagePermission = authService.hasPermission("unit:manage");
        addButton.setVisible(hasManagePermission);
        addButton.setManaged(hasManagePermission);
        editButton.setVisible(hasManagePermission);
        editButton.setManaged(hasManagePermission);
        deleteButton.setVisible(hasManagePermission);
        deleteButton.setManaged(hasManagePermission);
        exportButton.setVisible(hasManagePermission);
        exportButton.setManaged(hasManagePermission);
        unitActionColumn.setVisible(hasManagePermission);
    }

    private void setupTable() {
        selectColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(false));
        selectColumn.setCellFactory(param -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        Unit unit = getTableView().getItems().get(getIndex());
                        unitTable.getProperties().put(unit, newVal);
                    });
                }
            }
        });

        enabled.setCellValueFactory(cellData -> {
            Unit unit = cellData.getValue();
            return new SimpleStringProperty(unit.getEnabled() ? "是" : "否");
        });

        mainUnit.setCellValueFactory(cellData -> {
            Unit unit = cellData.getValue();
            return new SimpleStringProperty(unit.getMainUnit() ? "是" : "否");
        });
    }

    private void loadUnits() {
        unitTable.getItems().clear();
        unitTable.setItems(FXCollections.observableArrayList(unitService.findAll()));
        unitTable.refresh();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Unit, Boolean>, TableCell<Unit, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            Unit unit = getTableView().getItems().get(getIndex());
                            showEditDialog(unit);
                        });

                        deleteBtn.setOnAction(event -> {
                            Unit unit = getTableView().getItems().get(getIndex());
                            deleteSingleUnit(unit);
                        });
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

        unitActionColumn.setCellFactory(cellFactory);
        unitActionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    private List<Unit> getSelectedUnits() {
        List<Unit> selected = new ArrayList<>();
        for (int i = 0; i < unitTable.getItems().size(); i++) {
            Unit unit = unitTable.getItems().get(i);
            Boolean selectedFlag = (Boolean) unitTable.getProperties().get(unit);
            if (selectedFlag != null && selectedFlag) {
                selected.add(unit);
            }
        }
        return selected;
    }

    @FXML
    public void search() {
        String code = codeSearchField.getText();
        String name = nameSearchField.getText();
        unitTable.getItems().clear();
        unitTable.setItems(FXCollections.observableArrayList(unitService.search(code, name)));
        unitTable.refresh();
    }

    @FXML
    public void reset() {
        codeSearchField.clear();
        nameSearchField.clear();
        loadUnits();
    }

    @FXML
    public void showAddDialog() {
        Dialog<Unit> dialog = createUnitDialog("新增计量单位", null);
        dialog.showAndWait().ifPresent(unit -> {
            unitService.create(unit);
            loadUnits();
        });
    }

    @FXML
    public void showEditDialog() {
        List<Unit> selected = getSelectedUnits();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要修改的数据");
            return;
        }
        if (selected.size() > 1) {
            showAlert("提示", "只能选择一条数据进行修改");
            return;
        }
        showEditDialog(selected.get(0));
    }

    private void showEditDialog(Unit unit) {
        Dialog<Unit> dialog = createUnitDialog("修改计量单位", unit);
        dialog.showAndWait().ifPresent(updated -> {
            unitService.update(updated);
            loadUnits();
        });
    }

    @FXML
    public void deleteSelected() {
        List<Unit> selected = getSelectedUnits();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要删除的数据");
            return;
        }
        deleteUnits(selected);
    }

    private void deleteSingleUnit(Unit unit) {
        List<Unit> units = new ArrayList<>();
        units.add(unit);
        deleteUnits(units);
    }

    private void deleteUnits(List<Unit> units) {
        String names = units.stream().map(Unit::getName).collect(Collectors.joining(", "));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除以下计量单位吗？\n" + names);
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                List<Long> ids = units.stream().map(Unit::getId).collect(Collectors.toList());
                unitService.deleteAll(ids);
                loadUnits();
            }
        });
    }

    @FXML
    public void exportExcel() {
        List<Unit> units = unitTable.getItems();
        if (units.isEmpty()) {
            showAlert("提示", "没有数据可导出");
            return;
        }

        try {
            String fileName = "计量单位_" + System.currentTimeMillis() + ".csv";
            java.io.File file = new java.io.File(fileName);
            String absolutePath = file.getAbsolutePath();
            FileWriter writer = new FileWriter(file);

            writer.write("ID,单位编码,单位名称,启用主单位,是否启用,描述\n");
            for (Unit unit : units) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s\n",
                        unit.getId(),
                        unit.getCode() != null ? unit.getCode() : "",
                        unit.getName(),
                        unit.getMainUnit() ? "是" : "否",
                        unit.getEnabled() ? "是" : "否",
                        unit.getDescription() != null ? unit.getDescription() : ""));
            }
            writer.close();

            showAlert("成功", "导出成功！\n文件路径: " + absolutePath);
        } catch (IOException e) {
            showAlert("错误", "导出失败: " + e.getMessage());
        }
    }

    private Dialog<Unit> createUnitDialog(String title, Unit unit) {
        Dialog<Unit> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        ButtonType saveButtonType = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.getStyleClass().add("input-field");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("input-field");

        CheckBox mainUnitCheckBox = new CheckBox("启用主单位");
        CheckBox enabledCheckBox = new CheckBox("是否启用");

        TextField descField = new TextField();
        descField.getStyleClass().add("input-field");

        if (unit != null) {
            codeField.setText(unit.getCode() != null ? unit.getCode() : "");
            nameField.setText(unit.getName());
            mainUnitCheckBox.setSelected(unit.getMainUnit());
            enabledCheckBox.setSelected(unit.getEnabled());
            descField.setText(unit.getDescription() != null ? unit.getDescription() : "");
        } else {
            enabledCheckBox.setSelected(true);
        }

        grid.add(new Label("单位编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("单位名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("启⽤主单位:"), 0, 2);
        grid.add(mainUnitCheckBox, 1, 2);
        grid.add(new Label("是否启⽤:"), 0, 3);
        grid.add(enabledCheckBox, 1, 3);
        grid.add(new Label("描述:"), 0, 4);
        grid.add(descField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showAlert("提示", "单位名称为必填项");
                    return null;
                }
                Unit result = unit != null ? unit : new Unit();
                result.setCode(codeField.getText().isBlank() ? null : codeField.getText());
                result.setName(nameField.getText());
                result.setMainUnit(mainUnitCheckBox.isSelected());
                result.setEnabled(enabledCheckBox.isSelected());
                result.setDescription(descField.getText().isBlank() ? null : descField.getText());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }
}
