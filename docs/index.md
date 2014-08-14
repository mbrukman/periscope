Periscope
=========

*Periscope is a powerful, fast, thick and top-to-bottom right-hander, eastward from Sumbawa's famous west-coast. Timing is critical, as needs a number of elements to align before it shows its true colors.*

*Periscope is a heuristic Hadoop scheduler you associate with a QoS profile. Built on YARN schedulers, cloud and VM resource management API's it allows you to associate SLAs to applications and customers.*

##Overview

The purpose of Periscope is to bring QoS to a multi-tenant Hadoop cluster, while allowing to apply SLAs to individual applications and customers.
At [SequenceIQ](http://sequenceiq.com) working with multi-tenant Hadoop clusters for quite a while we have always seen the same frustration and fight for resource between users.
The **FairScheduler** was partially solving this problem - bringing in fairness based on the notion of [Dominant Resource Fairness](http://static.usenix.org/event/nsdi11/tech/full_papers/Ghodsi.pdf).
With the emergence of Hadoop 2 YARN and the **CapacityScheduler** we had the option to maximize throughput and the utilization of the cluster for a multi-tenant cluster in an operator-friendly manner.
The scheduler works around the concept of queues. These queues are typically setup by administrators to reflect the economics of the shared cluster.
While there is a pretty good abstraction and brings some level of SLA for `predictable` workloads, it often needs proper `design ahead`.
The queue hierarchy and resource allocation needs to be changed when new tenants and workloads are moved to the cluster.

Periscope was designed around the idea of `dynamic` clusters - without any need to preconfigure queues, cluster nodes or apply capacity planning ahead.

##How it works

Periscope monitors the application progress, the number of YARN containers/resources and their allocation on nodes, queue depths, and the number of nodes and their health.
Since we have switched to YARN a while ago (been among the first adopters) we have run an open source [monitoring project](https://github.com/sequenceiq/yarn-monitoring), based on R.
We have been collecting metrics from the YARN Timeline server, Hadoop Metrics2 and Ambari's Nagios/Ganglia - and profiling applications and correlating with these metrics.
One of the key findings we have found - and have applied to Periscope - was that while low level metrics are good to understand the cluster health - they might not necessarily help on making decisions when applying different SLAs on a multi-tenant cluster.
Focusing on higher level building blocks as queue depth, YARN containers, etc actually brings in the same quality of service, while not being lost in low level details.
We will follow up with examples and metrics on coming blog posts - make sure you follow us on [LinkedIn](https://www.linkedin.com/company/sequenceiq/), [Twitter](https://twitter.com/sequenceiq) or [Facebook](https://www.facebook).

_Example: Applying SLA based on `resource` load might not be the best solution - each application tasks generates different loads, and a CPU heavy reduce step might be followed by an I/O heavy mapper - making a decision based on a low `snapshot` might not be the right option.
Also note that a YARN cluster can run different applications - MR2, HBase, Spark, etc - and they all generate different load across different timeframes.
When YARN allocates containers it associates `resources` - it's actually more predictable to let YARN to deal with the resource allocation, and have Periscope orchestrate the process._

Periscope works with two types of Hadoop clusters: `static` and `dynamic`.

##Clusters

### Static clusters
From Periscope point of view we consider a cluster static when the cluster capacity can't be increased horizontally.
This means that the hardware resources are already given - and the throughput can't be increased by adding new nodes.
Periscope introspects the job submission process, monitors the applications and applies the following SLAs:

  1. Application ordering - can guaranty that a higher priority application finishes before another one (supporting parallel or sequential execution)
  2. Moves running applications between priority queues
  3. *Attempts* to enforce time based SLA (execution time, finish by, finish between, recurring)
  4. *Attempts* to enforce guaranteed cluster capacity requests ( x % of the resources)
  5. Support for distributed (but not YARN ready) applications using Apache Slider

### Dynamic clusters
From Periscope point of view we consider a cluster dynamic when the cluster capacity can be increased horizontally.
This means that nodes can be added dynamically - thus the throughput can be increased or decreased based on the cluster load, and scheduled applications.
In order to do that Periscope instructs [Cloudbreak](http://sequenceiq.com/cloudbreak/) to add or remove nodes from the cluster based on the SLAs and thus continuously provide a high *quality of service* for the multi-tenand Hadoop cluster.
Just to refresh memories - [Cloudbreak](http://sequenceiq.com/products.html) is [SequenceIQ's](http://sequenceiq.com) open source, cloud agnostic Hadoop as a Service API.
Given the option of provisioning or decommissioning cluster nodes on the fly, Periscope allows you to use the following set of SLAs:

  1. Application ordering - can guaranty that a higher priority application finishes before another one (supporting parallel or sequential execution)
  2. Moves running applications between priority queues
  3. *Enforce* time based SLA (execution time, finish by, finish between, recurring) by increasing cluster capacity and throughput
  4. Smart decommissioning - avoids HDFS storms, keeps `payed` nodes alive till the last minute
  5. *Enforce* guaranteed cluster capacity requests ( x % of the resources)
  6. *Private* cluster requests - supports provisioning of short lived private clusters with the possibility to merge
  7. Support for distributed (but not YARN ready) applications using Apache Slider

##Applications

##Policy
In order to horizontally scale up or down different policies can be applied at runtime. By default no policy is specified. A policy consists
of different rules which will determine whether to add or remove nodes from an existing cluster. For this purpose Periscope will instruct
Cloudbreak to launch new cloud instances and join them together or gracefully decommission them. Another benefit of using Cloudbreak is that in case of
down scaling it will take care of the data loss as it is always a challenge not to mention that it will do it in a cost friendly manner.
Rules can be added in different ways. There are pre-defined scale up/down rules which can be configured, but the main advantage of using Periscope
is that you can provide your own implementation and without any classpath magic it can be used at runtime. The pre-defined rules are the following:

* ResourcesBelowRule  
  Computes the free resource rate of the cluster and if the rate is below the specified threshold it will add new nodes.
* ResourcesAboveRule  
  Computes the free resource rate of the cluster and if the rate is above the specified threshold it will remove nodes.
* PendingAppsRule  
  If the pending applications exceed a certain limit it will add new nodes.
* PendingContainersRule  
  If the pending containers(resource requests) exceed a certain limit based on the container/node rate it will add new nodes.
* ForceNodeCountRule  
  Regardless of any resource usage and Hadoop configuration it will force the number of nodes to the specified.

Every rule can be configured to not to scale above a limit preventing to create a limitless cluster. In one policy multiple rules can be specified
and the first rule which will change the size of the cluster will apply. In your own implementation you can aggregate different cluster metrics
to create the exact rule which serves you the best.