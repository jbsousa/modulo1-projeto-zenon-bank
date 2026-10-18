package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    private record ReportTransaction(BigDecimal amount, boolean isFraud) {

    }

    public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {

        private final static Statistics ZERO = new Statistics(0, 0, BigDecimal.ZERO);

        private Statistics add(ReportTransaction rt) {
            return new Statistics(
                    totalTransactions + 1,
                    totalFrauds + (rt.isFraud ? 1 : 0),
                    totalAmount.add(rt.amount));
        }

    }

    public Statistics generateReport(String filename) {
        try (Stream<String> lines = Files.lines(Path.of(filename))) {

            return lines
                    .skip(1)
                    .map(this::parseReportTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(
                            Statistics.ZERO,
                            Statistics::add,
                            (s1, s2) -> s1);

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, ex);
        }
    }

    private Optional<ReportTransaction> parseReportTransaction(String line) {
        try {
            String[] chunks = line.split(",");

            if (chunks[2] == null || chunks[2].isEmpty())
                throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            boolean isFraud = "1".equals(chunks[9]);

            return Optional.of(new ReportTransaction(amount, isFraud));

        } catch (Exception e) {
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
        }

        return Optional.empty();
    }


}
