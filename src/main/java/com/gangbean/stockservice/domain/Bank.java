package com.gangbean.stockservice.domain;

import lombok.Builder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long number;

    public Bank(Long id) {
        this.id = id;
    }

    @Builder
    public Bank(String name, Long number) {
        this.name = name;
        this.number = number;
    }

    public Bank(Long id, String name, Long number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    public Bank() {}

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Long number() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bank bank = (Bank) o;
        return Objects.equals(id, bank.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
