# FACETz DMP segments uploader to myTarget
FACETz DMP publishes files with user segments to its SFTP server. 

This module knows how to create Audiences at your myTarget account using these files.

For each FACETz segment_id will be created Audience in myTarget with same id (ex. all users in FACETz segment with id 123 will be uploaded to myTarget Audience with name facetz_auditory_123)

## Requirements
JDK 7+ (http://www.oracle.com/technetwork/java/javase/downloads/index.html)

sbt 0.13.8 (http://www.scala-sbt.org/download.html)

## Launch

### Making executable jar
```bash
sbt clean assembly
```

### Show help message
```bash
java -jar target/scala-2.11/mailru-segment-exporter_2.11-<version>.jar --help
```

### Available options
```
   --help
           prints this usage text
     -i <workdir> | --workdir <workdir>
           Directory with files to process. ex. /opt/segments
     -p <partner> | --partner <partner>
           mailru partner prefix, will be first line of each processed file
     -o <processedfolder> | --processedfolder <processedfolder>
           folder name with parsing results. 'results' by default
     -d <date> | --date <date>
           suffix of segment file name. It will be used for auditory update in future. ex. 20151231, now by default
     -r <regexp> | --regexp <regexp>
           source filename pattern in workdir, default .*(.gz)$
     -w | --process
           process segments from files. false by default
     -g <allowedsegments> | --allowedsegments <allowedsegments>
           comma-separated allowed segment ids for upload . empty = all. empty by default
     -u | --upload
           upload segments to mailru or not. false by default
     -y | --auditoryupdate
           update auditories in mailru or not. false by default
     -a | --apiurl
           mailru api url, https://target.my.com by default
     -c <client> | --client <client>
           your mailru client_id
     -s <secret> | --secret <secret>
           your mailru client_secret
     -m <minion> | --minion <minion>
           subaccount name for agencies

```

### Full params for export and upload
```bash
java -jar target/scala-2.11/mailru-segment-exporter_2.11-<version>.jar --process --workdir /tmp/gz --processedfolder export-results --date 20150330 --regexp ".*(.gz)$" --partner pr --upload --client someclient --secret somesecret
```

### Full params for auditory update
```bash
java -jar target/scala-2.11/mailru-segment-exporter_2.11-<version>.jar --auditoryupdate --client someclient --secret somesecret --minion 6minion048@agency_client
```


### Simple process and upload with defaults
```bash
java -jar target/scala-2.11/mailru-segment-exporter_2.11-<version>.jar -w -i /tmp/gz -p dl -u -c someclient -s somesecret
```

## Publishing jar to FACETz Nexus repository
Create file with Nexus credentials ~/.sbt/.credentials

```
realm=Sonatype Nexus Repository Manager
host=repo
user=username
password=password
```

And run command

```bash
sbt clean publish
```