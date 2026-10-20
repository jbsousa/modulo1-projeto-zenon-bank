package br.com.zenon.fraud;

import br.com.zenon.fraud.TransactionReport.Statistics;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {

    void main(String[] args) {

        String language = (args.length > 0 ? args[0] : "pt");

        Locale locale = Locale.of(language);

        NumberFormat integerFormatter = NumberFormat.getIntegerInstance(locale);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(Currency.getInstance("USD"));

        ResourceBundle resourceBundle = ResourceBundle.getBundle("report", locale);

        var transactionReport = new TransactionReport();
        Statistics statistics= transactionReport.generateReport("data/PS_20174392719_1491204439457_log.csv");

        String fmtTotalTransactions = integerFormatter.format(statistics.totalTransactions());
        String fmtTotalFrauds = integerFormatter.format(statistics.totalFrauds());
        String fmtTotalAmount = currencyFormatter.format(statistics.totalAmount());

        String msgTotalTransactions = resourceBundle.getString("label.total.transactions");
        String msgTotalFrauds = resourceBundle.getString("label.total.frauds");
        String msgTotalAmount = resourceBundle.getString("label.total.amount");



        IO.println(
            """
            %s: %s
            %s: %s
            %s: %s
            """.formatted(
                msgTotalTransactions, fmtTotalTransactions,
                msgTotalFrauds, fmtTotalFrauds,
                msgTotalAmount, fmtTotalAmount
            )
        );


    }

}
