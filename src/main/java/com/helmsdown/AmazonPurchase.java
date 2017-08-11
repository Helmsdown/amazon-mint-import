package com.helmsdown;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author rbolles on 8/10/17.
 */
public class AmazonPurchase {

    private final LocalDate date;
    private final String itemTotal;
    private final String orderId;
    private final String title;
    private final String buyerName;
    private final String asinIsbn;


    private AmazonPurchase(LocalDate date, String itemTotal, String orderId, String title, String buyerName, String asinIsbn) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(itemTotal, "itemTotal");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(buyerName, "buyerName");
        Objects.requireNonNull(asinIsbn, "asinIsbn");
        this.date = date;
        this.itemTotal = itemTotal.replace("$", "");
        this.orderId = orderId;
        this.title = title.trim().replace("  ", "");
        this.buyerName = buyerName;
        this.asinIsbn = asinIsbn.trim();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getItemTotal() {
        return itemTotal;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTitle() {
        return title;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getAsinIsbn() {
        return asinIsbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmazonPurchase that = (AmazonPurchase) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(itemTotal, that.itemTotal) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(buyerName, that.buyerName) &&
                Objects.equals(asinIsbn, that.asinIsbn)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, itemTotal, orderId, title, buyerName, asinIsbn);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("itemTotal", itemTotal)
                .append("orderId", orderId)
                .append("title", title)
                .append("buyerName", buyerName)
                .append("asinIsbn", asinIsbn)
                .toString();
    }

    public String getFormattedUniqueIdentifier() {
        return orderId + "/" + asinIsbn;
    }


    public static final class Builder {
        private LocalDate date;
        private String itemTotal;
        private String orderId;
        private String title;
        private String buyerName;
        private String asinIsbn;

        public Builder withDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder withItemTotal(String itemTotal) {
            this.itemTotal = itemTotal;
            return this;
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withBuyerName(String buyerName) {
            this.buyerName = buyerName;
            return this;
        }

        public Builder withAsinIsbn(String asinIsbn) {
            this.asinIsbn = asinIsbn;
            return this;
        }

        public AmazonPurchase build() {
            AmazonPurchase transaction = new AmazonPurchase(date, itemTotal, orderId, title, buyerName, asinIsbn);
            return transaction;
        }
    }
}
