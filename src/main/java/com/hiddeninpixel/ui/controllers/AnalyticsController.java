package com.hiddeninpixel.ui.controllers;

import com.hiddeninpixel.db.AnalyticsDAO;
import com.hiddeninpixel.model.AnalyticsRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsController {

    @FXML private TableView<AnalyticsRecord> analyticsTable;
    @FXML private TableColumn<AnalyticsRecord, Integer> idColumn;
    @FXML private TableColumn<AnalyticsRecord, String> algorithmColumn;
    @FXML private TableColumn<AnalyticsRecord, String> resolutionColumn;
    @FXML private TableColumn<AnalyticsRecord, Double> sizeColumn;
    @FXML private TableColumn<AnalyticsRecord, Double> ratioColumn;
    @FXML private TableColumn<AnalyticsRecord, String> timestampColumn;

    @FXML private PieChart algorithmUsageChart;
    @FXML private BarChart<String, Number> metricsChart;

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
        resolutionColumn.setCellValueFactory(new PropertyValueFactory<>("resolution"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("payloadSizeKb"));
        ratioColumn.setCellValueFactory(new PropertyValueFactory<>("changeRatio"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        refreshData();
    }

    @FXML
    public void refreshData() {
        List<AnalyticsRecord> records = dao.getAll();
        ObservableList<AnalyticsRecord> data = FXCollections.observableArrayList(records);
        analyticsTable.setItems(data);

        updateCharts(records);
    }

    private void updateCharts(List<AnalyticsRecord> records) {
        // Aggregators for Charts
        Map<String, Integer> usageCount = new HashMap<>();
        Map<String, Double> totalPayload = new HashMap<>();

        for (AnalyticsRecord record : records) {
            String algo = record.getAlgorithmName();
            usageCount.put(algo, usageCount.getOrDefault(algo, 0) + 1);
            totalPayload.put(algo, totalPayload.getOrDefault(algo, 0.0) + record.getPayloadSizeKB());
        }

        // Populate Pie Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : usageCount.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }
        algorithmUsageChart.setData(pieChartData);

        // Populate Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average KB Embedded");
        for (Map.Entry<String, Double> entry : totalPayload.entrySet()) {
            int count = usageCount.get(entry.getKey());
            double average = entry.getValue() / count;
            series.getData().add(new XYChart.Data<>(entry.getKey(), average));
        }
        
        metricsChart.getData().clear();
        metricsChart.getData().add(series);
    }
}
