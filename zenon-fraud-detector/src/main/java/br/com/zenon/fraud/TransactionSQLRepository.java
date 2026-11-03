package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.sql.*;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {

    @Override
    public Optional<Transaction> findByOriginName(String originName) {

        String sql = """
                SELECT id, step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient,
                old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud
                FROM zenon_frauds.transactions
                WHERE name_origin = ?
                ORDER BY step
                LIMIT 1;
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, originName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Transaction transaction = mapResultSetToTransaction(rs);

                    return Optional.of(transaction);

                } else {
                    IO.println("Transacao nao encontrada para origin: " + originName);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar transação da origem: " + originName, e);
        }


        return Optional.empty();
    }

    @Override
    public void save(Transaction transaction) {
        String sql = """
        INSERT INTO zenon_frauds.transactions (
            step, type, amount,
            name_origin, old_balance_origin, new_balance_origin,
            name_recipient, old_balance_recipient, new_balance_recipient,
            is_fraud, is_flagged_fraud
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transaction.step());
            ps.setString(2, transaction.type().name());
            ps.setBigDecimal(3, transaction.amount());
            ps.setString(4, transaction.origin().name());
            ps.setBigDecimal(5, transaction.origin().oldBalance());
            ps.setBigDecimal(6, transaction.origin().newBalance());
            ps.setString(7, transaction.recipient().name());
            ps.setBigDecimal(8, transaction.recipient().oldBalance());
            ps.setBigDecimal(9, transaction.recipient().newBalance());
            ps.setBoolean(10, transaction.isFraud());
            ps.setBoolean(11, transaction.isFlaggedFraud());

            ps.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar nova transação: " + transaction, e);
        }

    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {

        try {

            int step = rs.getInt("step");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String nameOrigin = rs.getString("name_origin");
            BigDecimal oldBalanceOrigin = rs.getBigDecimal("old_balance_origin");
            BigDecimal newBalanceOrigin = rs.getBigDecimal("new_balance_origin");
            Customer customerOrigin = new Customer(nameOrigin, oldBalanceOrigin, newBalanceOrigin);

            String nameRecipient = rs.getString("name_recipient");
            BigDecimal oldBalanceRecipient = rs.getBigDecimal("old_balance_recipient");
            BigDecimal newBalanceRecipient = rs.getBigDecimal("new_balance_recipient");
            Customer customerRecipient = new Customer(nameRecipient, oldBalanceRecipient, newBalanceRecipient);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");


            return new Transaction(
                    step, type, amount, customerOrigin, customerRecipient, isFraud, isFlaggedFraud
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
