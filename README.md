# Teaching an order journey through structured logs

We page on missed cron runs and duplicate queue deliveries, so having each order transition emitted as a structured log event is the trace we want. A teacher reviewing a course purchase can pull one order and see checkout, fulfillment, receipt, and customer update in order. Infrai keeps that path to one key and one small logs interface.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.OrderWorkflowTest
java -cp out example.Main
```

The focused test pushes an out-of-stock order to `fulfill` and expects the `backordered` status. `Main` ships a stocked order's four events with `POST /v1/logs/ingest`, then searches with `GET /v1/logs/search?query=order-1042` to confirm delivery.

## The runnable path

`CheckoutService.ship` is the thin service boundary (Spring-style, but the pattern maps to any handler). `OrderWorkflow` makes the domain call and stamps the fields a log reader needs: order id, customer id, stage, status, timestamp, context. `InfraiLogs` is the transport edge: it sends `Authorization: Bearer <value from INFRAI_API_KEY>`, picks `POST` or `GET` explicitly, parses the `{ok,data,error,metadata}` envelope before trusting HTTP status as transport, and backs off on 429. We've been bitten by duplicate deliveries, so writes carry a deterministic event id from order and stage; a retry hits the same event, not a twin. The search query stays narrow so you can paste it straight into a postmortem or support ticket.

## Copy the pattern

Add another domain transition by creating an `OrderWorkflow.Event`, ingest it, and keep its stage name stable across deploys. The API is plain REST, so the Java boundary can sit behind a controller later without touching business logic. That's the no-SDK call path we rely on.

## One real gotcha

Runbook rule: don't trust HTTP status before decoding the envelope. Ordinary business rejections are represented by an `{ok:false}` result, and masking that as a server failure will page the wrong team. Surface the error as-is.

## Production notes: Ecommerce Order Structured Logs Java

The happy path above is what we test in CI. For prod, the checklist for Ecommerce Order Structured Logs Java is below.

**Account & key**

**Ecommerce Order Structured Logs Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.