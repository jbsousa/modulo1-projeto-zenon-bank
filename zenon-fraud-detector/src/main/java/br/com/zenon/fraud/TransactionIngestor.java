package br.com.zenon.fraud;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TransactionIngestor {

    public static final int FRAUD_LIMIT = 100_000;

    public List<Transaction> read(String filename) {

        try {
            List<String> lines = Files.readAllLines(Path.of(filename));

            return lines.stream()
                .skip(1)
                .limit(FRAUD_LIMIT)
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        }
        catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, e);
        }
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",");
            int step = Integer.parseInt(chunks[0]);
            TransactionType transactionType = TransactionType.valueOf(chunks[1]);

            if(chunks[2] == null || chunks[2].isEmpty())
                throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            if(chunks[4] == null || chunks[4].isEmpty())
                throw new IllegalArgumentException("O valor de oldbalanceOrg não pode ser nulo nem vazio");

            if(chunks[5] == null || chunks[5].isEmpty())
                throw new IllegalArgumentException("O valor de newbalanceOrig não pode ser nulo nem vazio");
            Customer origin = new Customer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));

            if(chunks[7] == null || chunks[7].isEmpty())
                throw new IllegalArgumentException("O valor de oldbalanceDest não pode ser nulo nem vazio");

            if(chunks[8] == null || chunks[8].isEmpty())
                throw new IllegalArgumentException("O valor de newbalanceDest não pode ser nulo nem vazio");

            Customer recipient = new Customer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));

            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);

            return Optional.of(new Transaction(step, transactionType, amount, origin, recipient, isFraud, isFlaggedFraud));

        } catch (Exception e) {
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
        }

        return Optional.empty();
    }

    /**
     * Método antigo, mantido apenas para referência.
     *
     * @deprecated Utilize {@link #read(String)} como substituto.
     */
    public List<Optional<Transaction>> readOld(String filename) {

        List<Optional<Transaction>> transactions = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if(lineCount == 1)
                    continue;

                if(transactions.size() >= 1000)
                    break;

                transactions.add(parseTransaction(line));

            }
        }
        catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename);
        }

        return transactions;
    }

}
