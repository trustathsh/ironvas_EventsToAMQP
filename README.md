ironvas EventsToAMQP
=======
ironvas EventsToAMQP is a *highly experimental* integration of Open Vulnerability
Assessment System ([OpenVAS] [1]) into a MAP-Infrastructure and the CLEARER project Infrastructure [CLEARER Project][2]. 
The integration aims to share security related informations (vulnerabilities
detected by OpenVAS) with other network components in the [TNC architecture] [3]
via IF-MAP and CLEARER. This version only works with the ironevents project here on trustathsh github. 

[![Build Status](https://travis-ci.org/trustathsh/ironvas.png)](https://travis-ci.org/trustathsh/ironvas)

ironvas EventsToAMQP consists of two elements:

* One part - the "publisher" - simply fetches the latest scan reports stored in
  an OpenVAS server, converts them into IF-MAP metadata and/or AMQP
  (currently "event"-metadata) and finally publishes them into a MAP server and/or RabbitMQ.
  ironvas takes care to not flood the MAPS with
  redundant information, furthermore you can specify a filter (in `filter.js`)
  for the vulnerabilities to publish.
  If a scan report is deleted from the OpenVAS server, ironvas will purge all
  published metadata, associated with the deleted report, from the MAPS and sends a deleted event to the AMQP Queue.
  In other words this means that ironvas EventsToAMQP always tries to reflect the current/latest
  knowledge of an OpenVAS server in a MAP server.
  The event-metadata Or AMQP event that ironvas published is filled with the following
  values from the scan reports:
  - the name of the vulnerability
  - the time it was discovered
  - the id of the discoverer (OpenVAS server)
  - the magnitude of the vulnerability
  - the significance
  - the event-type == CVE
  - CVE information
  - and the corresponding URIs for the CVE entries

* The second, more experimental, part of ironvas EventsToAMQP - the "subscriber" - goes the
  other way around.
  It will subscribe for "request-for-investigation"-metadata of a PDP in the MAPS or it listens for an AMQP 
  scan event. If the PDP publish those metadata to an IP address, ironvas schedules a new
  scan task for that IP address in OpenVAS. If the scan produces new
  vulnerability information they are collected by the "publisher" as described
  above.
  If the PDP removes the "request-for-investigation"-metadata from the IP
  address, ironvas also removes the scan task (and with it the report) from
  OpenVAS.
  You can also publish a delete event to AMQP to delete a scan task.

The binary package (`ironvas_EventsToAMQP-x.x.x-bundle.zip`) of ironvas
is ready to run, all you need is to configure it to your needs.
If you like to build ironvas EventsToAMQP by your own you can use the
latest code from the [GitHub repository][githubrepo].


Requirements
============
To use the binary package of ironvas EventsToAMQP you need the following components:

* OpenJDK Version 1.8 or higher
* OpenVAS-8 or higher
* MAP server implementation (e.g. [irond] [4])

If you have downloaded the source code and want to build ironvas EventsToAMQP by
yourself Maven 3 is also needed.


Configuration
=============
To setup the binary package you need to import the OpenVAS, AMQP-Server(optional)
and MAP server certificates into `ironvas.jks`.
On a Ubuntu installation of OpenVAS you can find the OpenVAS certificate in
`/var/lib/openvas/CA/servercert.pem`. If you want to use ironvas EventsToAMQP with irond
the keystores of both are configured with ready-to-use testing certificates.

The remaining configuration parameters can be done through the
`configuration.properties` file in the ironvas EventsToAMQP package.
In general you have to specify:

* the OpenVAS server IP address,
* the OpenVAS OMP port,
* the OpenVAS OMP credentials,
* the MAPS URL and credentials.

Optional the AMQP connection information
and credentials

Have a look at the comments in `configuration.properties` for more details.


Building
========
You can build ironvas EventsToAMQP by executing:

	$ mvn package

in the root directory of the ironvas project.
Maven should download all further needed dependencies for you. After a successful
build you should find the `ironvas_EventsToAMQP-x.x.x-bundle.zip` in the `target` sub-directory.


Running
=======
To run the binary package of ironvas EventsToAMQP simply execute:

	$ ./start.sh


Feedback
========
If you have any questions, problems or comments, please contact
	<f4-i-trust@lists.hs-hannover.de>


LICENSE
=======
ironvas EventsToAMQP is licensed under the [Apache License, Version 2.0] [5].


Note
====

ironvas EventsToAMQP is an experimental prototype and is not suitable for actual use. The Scala code is not
really idiomatic Scala, but some kind of learning-experiment.

Feel free to fork/contribute.


[1]: http://www.openvas.org
[2]: http://www.clearer-project.de/index.php/en/homepage.html
[3]: http://www.trustedcomputinggroup.org/developers/trusted_network_connect
[4]: https://github.com/trustathsh
[5]: http://www.apache.org/licenses/LICENSE-2.0.html
[githubrepo]: https://github.com/trustathsh/ironvas
