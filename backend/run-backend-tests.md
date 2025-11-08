## Filtered tests

```shell
gradle :backend:test 2>&1 | Where-Object { $_ -notmatch "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task" }
```

## Filtered tests including sdout and stderr (and application logs)

```shell
gradle :backend:test 2>&1 -PshowStreams=true | Where-Object { $_ -notmatch "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task" }
```

## Filtered tests for one test class

```shell
gradle :backend:test --tests CDU02IntegrationTest 2>&1 | Where-Object { $_ -notmatch "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task" }
```
