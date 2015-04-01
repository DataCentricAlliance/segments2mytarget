# Mailru segment exporter

## Requirements
- sbt 0.13.7

## For make executable jar:
```bash
sbt assembly
```

## Launch
### Full params
```bash
java -jar target/scala-2.11/mailru-segment-exporter-assembly-1.0.jar --workdir /tmp/gz --outputname export-results --date 20150330 --regexp ".*(.gz)$" --partner pr --upload --client someclient --secret somesecret
```

### Simple with defaults
```bash
java -jar target/scala-2.11/mailru-segment-exporter-assembly-1.0.jar -i /tmp/gz -p dl -u -c someclient -s somesecret
```

## Available options
```
      --help
            prints this usage text
      -i <workdir> | --workdir <workdir>
            Directory with files to process. ex. /opt/segments
      -p <partner> | --partner <partner>
            mailru partner prefix, will be first line of each processed file
      -o <outputname> | --outputname <outputname>
            output folder name with parsing results. 'results' by default
      -d <date> | --date <date>
            suffix of segment file name. It will be used for auditory update in future. ex. 20151231, now by default
      -r <regexp> | --regexp <regexp>
            source filename pattern in workdir, default .*(.gz)$
      -u | --upload
            upload segments to mailru or not. false by default
      -c <client> | --client <client>
            your mailru client_id
      -s <secret> | --secret <secret>
            your mailru client_secret
```

## Show help message:
```bash
java -jar target/scala-2.11/mailru-segment-exporter-assembly-1.0.jar --help
```