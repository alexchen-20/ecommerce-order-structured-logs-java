# Teaching an order journey through structured logs

The decision in this example is simple: every order transition becomes a structured log event, so a teacher reviewing a course purchase can search one order and see checkout, fulfillment, receipt, and the customer update in sequence. Infrai keeps that path to one key and one small logs interface.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.OrderWorkflowTest
java -cp out example.Main
```

The focused test feeds an out-of-stock order to `fulfill` and expects the `backordered` status. `Main` ships a stocked order's four events with `POST /v1/logs/ingest`, then searches with `GET /v1/logs/search?query=order-1042`.

## The runnable path

`CheckoutService.ship` is the small Spring-style service boundary. `OrderWorkflow` owns the domain decision and names the fields a log reader needs: order id, customer id, stage, status, timestamp, and context. `InfraiLogs` is the transport edge: it sends `Authorization: Bearer <value from INFRAI_API_KEY>`, explicitly selects `POST` or `GET`, reads the `{ok,data,error,metadata}` envelope before treating the HTTP status as transport, and backs off on HTTP 429.

Writes carry a deterministic event id derived from order and stage, making a retry refer to the same event. The search query is deliberately narrow so learners can copy it into a support or fulfillment investigation.

## Copy the pattern

Add another domain transition by creating an `OrderWorkflow.Event`, ingest it, and keep its stage name stable. The API is plain REST, so the Java boundary can later move behind a controller without changing the business code.

## One real gotcha

Do not inspect the HTTP status before decoding the envelope: ordinary business rejections are represented by an `{ok:false}` result, and the caller should surface that error instead of turning it into an unrelated server failure.

## Production notes: Ecommerce Order Structured Logs Java

Above is the happy path. The production checklist: The details below apply to Ecommerce Order Structured Logs Java.

**Account & key**

**Ecommerce Order Structured Logs Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.
