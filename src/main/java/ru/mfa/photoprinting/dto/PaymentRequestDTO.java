package ru.mfa.photoprinting.dto;

public class PaymentRequestDTO {
    private Double amount;
    private String paymentMethod;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}