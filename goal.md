Do tasks in any order you prefer.
Get familiar with "minimal valid example.json" file.
I have docker installed on this machine.
Initialize postgresql database.
Create liquebase script for this data "minimal valid example.json", each payment is unique. Use yaml scripts.
Prefer java records over lombock.
Add swagger.
Add Rest CRUD.
Create kafka in docker.
Create kafka topics with java implementation.
spring.kafka.consumer.group-id=future-payment
app.kafka.topics.future-payment.created=future-payment.created
app.kafka.topics.future-payment.modified=future-payment.modified
app.kafka.topics.future-payment.deleted=future-payment.deleted
Write integration tests for kafka: When "future-payment.created" is called, then record in the database is found. Also tests for modified, and deleted.

Create elasticsearch docker.
Add logback logging(opentelemetry: traceid, spanid) into elasticsearch(ECS-compatible JSON format) 
Add tests(unit, integration, ...) using containers.

