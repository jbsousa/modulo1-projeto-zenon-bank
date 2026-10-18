package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        IO.println("----------------------------------------------------------------------------------------------------");

        List<Transaction> transactionsBadData = new TransactionIngestor().read("data/paysim_with_bad_data.csv");

        IO.println(transactionsBadData.size());

        transactionsBadData.forEach(IO::println);

        IO.println("----------------------------------------------------------------------------------------------------");

        FraudAnalyzer fraudAnalyzer = new FraudAnalyzer(transactions);

        IO.println(transactions.size());
        IO.println("1. Total de Fraudes: " + fraudAnalyzer.countFrauds());

        IO.println("2. Top 3 Fraudes de Maior Valor: ");
        fraudAnalyzer.topHighestValueFrauds(3).stream()
                .map(BigDecimal::toPlainString)
                .forEach(IO::println);

        IO.println("3. Clientes Suspeitos:");
        fraudAnalyzer.findTopSuspiciusClients(5).forEach(IO::println);

        IO.println("4. Prejuízo Total: " + fraudAnalyzer.calculateTotalFraudsLoss());

        IO.println("5. Fraudes por Tipo:");
        fraudAnalyzer.countFraudsByTypes().forEach((type, count) -> IO.println(" - %s: %d".formatted(type, count)));

        IO.println("----------------------------------------------------------------------------------------------------");

        TransactionRepository transactionRepository = new TransactionListRepository(transactions);

        String notFoundOriginName = "C12345";
        String existsOriginName = "C1868032458";

        transactionRepository.findByOriginName(notFoundOriginName)
                .ifPresentOrElse(IO::println, () -> IO.println("Transacão não encontrada para " + notFoundOriginName));

        long startTimeList = System.nanoTime();
        transactionRepository.findByOriginName(existsOriginName)
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para " + existsOriginName));
        long endTimeList = System.nanoTime();
        IO.println("Tempo de busca - List (ms): " + (endTimeList - startTimeList) / 1_000_000.0);

        transactionRepository = new TransactionMapRepository(transactions);

        long startTimeMap = System.nanoTime();
        transactionRepository.findByOriginName(existsOriginName)
                .ifPresentOrElse(IO::println, () -> IO.println("Transacão não encontrada para " + existsOriginName));

        long endTimeMap = System.nanoTime();

        IO.println("Tempo de busca - Map (ms): " + (endTimeMap - startTimeMap) / 1_000_000.0);


    }
}
