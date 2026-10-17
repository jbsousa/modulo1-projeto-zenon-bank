package br.com.zenon.fraud;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionIngestor {

    public List<Transaction> read(String filename) {

        try {
            List<String> lines = Files.readAllLines(Path.of(filename));

            return lines.stream()
                .skip(1)
                .limit(1000)
                .map(this::parseTransaction)
                .toList();

        }
        catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename);
        }
    }

    private Transaction parseTransaction(String line) {
        String[] chunks = line.split(",");
        int step = Integer.parseInt(chunks[0]);
        TransactionType transactionType = TransactionType.valueOf(chunks[1]);
        BigDecimal amount = new BigDecimal(chunks[2]);
        Customer origin = new Customer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
        Customer recipient = new Customer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));
        boolean isFraud = "1".equals(chunks[9]);
        boolean isFlaggedFraud = "1".equals(chunks[10]);

        return new Transaction(step, transactionType, amount, origin, recipient, isFraud, isFlaggedFraud);
    }

    /**
     * Método antigo, mantido apenas para referência.
     *
     * @deprecated Utilize {@link #read(String)} como substituto.
     */
    public List<Transaction> readOld(String filename) {

        List<Transaction> transactions = new ArrayList<>();

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
