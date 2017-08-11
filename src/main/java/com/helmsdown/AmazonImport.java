package com.helmsdown;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Arguments:
 * [0] = Mint username
 * [1] = Mint password
 * [2] = Absolute path to Amazon Purchase CSV
 *
 * @author rbolles on 8/9/17.
 */
public class AmazonImport {

    public static void main(String[] args) throws Exception {
        Collection<AmazonPurchase> amazonPurchases = parsePurchaseHistoryCSV(args[2]);

        System.out.println("Going to import " + amazonPurchases.size() + " purchases");
        for (AmazonPurchase amazonPurchase : amazonPurchases) {
            System.out.println(amazonPurchase);
        }

        try (Mint mint = new Mint()) {
            mint.login(args[0], args[1]);
            mint.addTransactions(amazonPurchases);
        }
    }

    private static Collection<AmazonPurchase> parsePurchaseHistoryCSV(String csvFilePath) throws IOException {
        List<AmazonPurchase> amazonPurchaseList = new ArrayList<>();

        Reader in = new FileReader(csvFilePath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader("Order Date", "Order ID", "Title", "Category", "ASIN/ISBN", "UNSPSC Code", "Website", "Release Date", "Condition", "Seller", "Seller Credentials", "List Price Per Unit", "Purchase Price Per Unit", "Quantity", "Payment Instrument Type", "Purchase Order Number", "PO Line Number", "Ordering Customer Email", "Shipment Date", "Shipping Address Name", "Shipping Address Street 1", "Shipping Address Street 2", "Shipping Address City", "Shipping Address State", "Shipping Address Zip", "Order Status", "Carrier Name & Tracking Number", "Item Subtotal", "Item Subtotal Tax", "Item Total", "Tax Exemption Applied", "Tax Exemption Type", "Exemption Opt-Out", "Buyer Name", "Currency", "Group Name")
                .withSkipHeaderRecord()
                .parse(in);

        for (CSVRecord record : records) {
            String orderDateStr = record.get("Order Date");
            LocalDate orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ofPattern("M/d/uu"));

            amazonPurchaseList.add(
                    new AmazonPurchase.Builder()
                            .withTitle(record.get("Title").trim())
                            .withDate(orderDate)
                            .withOrderId(record.get("Order ID"))
                            .withItemTotal(record.get("Item Total"))
                            .withBuyerName(record.get("Buyer Name"))
                            .withAsinIsbn(record.get("ASIN/ISBN").trim())
                            .build()
            );

        }

        return amazonPurchaseList;
    }
}
