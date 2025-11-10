## Filtered tests

```bash
gradle :backend:test 2>&1 | grep -vE "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task"
```

## Filtered tests including sdout and stderr (and application logs)

```bash
gradle :backend:test 2>&1 -PshowStreams=true | grep -vE "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task"
```

## Filtered tests for one test class

```bash
gradle :backend:test --tests CDU02IntegrationTest 2>&1 | grep -vE "at java\.base|junit|org\.springframework\.test|gradle|hamcrest|> Task"
```

