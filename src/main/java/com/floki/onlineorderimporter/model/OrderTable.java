package com.floki.onlineorderimporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.floki.onlineorderimporter.CustomLocalDateDeserializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@IdClass(OrderId.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "order_table")  public class OrderTable {

    @Id
    private UUID uuid;
    @Id
    private Long id;
    private String region;
    private String country;
    private String item_type;
    private String sales_channel;
    private String priority;
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    private LocalDate date;
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    private LocalDate ship_date;
    private Integer units_sold;
    private BigDecimal unit_price;
    private BigDecimal unit_cost;
    private BigDecimal total_revenue;
    private BigDecimal total_cost;
    private BigDecimal total_profit;
//    private String link;

    // Getters and Setters

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public String getSales_channel() {
        return sales_channel;
    }

    public void setSales_channel(String sales_channel) {
        this.sales_channel = sales_channel;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getShip_date() {
        return ship_date;
    }

    public void setShip_date(LocalDate ship_date) {
        this.ship_date = ship_date;
    }

    public Integer getUnits_sold() {
        return units_sold;
    }

    public void setUnits_sold(Integer units_sold) {
        this.units_sold = units_sold;
    }

    public BigDecimal getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(BigDecimal unit_price) {
        this.unit_price = unit_price;
    }

    public BigDecimal getUnit_cost() {
        return unit_cost;
    }

    public void setUnit_cost(BigDecimal unit_cost) {
        this.unit_cost = unit_cost;
    }

    public BigDecimal getTotal_revenue() {
        return total_revenue;
    }

    public void setTotal_revenue(BigDecimal total_revenue) {
        this.total_revenue = total_revenue;
    }

    public BigDecimal getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(BigDecimal total_cost) {
        this.total_cost = total_cost;
    }

    public BigDecimal getTotal_profit() {
        return total_profit;
    }

    public void setTotal_profit(BigDecimal total_profit) {
        this.total_profit = total_profit;
    }

//    public String getLink() {
//        return link;
//    }

//    public void setLink(String link) {
//        this.link = link;
//    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
