
# Geographical IDs

https://developers.google.com/analytics/devguides/collection/protocol/v1/geoid

The first part of UK postcodes is available in the list, but unfortunately
these codes **do not appear to work** in the analytics views

```
Criteria ID,Name,Canonical Name,Parent ID,Country Code,Target Type,Status
"9046316","LS6","LS6,England,United Kingdom","20339","GB","Postal Code",Active
"9046317","LS7","LS7,England,United Kingdom","20339","GB","Postal Code",Active
"9046318","LS8","LS8,England,United Kingdom","20339","GB","Postal Code",Active
"9046319","LS2","LS2,England,United Kingdom","20339","GB","Postal Code",Active
```

The city codes do work, so you can do:

`sharedState.put( 'googleAnalytics_geoid', '1007121' )`

However, how we determine the value '1007121' is another matter...

```
Criteria ID,Name,Canonical Name,Parent ID,Country Code,Target Type,Status
"1007116","Stockbridge","Stockbridge,England,United Kingdom","20339","GB","City",Active
"1007117","Stockley","Stockley,England,United Kingdom","20339","GB","City",Active
"1007118","Stockport","Stockport,England,United Kingdom","20339","GB","City",Active
"1007119","Stockton-on-Tees","Stockton-on-Tees,England,United Kingdom","20339","GB","City",Active
"1007120","Stoke Gifford","Stoke Gifford,England,United Kingdom","20339","GB","City",Active
"1007121","Stoke-on-Trent","Stoke-on-Trent,England,United Kingdom","20339","GB","City",Active
"1007122","Stoke Poges","Stoke Poges,England,United Kingdom","20339","GB","City",Active
```
