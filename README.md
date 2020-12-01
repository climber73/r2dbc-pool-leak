# Simple project to reproduce possible r2dbc-pool leak

## Description

What's required to reproduce the leak:
1) PostgreSQL (provided as docker container)
2) Transactions with serializable isolation level
3) Relatively heavy load which involve concurrent (with the same account id) DB requests
4) The client should retry 40001 errors (serialization_failure)
5) The client should close HTTP connection by timeout (to get the cancel signal sent to the reactive chains)

## Run test
```shell script
./run.sh
```

Expected result should be as follows:

```shell script
    2020-12-01 13:09:01.951  INFO 81274 --- [Test worker] sample.AppTest  : Started AppTest in 8.229 seconds (JVM running for 9.619)

AppTest > test() STANDARD_OUT
    2020-12-01 13:09:02.416  INFO 81274 --- [Test worker] sample.AppTest  : ---- run preliminary test to estimate required timings ----
    2020-12-01 13:09:13.063  INFO 81274 --- [Test worker] sample.AppTest  : >>> preliminary load test took PT10.645515S
    2020-12-01 13:09:13.063  INFO 81274 --- [Test worker] sample.AppTest  : ---------------------- test itself ------------------------
    2020-12-01 13:09:18.410  INFO 81274 --- [Test worker] sample.AppTest  : ----------------------- shutdown --------------------------
    2020-12-01 13:09:29.059  INFO 81274 --- [Test worker] sample.AppTest  : ------------------------- idle ----------------------------
    2020-12-01 13:09:29.059  INFO 81274 --- [Test worker] sample.AppTest  : ------------------------- idle ----------------------------
    2020-12-01 13:09:29.059  INFO 81274 --- [Test worker] sample.AppTest  : ------------------------- idle ----------------------------

Gradle Test Executor 26 finished executing tests.

> Task :test

AppTest > test() FAILED
    org.opentest4j.AssertionFailedError: expected r2dbc.pool.acquired to be 0.0, but was 8.0 ==> expected: <0.0> but was: <8.0>
        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)
        at org.junit.jupiter.api.AssertionUtils.failNotEqual(AssertionUtils.java:62)
        at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:70)
        at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:908)
        at sample.AppTest.actualTest(AppTest.java:73)
        at sample.AppTest.test(AppTest.java:49)

1 test completed, 1 failed
```

If the test fails than there was some connections, which became acquired forever.
