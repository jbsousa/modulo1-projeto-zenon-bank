package br.com.zenon.fraud;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static br.com.zenon.fraud.TransactionType.*;

public class Main {

    void main() {

        var t1 = new Transaction(1, PAYMENT, new BigDecimal("9839.64"),
                new Customer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new Customer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false, false);


        var t2 = new Transaction(743, CASH_OUT, new BigDecimal("850002.52"),
                new Customer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new Customer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true, false);


        IO.println(t1);
        IO.println(t2);

        IO.println("----------------------------------------------------------------------------------------------------");

        List<Transaction> transactions = new TransactionIngestor().read("data/PS_20174392719_1491204439457_log.csv");

        IO.println(transactions.size());

        transactions.stream()
                .limit(10)
                .forEach(IO::println);

    }
}
