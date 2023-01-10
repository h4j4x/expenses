package com.h4j4x.expenses.api.model;

public class UserTransactionDTO {
    private double amount = .0;

    private String notes;

    private TransactionCreationWay creationWay;

    private TransactionStatus status;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TransactionCreationWay getCreationWay() {
        return creationWay;
    }

    public void setCreationWay(TransactionCreationWay creationWay) {
        this.creationWay = creationWay;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
