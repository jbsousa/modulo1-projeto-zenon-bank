package br.com.zenon.fraud;

import java.util.List;

public class DBMain {

    void main() {

        ConnectionFactory.getConnection();
        IO.println("Conexão com o BD criada :)");

        var repository = new TransactionSQLRepository();
        var transactionIngestor = new TransactionIngestor();

        long startTimeSQL = System.nanoTime();

        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());
        IO.println("Iniciando adição das transações no BD....");

        transactions.forEach(repository::save);

        long endTimeSQL = System.nanoTime();

        IO.println("Tempo de busca - List (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);

        repository.findByOriginName("C1231006815")
            .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para: C1231006815"));

    }

}
