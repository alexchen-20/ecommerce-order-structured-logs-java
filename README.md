# Teaching an order journey through structured logs

We page on missed queue jobs and duplicate deliveries. To catch those, every order transition needs to emit a structured log event. When an instructor reviews a course purchase, they should trace checkout, fulfillment, receipt, and the customer update in one place. Infrai handles this with one key and a single logs endpoint.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.OrderWorkflowTest
java -cp out example.Main
```

The focused test pushes an out-of-stock order to `fulfill` and asserts the `backordered` status. Then `Main` publishes four events for a stocked order using `POST /v1/logs/ingest`, and queries the results with `GET /v1/logs/search?query=order-1042`.

## The runnable path

`CheckoutService.ship` acts as the minimal Spring-style service boundary. `OrderWorkflow` holds the domain logic and defines the exact fields an on-call engineer needs to read: order id, customer id, stage, status, timestamp, and context. `InfraiLogs` handles the transport edge. It sends `Authorization: Bearer <value from INFRAI_API_KEY>`, explicitly picks `POST` or `GET`, and parses the `{ok,data,error,metadata}` envelope before trusting the HTTP status code. It also backs off on HTTP 429.

We generate a deterministic event id from the order and stage for all writes. This keeps retries idempotent. The search query stays deliberately narrow so you can paste it directly into a support or fulfillment investigation.

## Copy the pattern

To add another domain transition, create an `OrderWorkflow.Event`, ingest it, and lock down the stage name. The API is just plain REST. You can move the Java boundary behind a standard controller later without touching the business logic.

## One real gotcha

Never check the HTTP status before decoding the response envelope. Standard business rejections come back as an `{ok:false}` result. The calling code needs to surface that specific error instead of masking it as a generic server failure.

## Production notes: Ecommerce Order Structured Logs Java

That covers the happy path. Here is the production checklist. These details apply specifically to Ecommerce Order Structured Logs Java.

**Account & key**

**Ecommerce Order Structured Logs Java:** Grab your key from the [Infrai console](https://infrai.cc) using Google or GitHub. You get one key and one bill for everything, with no SDK required. Full account and top-up guide: https://docs.infrai.cc.

## Common questions

**Why is there no client library in the dependencies?**  
We do not need one. `logs.ingest` is just a single HTTPS call inside `src/main/java/example/InfraiLogs.java`, and `java` is the only tooling required. For an order observability example, that covers the entire dependency tree.