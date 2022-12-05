# Artifact Verifier
(project for OMSCS Practicum @ Georgia Insitute of Technology)

This application implements an artifact verifier that has a few key components:
- A verification mechanism that checks:
  - The validity of the signing certificate (must be signed by the Fulcio Intermediate)
  - The validity of the SAN / OIDC Provider as specified by configuration
  - The validity of the signature against an artifact
- A webhook endpoint for the Sonatype Nexus 3 repository manager to listen for asset creation events
- A notifier service that initially emits validation errors to Discord

## Getting Started

Generate an executable jar, create the docker image and then run it
```shell
$ ./gradlew bootJar
$ docker build . -t artifact-verifier:0.0.1
$ docker run -p 8080:8080 --rm -t artifact-verifier:0.0.1
```

### Testing The Application

Until the webhook endpoints are wired up to the verifier, the endpoints can be tested with the following commands

```shell
$ curl -XPOST -H "X-Nexus-Webhook-Id: rm:repository:component" -H "Content-Type: application/json" localhost:8080/nexuswebhook -vvv -d '{                          ok  at 00:52:27 
   "timestamp":"2016-11-14T19:32:13.515+0000",  
   "nodeId":"7FFA7361-6ED33978-36997BD4-47095CC4-331356BE",  
   "initiator":"anonymous/127.0.0.1",
   "repositoryName":"npm-proxy",  
   "action":"CREATED",  
   "component":{
      "id":"08909bf0c86cf6c9600aade89e1c5e25", 
      "componentId":"bnBtLXByb3h5OjA4OTA5YmYwYzg2Y2Y2Yzk2MDBhYWRlODllMWM1ZTI1",
      "format":"npm", 
      "name":"angular2",
      "group":"types",
      "version":"0.0.2"
   }
}'

$ curl -XPOST -H "X-Nexus-Webhook-Id: rm:repository:asset" -H "Content-Type: application/json" localhost:8080/nexuswebhook -vvv -d '{                              ok  at 00:50:53 
   "timestamp" : "2016-11-10T23:57:49.664+0000",
   "nodeId" : "52905B51-085CCABB-CEBBEAAD-16795588-FC927D93",
   "initiator" : "admin/127.0.0.1",
   "repositoryName" : "npm-proxy",
   "action" : "CREATED",
   "asset" : {
     "id" : "31c950c8eeeab78336308177ae9c441c",
     "assetId" : "bnBtLXByb3h5OjMxYzk1MGM4ZWVlYWI3ODMzNjMwODE3N2FlOWM0NDFj",
     "format" : "npm",
     "name" : "concrete"
  }
}'

# A fallback mapping exists to handle events we don't care about
$ curl -XPOST -H "X-Nexus-Webhook-Id: rm:repository:whatever" -H "Content-Type: application/json" localhost:8080/nexuswebhook -vvv -d '{                           ok  at 00:51:31 
  "nodeId":"7FFA7361-6ED33978-36997BD4-47095CC4-331356BE",
  "initiator":"admin/127.0.0.1",
  "audit":{
     "domain":"security.user",
     "type":"created",
     "context":"testuser",
     "attributes":{
        "id":"testuser",
        "name":"test user",
        "email":"test@test.com",
        "source":"default",
        "status":"active",
        "roles":"nx-admin, nx-anonymous"
      }
   }
}'
```

## Evaluation

### Start Docker Compose

`NEXUS_PASSWORD` and `SLACK_WEBHOOK_URL` are set through [variable substitution](https://docs.docker.com/compose/compose-file/compose-file-v3/#variable-substitution).
Create and modify a `.env` file to set those properties (former is required; latter is optional).

Bring the testing rig up using the command `docker compose up`.

The following services will be online:
- [Artifact Verifier](http://localhost:8080)
- [Nexus Repository 3](http://localhost:8081)
- [Grafana](http://localhost:3000)

If this is the first time running, see the notes below about setting up nexus3's data volume.  You'll need the 
[backup.tar.gz file](https://github.com/jidemoto/artifact-verifier/releases/download/v0.0.1/backup.tar.gz) containing 
the volume data to perform the restore operation.

### Evaluating Behavior

See the [dependency-confusion-poc repo](https://github.com/jidemoto/dependency-confusion-poc) for more information on 
how to trigger the Proof-of-Concept dependency confusion attack.  Verifier should pick up the problematic dependency if 
everything is configured properly.

### Running the evaluation script

Set the user credentials on the environment through the following command:
```shell
export credentials=verifier:hunter2
```

Then you can run the eval script from within the `evaluation` folder
```shell
bash upload-test-artifact.sh
```

It will run for 5 minutes and you'll be able to inspect the logs of the verifier.  I recommend not enabling the slack 
notifier while it's running so your integration doesn't get shut down.

## Technical Notes

### Docker backup and restore

The following command can be run to backup the nexus volume.  We'll start up an ubuntu image and use the same volumes as our docker-compose container 
```shell
docker run --rm --volumes-from artifact-verifier_nexus_1 -v $(pwd):/backup ubuntu tar zcvf /backup/backup.tar.gz /nexus-data
```

Then we can restore with the following command after starting a new container:
```shell
docker run --rm --volumes-from artifact-verifier_nexus_1 -v $(pwd):/backup ubuntu bash -c "cd /nexus-data && rm -rf * && tar zxvf /backup/backup.tar.gz --strip 1"
```