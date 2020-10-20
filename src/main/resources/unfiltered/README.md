Unity Intelligence Engine Performance Tests vesrion {project.version}.
 
 
Copyright (c) 2005-2019 Intellective Inc.

configs folder contains sample configurations for Dummy crawler, searcher and SOLR8 setup.


Modify application.properties file to customize tests:

* Search requests count:
  request.count=1000
  Parallel executions:
* request.threads=10
  UIE searcher URL:
* uie.url=http://localhost:8080/uie-searcher

users and queries folder contains sample HLI queries and VU user contexts. 
Modify queries and contexts to match your index fields and searcher users ids and roles.