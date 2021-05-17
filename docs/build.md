
# Build the Google Analytics Node

See this link
[Preparing for Development](https://backstage.forgerock.com/docs/am/6.5/auth-nodes/#preparing-for-nodes)
for how to setup your local maven environment to build authentication nodes

Once maven is setup, to build

`mvn clean install`

Some useful links:

[Authentication Node Development Guide](https://backstage.forgerock.com/docs/am/6.5/auth-nodes/)
 ... ForgeRock documentation

[Upgrading Nodes and Configuration Changes](https://backstage.forgerock.com/docs/am/6.5/auth-nodes/#node-upgrade)
 ... updating the custom node - this is a common cause of failure 

### Local Tomcat deployment

To install Tomcat see the [ForgeRock Quick Start installation guide](https://backstage.forgerock.com/docs/am/6.5/quick-start-guide/)

To deploy the authentication node, create a script `deploy.sh` with the following:

```
cd /Users/<you>/dev/forgerock-google-analytics-node/target

cp google-analytics-node-6.0.0.jar /Users/<you>/dev/apache-tomcat-7.0.105/webapps/openam/WEB-INF/lib/google-analytics-node-6.0.0.jar

cd /Users/<you>/dev/apache-tomcat-7.0.105/bin

export GOOGLE_ANALYTICS_URL="http://www.google-analytics.com/collect?v=1&tid=<ENTER ACCOUNT ID HERE>"

sh startup.sh
```
...the above assumes you have
 - installed the same version of Tomcat
 - installed the OpenAM webapp as `openam`
 - everything in a `/dev` directory
 - shutdown Tomcat first using `shutdown.sh`

### Local Tomcat debugging

When installed locally a directory is created at the root

`/Users/<you>/openam/openam/debug/GoogleAnalyticsNode` logging from the custom node

`/Users/<you>/openam/openam/debug/CoreSystem` general system errors
