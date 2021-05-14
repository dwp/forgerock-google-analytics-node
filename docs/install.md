
# Install the Google Analytics Node

### Local Tomcat

Copy the file `google-analytics-node-6.0.0.jar` 
to the `/apache-tomcat-7.0.105/webapps/openam/WEB-INF/lib/` directory

You can then start Tomcat with

```
export GOOGLE_ANALYTICS_URL="https://www.google-analytics.com/collect?v=1&tid=<ENTER ACCOUNT ID HERE>"

startup.sh
```

### Local Docker 

[See here for full instructions](https://gitlab.nonprod.dwpcloud.uk/idt/forgerock/developer-guidelines/-/wikis/Configuration-of-am#building-custom-nodes)

If you are already running AM in docker, you can copy the jar file with:

```
docker cp /Users/<you>/dev/forgerock-google-analytics-node/target/google-analytics-node-6.0.0.jar docker-compose_am_1:/usr/local/tomcat/webapps/am/WEB-INF/lib
```

...where _docker-compose_am_1_ is the docker container name

Then restart the docker image.

TODO: how to set the GOOGLE_ANALYTICS_URL environment variable ?

### Deployed Systems

[See here](https://gitlab.nonprod.dwpcloud.uk/idt/forgerock/developer-guidelines/-/wikis/Configuration-of-am#building-custom-nodes)
