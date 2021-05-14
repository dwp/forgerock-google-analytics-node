
# Build the Google Analytics Node

https://gitlab.nonprod.dwpcloud.uk/idt/forgerock/developer-guidelines/-/wikis/Configuration-of-AM-and-working-with-custom-nodes
... how to setup maven - see the `Building Custom Nodes` section

Once maven is setup, to build

`mvn clean install`

https://backstage.forgerock.com/docs/am/6.5/auth-nodes/
 ... ForgeRock documentation

https://backstage.forgerock.com/docs/am/6.5/auth-nodes/#node-upgrade
 ... updating the custom node - this is a common cause of failure 

### Local Tomcat deployment

Create a script `deploy.sh` with the following:

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
 
See here https://gitlab.nonprod.dwpcloud.uk/idt/forgerock/developer-guidelines/-/wikis/Running-AM-and-IDM-locally
 ... for how to install Tomcat and AM

[ForgeRock Quick Start installation guide](https://backstage.forgerock.com/docs/am/6.5/quick-start-guide/)

### Local Tomcat debugging

When installed locally a directory is created at the root

`/Users/<you>/openam/openam/debug/GoogleAnalyticsNode` logging from the custom node

`/Users/<you>/openam/openam/debug/CoreSystem` general system errors
