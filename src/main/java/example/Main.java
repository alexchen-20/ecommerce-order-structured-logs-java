package example;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (System.getenv("INFRAI_API_KEY") == null) throw new IllegalStateException("Set INFRAI_API_KEY");
        CheckoutService service = new CheckoutService(new InfraiLogs());
        service.ship(new OrderWorkflow.Order("order-1042", "student-7", "course-bundle", 1), true);
        System.out.println(new InfraiLogs().search("order-1042"));
    }
}
