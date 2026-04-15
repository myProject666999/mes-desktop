package com.mes.controller;

import com.mes.dto.UnitOfMeasureDTO;
import com.mes.entity.UnitOfMeasure;
import com.mes.service.UnitOfMeasureService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    @FXML
    private TableView<UnitOfMeasureDTO> unitTable;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Boolean> selectColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Boolean> actionColumn;

    @FXML
    private TextField searchUnitCodeField;

    @FXML
    private TextField searchUnitNameField;

    private Set<Long> selectedIds = new HashSet<>();

    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService) {
        this.unitOfMeasureService = unitOfMeasureService;
    }

    @FXML
    public void initialize() {
        loadUnits();
        setupSelectColumn();
        setupActionColumn();
    }

    private void loadUnits() {
        List<UnitOfMeasureDTO> units = unitOfMeasureService.findAll().stream()
                .map(UnitOfMeasureDTO::fromEntity)
                .collect(Collectors.toList());
        unitTable.setItems(FXCollections.observableArrayList(units));
        selectedIds.clear();
    }

    private void setupSelectColumn() {
        selectColumn.setCellValueFactory(cellData -> {
            UnitOfMeasureDTO dto = cellData.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(selectedIds.contains(dto.getId()));
            property.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedIds.add(dto.getId());
                } else {
                    selectedIds.remove(dto.getId());
                }
            });
            return property;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
    }

    private void setupActionColumn() {
        Callback<TableColumn<UnitOfMeasureDTO, Boolean>, TableCell<UnitOfMeasureDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            UnitOfMeasureDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            UnitOfMeasureDTO dto = getTableView().getItems().get(getIndex());
                            deleteSingle(dto);
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

        actionColumn.setCellFactory(cellFactory);
        actionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    @FXML
    public void search() {
        String unitCode = searchUnitCodeField.getText();
        String unitName = searchUnitNameField.getText();

        List<UnitOfMeasureDTO> units = unitOfMeasureService.findByConditions(unitCode, unitName).stream()
                .map(UnitOfMeasureDTO::fromEntity)
                .collect(Collectors.toList());
        unitTable.setItems(FXCollections.observableArrayList(units));
        selectedIds.clear();
    }

    @FXML
    public void resetSearch() {
        searchUnitCodeField.clear();
        searchUnitNameField.clear();
        loadUnits();
    }

    @FXML
    public void showAddDialog() {
        Dialog<UnitOfMeasure> dialog = createUnitDialog("新增计量单位", null);
        dialog.showAndWait().ifPresent(unit -> {
            try {
                unitOfMeasureService.create(unit);
                loadUnits();
                showAlert("成功", "计量单位添加成功");
            } catch (Exception e) {
                showErrorAlert("错误", e.getMessage());
            }
        });
    }

    private void showEditDialog(UnitOfMeasureDTO dto) {
        UnitOfMeasure unit = unitOfMeasureService.findById(dto.getId()).orElse(null);
        if (unit == null) {
            showErrorAlert("错误", "计量单位不存在");
            return;
        }

        Dialog<UnitOfMeasure> dialog = createUnitDialog("修改计量单位", unit);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                unitOfMeasureService.update(updated);
                loadUnits();
                showAlert("成功", "计量单位修改成功");
            } catch (Exception e) {
                showErrorAlert("错误", e.getMessage());
            }
        });
    }

    @FXML
    public void showEditDialogFromSelection() {
        if (selectedIds.isEmpty()) {
            showErrorAlert("提示", "请先选择要修改的数据");
            return;
        }
        if (selectedIds.size() > 1) {
            showErrorAlert("提示", "只能选择一条数据进行修改");
            return;
        }

        Long id = selectedIds.iterator().next();
        UnitOfMeasure unit = unitOfMeasureService.findById(id).orElse(null);
        if (unit == null) {
            showErrorAlert("错误", "计量单位不存在");
            return;
        }

        Dialog<UnitOfMeasure> dialog = createUnitDialog("修改计量单位", unit);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                unitOfMeasureService.update(updated);
                loadUnits();
                showAlert("成功", "计量单位修改成功");
            } catch (Exception e) {
                showErrorAlert("错误", e.getMessage());
            }
        });
    }

    private Dialog<UnitOfMeasure> createUnitDialog(String title, UnitOfMeasure unit) {
        Dialog<UnitOfMeasure> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField unitCodeField = new TextField();
        unitCodeField.getStyleClass().add("input-field");
        unitCodeField.setPromptText("请输入单位编码");

        TextField unitNameField = new TextField();
        unitNameField.getStyleClass().add("input-field");
        unitNameField.setPromptText("请输入单位名称");

        CheckBox isPrimaryCheckBox = new CheckBox("启用主单位");

        CheckBox isEnabledCheckBox = new CheckBox("是否启用");
        isEnabledCheckBox.setSelected(true);

        TextArea descriptionField = new TextArea();
        descriptionField.getStyleClass().add("input-field");
        descriptionField.setPromptText("请输入描述");
        descriptionField.setPrefRowCount(3);

        if (unit != null) {
            unitCodeField.setText(unit.getUnitCode());
            if (unit.getId() != null) {
                unitCodeField.setDisable(true);
            }
            unitNameField.setText(unit.getUnitName());
            isPrimaryCheckBox.setSelected(Boolean.TRUE.equals(unit.getIsPrimary()));
            isEnabledCheckBox.setSelected(Boolean.TRUE.equals(unit.getIsEnabled()));
            descriptionField.setText(unit.getDescription());
        }

        Label unitCodeLabel = new Label("单位编码:");
        unitCodeLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(unitCodeLabel, 0, 0);
        grid.add(unitCodeField, 1, 0);

        Label unitNameLabel = new Label("单位名称:");
        unitNameLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(unitNameLabel, 0, 1);
        grid.add(unitNameField, 1, 1);

        Label isPrimaryLabel = new Label("启用主单位:");
        isPrimaryLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(isPrimaryLabel, 0, 2);
        grid.add(isPrimaryCheckBox, 1, 2);

        Label isEnabledLabel = new Label("是否启用:");
        isEnabledLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(isEnabledLabel, 0, 3);
        grid.add(isEnabledCheckBox, 1, 3);

        Label descriptionLabel = new Label("描述:");
        descriptionLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(descriptionLabel, 0, 4);
        grid.add(descriptionField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (unitCodeField.getText().trim().isEmpty()) {
                    showErrorAlert("错误", "单位编码不能为空");
                    return null;
                }
                if (unitNameField.getText().trim().isEmpty()) {
                    showErrorAlert("错误", "单位名称不能为空");
                    return null;
                }

                UnitOfMeasure result = unit != null ? unit : new UnitOfMeasure();
                result.setUnitCode(unitCodeField.getText().trim());
                result.setUnitName(unitNameField.getText().trim());
                result.setIsPrimary(isPrimaryCheckBox.isSelected());
                result.setIsEnabled(isEnabledCheckBox.isSelected());
                result.setDescription(descriptionField.getText().trim());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void deleteSingle(UnitOfMeasureDTO dto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除计量单位 " + dto.getUnitName() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    unitOfMeasureService.delete(dto.getId());
                    loadUnits();
                    showAlert("成功", "计量单位删除成功");
                } catch (Exception e) {
                    showErrorAlert("错误", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void deleteSelected() {
        if (selectedIds.isEmpty()) {
            showErrorAlert("提示", "请先选择要删除的数据");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除选中的 " + selectedIds.size() + " 条数据?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    unitOfMeasureService.deleteBatch(selectedIds.stream().toList());
                    loadUnits();
                    showAlert("成功", "计量单位删除成功");
                } catch (Exception e) {
                    showErrorAlert("错误", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("计量单位数据.csv");

        File file = fileChooser.showSaveDialog(unitTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write('\ufeff');
                writer.write("ID,单位编码,单位名称,主单位,状态,描述,创建时间,更新时间\n");

                for (UnitOfMeasureDTO dto : unitTable.getItems()) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                            dto.getId(),
                            escapeCsv(dto.getUnitCode()),
                            escapeCsv(dto.getUnitName()),
                            dto.getIsPrimaryText(),
                            dto.getIsEnabledText(),
                            escapeCsv(dto.getDescription()),
                            dto.getCreatedAt() != null ? dto.getCreatedAt() : "",
                            dto.getUpdatedAt() != null ? dto.getUpdatedAt() : ""
                    ));
                }

                showAlert("成功", "数据导出成功");
            } catch (IOException e) {
                showErrorAlert("错误", "导出失败: " + e.getMessage());
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }
}
