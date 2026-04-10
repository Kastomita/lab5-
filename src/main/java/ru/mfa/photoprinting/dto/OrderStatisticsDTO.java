package ru.mfa.photoprinting.dto;

public class OrderStatisticsDTO {
    private Long totalOrders;
    private Double totalRevenue;
    private Long paidOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    // Getters and Setters
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getPaidOrders() { return paidOrders; }
    public void setPaidOrders(Long paidOrders) { this.paidOrders = paidOrders; }

    public Long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }

    public Long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(Long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
}