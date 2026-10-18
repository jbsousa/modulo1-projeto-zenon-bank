package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FraudAnalyzer {

    private final List<Transaction> transactions;

    public FraudAnalyzer(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactions = transactions;
    }

    /**
     * Conta a quantidade total de transações fraudulentas.
     *
     * @return número de transações fraudulentas
     */
    public long countFrauds() {
        return fraudStream()
                .count();
    }

    /**
     * Retorna as transações fraudulentas com os maiores valores.
     *
     * @param limit quantidade máxima de transações retornadas
     * @return lista de transações fraudulentas ordenadas por valor decrescente
     */
    public List<BigDecimal> topHighestValueFrauds(int limit) {
        return hightValueFraudStream()
                .map(Transaction::amount)
                .limit(limit)
                .toList();
    }

    /**
     * Retorna os nomes dos clientes de origem das fraudes de maior valor,
     * sem duplicidades.
     *
     * @param limit quantidade máxima de clientes retornados
     * @return lista de nomes dos clientes de origem ordenados pelas fraudes de maior valor
     */
    public List<String> findTopSuspiciusClients(int limit) {
        return hightValueFraudStream()
                .map(transaction -> transaction.origin().name())
                .distinct()
                .limit(limit)
                .toList();
    }

    /**
     * Calcula o valor total das perdas associadas às transações fraudulentas.
     *
     * @return soma dos valores das transações fraudulentas
     */
    public BigDecimal calculateTotalFraudsLoss() {
        return fraudStream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Conta as transações fraudulentas agrupadas por tipo.
     *
     * @return mapa contendo cada tipo de transação e sua quantidade de fraudes
     */
    public Map<TransactionType, Long> countFraudsByTypes() {
        return fraudStream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }

    private Stream<Transaction> fraudStream() {
        return transactions
                .stream()
                .filter(Transaction::isFraud);
    }

    private Stream<Transaction> hightValueFraudStream() {
        return fraudStream()
                .sorted(Comparator.comparing(Transaction::amount).reversed());
    }
}
