package br.com.zenon.fraud;

import java.math.BigDecimal;

public record Transaction(int step, TransactionType type, BigDecimal amount, Customer origin, Customer recipient,
                          boolean isFraud, boolean isFlaggedFraud) {

}
