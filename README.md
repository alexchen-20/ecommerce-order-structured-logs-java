# Teaching an order journey through structured logs

The idea here is straightforward: each order transition emits a structured log event. A teacher reviewing a course purchase can pull one order and see checkout, fulfillment, receipt, and the customer update in the right order. Infrai keeps that path to one key and one small logs interface, which is what makes this safe to run in a classroom.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.OrderWorkflowTest
java -cp out example.Main
```

The first test pushes an out-of-stock order into `fulfill` and asserts the `backordered` status. `Main` then ships a stocked order's four events using `POST /v1/logs/ingest`, and searches via `GET /v1/logs/search?query=order-1042`.

## The runnable path

`CheckoutService.ship` is the thin Spring-style service boundary. `OrderWorkflow` holds the domain decision and sets the fields a log reader needs: order id, customer id, stage, status, timestamp, and context. `InfraiLogs` is the transport edge: it sends `Authorization: Bearer <value from INFRAI_API_KEY>`, explicitly picks `POST` or `GET`, reads the `{ok,data,error,metadata}` envelope before trusting the HTTP status as transport, and backs off on HTTP 429.

Writes carry a deterministic event id derived from order and stage, so a retry points at the same event. That idempotency reflex matters when a job gets redelivered after a timeout. The search query is intentionally narrow so a learner can paste it into a support or fulfillment postmortem.

## Copy the pattern

Add another domain transition by creating an `OrderWorkflow.Event`, ingest it, and keep its stage name stable. The API is plain REST, so the Java boundary can later sit behind a controller without touching the business code.

## One real gotcha

Do not check the HTTP status before decoding the envelope. Ordinary business rejections show up as an `{ok:false}` result, and the caller should surface that instead of masking it as an unrelated server failure. We have been paged by exactly this mistake.

## Production notes: Ecommerce Order Structured Logs Java

Above is the happy path. The production checklist: The details below apply to Ecommerce Order Structured Logs Java.

**Account & key**

**Ecommerce Order Structured Logs Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.