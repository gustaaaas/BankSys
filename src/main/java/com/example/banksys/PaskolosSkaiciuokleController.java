package com.example.banksys;
import com.example.banksys.util.PdfExporter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.collections.ObservableList;

public class PaskolosSkaiciuokleController {
    @FXML
    private TextField amountField;

    @FXML
    private TextField interestField;

    @FXML
    private Spinner<Integer> termSpinner;

    @FXML
    private RadioButton yearsRadio;

    @FXML
    private ChoiceBox<String> loanTypeChoice;

    @FXML
    private Label resultSum;

    @FXML
    private Label interestSum;

    @FXML
    private Spinner<Integer> monthSelector;

    @FXML
    private Label resultRemainingAmount;

    @FXML
    private final List<Double> monthlyPayments = new ArrayList<>();
    @FXML
    private TableView<PaymentEntry> paymentTable;
    @FXML
    private Spinner<Integer> delayStartMonth;
    @FXML
    private TableColumn<PaymentEntry, Integer> monthColumn;
    @FXML
    private Label selectedMonthShow;
    @FXML
    private TableColumn<PaymentEntry, String> paymentColumn;
    @FXML
    private VBox chartContainer;
    @FXML
    private void onExportToPDF() {
        PdfExporter.exportToPDF(monthlyPayments, "paskolos_rezultatai.pdf");
    }
    @FXML
    public void initialize() {
        termSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 30, 10));
        loanTypeChoice.setItems(FXCollections.observableArrayList("Anuitetas", "Linijinis"));
        loanTypeChoice.setValue("Anuitetas");
        monthSelector.setValueFactory(new IntegerSpinnerValueFactory(1, 1, 1));
        delayStartMonth.setValueFactory(new IntegerSpinnerValueFactory(0, 12, 0));
        monthColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getMonth()));
        paymentColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPayment()));
    }
    @FXML
    protected void calculateLoan() {
            double amount = Double.parseDouble(amountField.getText());
            double interestRate = Double.parseDouble(interestField.getText());
            int term = termSpinner.getValue();
            int delay = delayStartMonth.getValue();
            if (yearsRadio.isSelected()) {
                term *= 12;
            }
            String loanType = loanTypeChoice.getValue();
            double monthlyPayment;
            double paymentSum = 0;

            if (loanType.equals("Anuitetas")) {
                monthlyPayments.clear();
                monthlyPayment = calculateAnnuityLoan(amount, term, interestRate);

                for (int i = 0; i < delay; i++) {
                    monthlyPayments.add(0.00);
                }
                for (int i = 0; i < term; i++) {
                    monthlyPayments.add(monthlyPayment);
                }
                resultSum.setText("Bendra grąžinimo suma: " + String.format("%.2f €", monthlyPayment * term));
                interestSum.setText("Bendra palūkanų suma: " + String.format("%.2f €", monthlyPayment * term - amount));
                showChart(monthlyPayments);
            } else {
                double[] monthlyPaymentsArray = calculateLinearLoan(amount, term, interestRate);
                monthlyPayments.clear();
                for (double p : monthlyPaymentsArray) {
                    monthlyPayments.add(p);
                }

                StringBuilder paymentInfo = new StringBuilder();
                for (int month = 0; month < term; month++) {
                    paymentInfo.append((month + 1) + " Mėnesio įmoka: " + String.format("%.2f €\n", monthlyPayments.get(month)));
                    paymentSum += monthlyPayments.get(month);
                }

                double interestPaid = paymentSum - amount;
                interestSum.setText("Palūkanų suma: " + String.format("%.2f", interestPaid));
                resultSum.setText("Bendra grąžinimo suma: " + String.format("%.2f", paymentSum));
                showChart(monthlyPayments);
            }

            monthSelector.setValueFactory(new IntegerSpinnerValueFactory(1, term, 1));
            ObservableList<PaymentEntry> entries = FXCollections.observableArrayList();
            for (int i = 0; i < monthlyPayments.size(); i++) {
                entries.add(new PaymentEntry(i + 1, String.format("%.2f €", monthlyPayments.get(i))));
            }
            paymentTable.setItems(entries);
    }

    @FXML
    private void onShowSelectedMonth() {
            int selectedMonth = monthSelector.getValue();
            int totalMonths = termSpinner.getValue() * (yearsRadio.isSelected() ? 12 : 1);
            double amount = Double.parseDouble(amountField.getText());
            double interestRate = Double.parseDouble(interestField.getText());

            if (loanTypeChoice.getValue().equals("Anuitetas")) {
                double monthlyPayment = calculateAnnuityLoan(amount, totalMonths, interestRate);
                selectedMonthShow.setText(String.format("%.2f",monthlyPayment)+ "€ įmoka");
            } else {
                double payment = monthlyPayments.get(selectedMonth - 1);
                selectedMonthShow.setText(String.format("%.2f",payment)+ "€ įmoka");
            }

            double remainingAmount = calculateRemainingAmountToPay(amount, totalMonths, selectedMonth, interestRate);
            resultRemainingAmount.setText("Likusi suma: " + String.format("%.2f", remainingAmount) + " €");
    }

    private double calculateRemainingAmountToPay(double amount, int totalMonths, int currentMonth, double interestRate) {
        if (loanTypeChoice.getValue().equals("Anuitetas")) {
            return calculateRemainingAnnuityAmount(amount, totalMonths, currentMonth, interestRate);
        } else {
            return calculateRemainingLinearAmount(amount, totalMonths, currentMonth);
        }
    }

    private double calculateRemainingLinearAmount(double amount, int totalMonths, int currentMonth) {
        double principalPart = amount / totalMonths;
        return Math.max(0, amount - (currentMonth * principalPart));
    }

    private double calculateRemainingAnnuityAmount(double amount, int totalMonths, int currentMonth, double interestRate) {
        double monthlyRate = interestRate / 100 / 12;
        double monthlyPayment = calculateAnnuityLoan(amount, totalMonths, interestRate);
        double remainingPrincipal = amount;

        for (int i = 0; i < currentMonth; i++) {
            double interest = remainingPrincipal * monthlyRate;
            double principal = monthlyPayment - interest;
            remainingPrincipal -= principal;
        }

        return Math.max(0, remainingPrincipal);
    }

    private double calculateAnnuityLoan(double amount, int months, double interestRate) {
        double monthlyRate = interestRate / 100 / 12;
        double annuityFactor = (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
        return amount * annuityFactor;
    }

    private double[] calculateLinearLoan(double amount, int months, double interestRate) {
        double[] payments = new double[months];
        double principalPart = amount / months;

        for (int month = 0; month < months; month++) {
            double remainingPrincipal = amount - (month * principalPart);
            double interestPart = remainingPrincipal * (interestRate / 100 / 12);
            payments[month] = principalPart + interestPart;
        }

        return payments;
    }

    public void showChart(List<Double> payments) {
        chartContainer.getChildren().clear();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Mėnuo");

        double min = Collections.min(payments) - 50;
        double max = Collections.max(payments) + 50;
        double range = max - min;
        double tickUnit = Math.max(10, range / 10);

        NumberAxis yAxis = new NumberAxis(min, max, tickUnit);
        yAxis.setLabel("Įmoka (€)");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Mėnesinės įmokos");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Įmokų grafikas");

        for (int i = 0; i < payments.size(); i++) {
            int month = i + 1;
            double payment = payments.get(i);
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(month, payment);
            series.getData().add(dataPoint);
        }
        chart.getData().add(series);
        chart.setPrefWidth(300);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(true);
        chart.setVerticalGridLinesVisible(false);

        for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
            Tooltip tooltip = new Tooltip("Mėnuo: " + dataPoint.getXValue() +
                    "\nĮmoka: " + String.format("%.2f €", dataPoint.getYValue().doubleValue()));
            Tooltip.install(dataPoint.getNode(), tooltip);
        }

        chartContainer.getChildren().add(chart);
    }
    public static class Entry {
        public String getInfo() {
            return "Basic entry";
        }
    }
    public static class PaymentEntry extends Entry {
        private final Integer month;
        private final String payment;

        public PaymentEntry(Integer month, String payment) {
            this.month = month;
            this.payment = payment;
        }

        public Integer getMonth() {
            return this.month;
        }

        public String getPayment() {
            return this.payment;
        }

        @Override
        public String getInfo() {
            return "Mėnuo: " + this.month + ", Įmoka: " + this.payment;
        }
    }

}