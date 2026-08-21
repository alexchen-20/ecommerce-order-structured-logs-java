package example;

public final class OrderWorkflowTest {
    public static void main(String[] args) {
        OrderWorkflow w = new OrderWorkflow();
        var e = w.fulfill(new OrderWorkflow.Order("o1", "c1", "sku", 1), false);
        if (e.stage() != OrderWorkflow.Stage.FULFILLMENT || !e.status().equals("backordered")) throw new AssertionError(e);
        System.out.println("OrderWorkflowTest passed");
    }
}
