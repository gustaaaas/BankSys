package com.example.banksys;

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
    private Label resultLabel;

    @FXML
    private Label resultSum;

    @FXML
    private Label interestSum;

    @FXML
    private Spinner<Integer> monthSelector;

    @FXML
    private Label resultRemainingAmount; // Added this label for remaining amount

    @FXML
    private List<Double> monthlyPayments = new ArrayList<>();

    @FXML
    private VBox chartContainer;
    @FXML
    public void initialize() {
        termSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 30, 10));
        loanTypeChoice.setItems(FXCollections.observableArrayList("Anuitetas", "Linijinis"));
        loanTypeChoice.setValue("Anuitetas");
        monthSelector.setValueFactory(new IntegerSpinnerValueFactory(1, 1, 1));
    }

    @FXML
    protected void calculateLoan() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            double interestRate = Double.parseDouble(interestField.getText());
            int term = termSpinner.getValue();

            if (yearsRadio.isSelected()) {
                term *= 12;
            }
            String loanType = loanTypeChoice.getValue();
            double monthlyPayment;
            double paymentSum = 0;

            if (loanType.equals("Anuitetas")) {
                monthlyPayments.clear();
                monthlyPayment = calculateAnnuityLoan(amount, term, interestRate);

                //Reikia masyvo grafikui braizyt
                for (int i = 0; i < term; i++) {
                    monthlyPayments.add(monthlyPayment);
                }
                resultLabel.setText("Mėnesio įmoka: " + String.format("%.2f €", monthlyPayment));
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
                resultLabel.setText(paymentInfo.toString());
                resultSum.setText("Bendra grąžinimo suma: " + String.format("%.2f", paymentSum));
                showChart(monthlyPayments);
            }

            // Update month selector range
            monthSelector.setValueFactory(new IntegerSpinnerValueFactory(1, term, 1));

        } catch (NumberFormatException e) {
            resultLabel.setText("Įveskite tinkamus skaičius!");
        }
    }

    @FXML
    private void onShowSelectedMonth() {
        try {
            int selectedMonth = monthSelector.getValue();
            int totalMonths = termSpinner.getValue() * (yearsRadio.isSelected() ? 12 : 1);
            double amount = Double.parseDouble(amountField.getText());
            double interestRate = Double.parseDouble(interestField.getText());

            if (loanTypeChoice.getValue().equals("Anuitetas")) {
                double monthlyPayment = calculateAnnuityLoan(amount, totalMonths, interestRate);
                resultLabel.setText(selectedMonth + " mėnesio įmoka: " + String.format("%.2f", monthlyPayment) + " €");
            } else {
                double payment = monthlyPayments.get(selectedMonth - 1);
                resultLabel.setText(selectedMonth + " mėnesio įmoka: " + String.format("%.2f", payment) + " €");
            }

            // Calculate and display remaining amount to pay
            double remainingAmount = calculateRemainingAmountToPay(amount, totalMonths, selectedMonth, interestRate);
            resultRemainingAmount.setText("Likusi suma: " + String.format("%.2f", remainingAmount) + " €");
        } catch (Exception e) {
            resultLabel.setText("Klaida skaičiuojant!");
        }
    }

    private double calculateRemainingAmountToPay(double amount, int totalMonths, int currentMonth, double interestRate) {
        if (loanTypeChoice.getValue().equals("Anuitetas")) {
            return calculateRemainingAnnuityAmount(amount, totalMonths, currentMonth, interestRate);
        } else {
            return calculateRemainingLinearAmount(amount, totalMonths, currentMonth, interestRate);
        }
    }

    private double calculateRemainingLinearAmount(double amount, int totalMonths, int currentMonth, double interestRate) {
        double principalPart = amount / totalMonths;
        return Math.max(0, amount - (currentMonth * principalPart));
    }

    private double calculateRemainingAnnuityAmount(double amount, int totalMonths, int currentMonth, double interestRate) {
        double monthlyRate = interestRate / 100 / 12;
        double monthlyPayment = calculateAnnuityLoan(amount, totalMonths, interestRate);

        // Calculate remaining principal using future value formula
        double remainingAmount = amount * Math.pow(1 + monthlyRate, totalMonths)
                - (monthlyPayment * ((Math.pow(1 + monthlyRate, totalMonths) - Math.pow(1 + monthlyRate, currentMonth)) / monthlyRate));

        return Math.max(0, remainingAmount);
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
        // Clear previous charts if any
        chartContainer.getChildren().clear();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Mėnuo");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Įmoka (€)");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Mėnesinės įmokos");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Įmokų grafikas");

        for (int i = 0; i < payments.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, payments.get(i)));
        }


        chart.getData().add(series);
        chart.setPrefWidth(300);
        chart.setLegendVisible(false);

        chartContainer.getChildren().add(chart);
    }

}