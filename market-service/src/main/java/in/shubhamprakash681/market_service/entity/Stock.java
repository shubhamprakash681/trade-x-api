package in.shubhamprakash681.market_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks_table")
public class Stock {
    @Id
    @Column(nullable = false, unique = true, length = 32)
    private String symbol;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 32)
    private String exchange;

    @Column(nullable = false, length = 80)
    private String sector;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal referencePrice;

    @Column(nullable = false)
    private boolean synthetic;

    protected Stock() {
    }

    public Stock(String symbol, String name, String exchange, String sector, BigDecimal referencePrice, boolean synthetic) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.sector = sector;
        this.referencePrice = referencePrice;
        this.synthetic = synthetic;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
}
