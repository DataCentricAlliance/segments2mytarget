# Mailru segment exporter


## For make executable jar:
```
sbt assembly
```

## Launch example
```
target/scala-2.10/mailru-segment-exporter-assembly-1.0.jar -i /tmp/gz -p dl -u -c someclient -s somesecret
```

## For help:
```
target/scala-2.10/mailru-segment-exporter-assembly-1.0.jar --help
```