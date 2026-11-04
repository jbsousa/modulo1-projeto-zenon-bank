package br.com.zenon.fraud;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {


//    public static final int FRAUD_LIMIT = 10_000;
    public static final int LINE_BATCH_SIZE = 2_500;

    public void readAsBatch(String filename, Consumer<List<Transaction>> batchConsumer) {
        Path path = Path.of(filename);
        try (ExecutorService executor = Executors.newFixedThreadPool(10);
             Stream<String> lines = Files.lines(path).skip(1)) {

            var interator = lines.iterator();

            List<String> linesBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while (interator.hasNext()) {

                String line = interator.next();
                linesBatch.add(line);

                if (linesBatch.size() >= LINE_BATCH_SIZE) {
                    IO.println("Executando batch ingestor...");
                    final List<String> currentLineBatch = List.copyOf(linesBatch);
                    executor.submit(() -> executeBatch(currentLineBatch, batchConsumer));
                    linesBatch.clear();
                }
            }

            if (!linesBatch.isEmpty()) {
                IO.println("Executando batch final ingestor...");
                final List<String> currentLineBatch = List.copyOf(linesBatch);
                executor.submit(() -> executeBatch(currentLineBatch, batchConsumer));
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, e);
        }
    }

    public void readAsStream(String filename, Consumer<Transaction> consumer) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {

            lines
                    .skip(1)
//                    .limit(FRAUD_LIMIT)
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(consumer);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, e);
        }
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",");
            int step = Integer.parseInt(chunks[0]);
            TransactionType transactionType = TransactionType.valueOf(chunks[1]);

            if (chunks[2] == null || chunks[2].isEmpty())
                throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            if (chunks[4] == null || chunks[4].isEmpty())
                throw new IllegalArgumentException("O valor de oldbalanceOrg não pode ser nulo nem vazio");

            if (chunks[5] == null || chunks[5].isEmpty())
                throw new IllegalArgumentException("O valor de newbalanceOrig não pode ser nulo nem vazio");
            Customer origin = new Customer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));

            if (chunks[7] == null || chunks[7].isEmpty())
                throw new IllegalArgumentException("O valor de oldbalanceDest não pode ser nulo nem vazio");

            if (chunks[8] == null || chunks[8].isEmpty())
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

                if (lineCount == 1)
                    continue;

                if (transactions.size() >= 1000)
                    break;

                transactions.add(parseTransaction(line));

            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo " + filename);
        }

        return transactions;
    }

    public void executeBatch(List<String> linebatch, Consumer<List<Transaction>> batchConsumer) {
        List<Transaction> transactionsBatch = linebatch
                .stream()
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        batchConsumer.accept(transactionsBatch);
    }
}

