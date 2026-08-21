package example;

public final class CheckoutService {
    private final OrderWorkflow workflow = new OrderWorkflow();
    private final InfraiLogs logs;
    public CheckoutService(InfraiLogs logs) { this.logs = logs; }
    public void ship(OrderWorkflow.Order order, boolean inStock) throws Exception {
        logs.ingest(workflow.checkout(order));
        logs.ingest(workflow.fulfill(order, inStock));
        logs.ingest(workflow.receipt(order, "rcpt-" + order.orderId()));
        logs.ingest(workflow.customerUpdate(order, inStock ? "ready" : "waiting"));
    }
}
