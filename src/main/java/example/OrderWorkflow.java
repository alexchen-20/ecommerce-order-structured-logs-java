package example;

import java.time.Instant;
import java.util.Map;

public final class OrderWorkflow {
    public enum Stage { CHECKOUT, FULFILLMENT, RECEIPT, CUSTOMER_UPDATE }

    public record Order(String orderId, String customerId, String sku, int quantity) {}
    public record Event(String orderId, String customerId, Stage stage, String status, Instant at, Map<String, Object> fields) {}

    public Event checkout(Order order) {
        return event(order, Stage.CHECKOUT, "accepted", Map.of("quantity", order.quantity()));
    }

    public Event fulfill(Order order, boolean inStock) {
        String status = inStock ? "packed" : "backordered";
        return event(order, Stage.FULFILLMENT, status, Map.of("inStock", inStock));
    }

    public Event receipt(Order order, String receiptId) {
        return event(order, Stage.RECEIPT, "issued", Map.of("receiptId", receiptId));
    }

    public Event customerUpdate(Order order, String status) {
        return event(order, Stage.CUSTOMER_UPDATE, status, Map.of());
    }

    private Event event(Order o, Stage stage, String status, Map<String, Object> fields) {
        return new Event(o.orderId(), o.customerId(), stage, status, Instant.now(), fields);
    }
}
