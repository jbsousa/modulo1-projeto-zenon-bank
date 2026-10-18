package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.Objects;

public record Customer(String name, BigDecimal oldBalance, BigDecimal newBalance) {

    public Customer {
        Objects.requireNonNull(name);
        Objects.requireNonNull(oldBalance);
        Objects.requireNonNull(newBalance);

        if (name.trim().isEmpty()) throw new IllegalArgumentException("O name não pode ser vazio");
        if (oldBalance.signum() < 0) throw new IllegalArgumentException("O valor de oldBalance deve ser positivo ou zero: " + oldBalance);
        if (newBalance.signum() < 0) throw new IllegalArgumentException("O valor de newBalance deve ser positivo ou zero: " + newBalance);
    }
}
