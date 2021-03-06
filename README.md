Demeter
===
About
---
[Demeter](#Demeter) is a remote package management and delivery server for DSL devices of OpenWRT platform, and it was constructed in the form of [Openfire](https://github.com/igniterealtime/Openfire) plugins.

Features
---
- Packages repository for DSL devices of OpenWRT platform
- Packages remote installation for DSL devices of OpenWRT platform
- Packages remote uninstallation for DSL devices of OpenWRT platform
- Packages remote control for DSL devices of OpenWRT platform

Architecture
---
![](https://github.com/Sercomm-Demeter-group/lcmdemeter/blob/main/resources/demeter-architecture.jpg)

* `MySQL` - Currently Demeter supports MySQL database schema only
* `Openfire` - With [Demeter](#Demeter) plugins, Openfire handles the RESTful API requests and WebSocket communication with DSL devices
* `NGINX` - In additional to host Demeter Portal, it plays the role of reverse proxy to forward Demeter C2C API requests and WebSocket communication from DSL devices to Openfire

Project Components
---
* `lcmdemeter/demeter_portal` - Demeter Portal web pages
* `lcmdemeter/demeter_commons_id` - define the ID rule of DSL devices
* `lcmdemeter/demeter_commons_umei` - define the **U**nified **M**essage **E**xchange **i**nterface standard of Sercomm
* `lcmdemeter/demeter_commons_util` - some useful and common utilities
* `lcmdemeter/demeter_c2c_client` - client methods to access RESTful API of Demeter
* `lcmdemeter/openfire.plugin.demeter_core` - an Openfire plugin which implements the main [features](#Features) of Demeter server
* `lcmdemeter/openfire.plugin.demeter_service_api` - an Openfire plugin which implements RESTful API for Demeter Portal
* `lcmdemeter/openfire.plugin.demeter_c2c_api` - an Openfire plugin which implements RESTful API of Demeter server for cloud to cloud integration
* `lcmdemeter/nginx_conf` - Configuration files for NGINX to be the reverse proxy of Demeter

Getting Start - Manual Build
---
1. Host your [Openfire](https://github.com/igniterealtime/Openfire) server (v4.5.4 is recommended) by building Openfire source code from GitHub or installing the package from [Openfire official site](https://www.igniterealtime.org/downloads/) </br>
**NOTICE 1:** Since Demeter only supports MySQL database schema currently, please select `MySQL` as your database durning configuring Openfire</br>
**NOTICE 2:** Demeter has dependency of [Hazelcast Plugin](https://github.com/igniterealtime/openfire-hazelcast-plugin), and it is necessary to install Hazelcast plugin and enable the cluster manually during configuring your Openfire to ensure that Demeter will work properly</br>

2. Create folder for Demeter server to store packages and the other necessary resources. The default folder path is `/opt/demeter/`
```console
sudo mkdir /opt/demeter
```
3. Install [OpenJDK](https://openjdk.java.net/) and [Maven](https://maven.apache.org/) for building the following packages
4. Build `lcmdemeter/demeter_commons_id` 
```console
$ cd demeter_commons_id
$ ./mvnw install -Dmvn.test.skip=true
```
1. Build `lcmdemeter/demeter_commons_umei` 
```console
$ cd demeter_commons_umei
$ ./mvnw install -Dmvn.test.skip=true
```
1. Build `lcmdemeter/demeter_commons_util` 
```console
$ cd demeter_commons_util
$ ./mvnw install -Dmvn.test.skip=true
```
1. Build `lcmdemeter/demeter_c2c_client` 
```console
$ cd demeter_c2c_client
$ ./mvnw install -Dmvn.test.skip=true
```
8. Build `lcmdemeter/openfire.plugin.demeter_core`, copy the produced Java package file to Openfire plugins folder and rename it to **demeter_core.jar**
```console
$ cd openfire.plugin.demeter_core/
$ ./mvnw install -Dmvn.test.skip=true
$ cp target/demeter_core-openfire-plugin-assembly.jar your-openfire-plugins-folder/.
$ mv your-openfire-plugins-folder/demeter_core-openfire-plugin-assembly.jar your-openfire-plugins-folder/demeter_core.jar
```
9. Build `lcmdemeter/openfire.plugin.demeter_service_api`, copy the produced Java package file to Openfire plugins folder and rename it to **demeter_service_api.jar**
```console
$ cd openfire.plugin.demeter_service_api/
$ ./mvnw verify -Dmvn.test.skip=true
$ cp target/demeter_service_api-openfire-plugin-assembly.jar your-openfire-plugins-folder/.
$ mv your-openfire-plugins-folder/demeter_service_api-openfire-plugin-assembly.jar your-openfire-plugins-folder/demeter_service_api.jar
```
10. Build `lcmdemeter/openfire.plugin.demeter_c2c_api`, copy the produced Java package file to Openfire plugins folder and rename it to **demeter_c2c_api.jar**
```console
$ cd openfire.plugin.demeter_c2c_api/
$ ./mvnw verify -Dmvn.test.skip=true
$ cp target/demeter_c2c_api-openfire-plugin-assembly.jar your-openfire-plugins-folder/.
$ mv your-openfire-plugins-folder/demeter_c2c_api-openfire-plugin-assembly.jar your-openfire-plugins-folder/demeter_c2c_api.jar
```
11. Restart your Openfire
12. Update properties for Demeter by browsing the administration console of Openfire (e.g. http://your-openfire-hostname:9090/) and navigating its `System Properties` page, then modifying the following properties to fit your environment.
    | Property Name                     | Description                                                                            |
    | --------------------------------- | -------------------------------------------------------------------------------------- |
    | sercomm.demeter.host.device.entry | Endpoint address information for DSL devices                                           |
    | sercomm.demeter.host.service.api  | Endpoint address information for Demeter portal pages                                  |
    | sercomm.demeter.storage.scheme  | Endpoint protocol scheme for accessing Demeter package storage.<br />It can be the following values:<br />- **http**<br />- **https**                                  |
    | sercomm.c2c.kafka.config          | Endpoint address and topic information for Demeter to produce messages to Apache Kafka |
    
    **NOTE**:
    Value of "sercomm.c2c.kafka.config" property will be in JSON format. Once this property value has been changed and saved, Demeter server will try to connect to the specific Kafka server automatically and create the topic name if it is necessary. The mentioned JSON arguments are discribed as below, for example:
    ```json
    {
        "enable": "false",
        "bootstrapServers": "127.0.0.1:9092",
        "topicName": "any.specified.topic.name.string",
        "topicPartitionCount": 4
    }
    ```
    | Parameter Name      | Description                                                                                     |
    | ------------------- | ----------------------------------------------------------------------------------------------- |
    | enable              | **false**: disable producing messages to Kafka</br>**true**: enable producing messages to Kafka |
    | bootstrapServers    | Address of specific Kafka server                                                                |
    | topicName           | The topic name where Demeter server will produce messages                                       |
    | topicPartitionCount | Partition count of the topic which "topicName" was specified                                    |
![](https://github.com/sercomm-cloudwu/lcmdemeter/blob/main/resources/demeter-properties.png)
1.  Adjust your NGINX configuration by refering all of the configuration files in `lcmdemeter/nginx_conf`, then reloading NGINX
2.  Deploy `lcmdemeter/demeter_portal` to root folder of NGINX, e.g.:
```console
$ cd demeter_portal/
$ cp -R * /usr/share/nginx/html/.
```
15.  Finally, browse and login Demeter portal. The default username and password will be:
```
Username: admin
Password: 12345678
```
![](https://github.com/sercomm-cloudwu/lcmdemeter/blob/main/resources/demeter-portal.png)

Making Changes
---
For understanding which container that the specific OpenWRT package should be installed and ensure the context of ubus command can be sent properly, Demeter needs to recognize your DSL devices by `Model` field of `System.Hardware` ubus command response like:
```json
{
    "Carrier":"Vodafone",
    "CasingColour":"Black",
    "FriendlyName":"Sercomm HG5244B Router",
    "MAC":"AABBCCDDEEFF",
    "Manufacturer":"Sercomm",
    "Model":"HG5244B",
    "ProductClass":"HG5244B",
    "SerialNumber":"123456789012345A1",
    "SoftwareVersion":"HG5244B3004D",
    "Variant":"VFES"
}
```
Suppose that you have your own devices with specific model name which Demeter cannot recognize currently, then it is necessary to add its command profile by implementing `com.sercomm.openfire.plugin.profile.IProfile` interface of `lcmdemeter/openfire.plugin.demeter_core` plugin.
```java
public interface IProfile
{
    /*
     * Get CPU, memory and storage usages by ubus command
     */
    Map<?,?> getUsage(String serial, String mac, String appName) throws Throwable;    
    /*
     * Install specific package (App) by ubus command
     */
    Map<?,?> installApp(String serial, String mac, String appName, Map<String, Object> payload) throws Throwable;
    /*
     * Patch/upgrade specific package (App) by ubus command
     */
    Map<?,?> patchApp(String serial, String mac, String appName, Map<String, Object> payload) throws Throwable;
    /*
     * Uninstalled specific package (App) by ubus command
     */
    Map<?,?> uninstallApp(String serial, String mac, String appName) throws Throwable;
    /*
     * Get details of installed packages (App) by ubus command
     */
    List<Map<?,?>> getInstalledApps(String serial, String mac) throws Throwable;
    /*
     * Get details of installed package (App) by ubus command
     */
    Map<?,?> getInstalledApp(String serial, String mac, String appName) throws Throwable;
    /*
     * Start the specific package (App) by ubus command
     */
    Map<?,?> startApp(String serial, String mac, String appName) throws Throwable;
    /*
     * Stop the specific package (App) by ubus command
     */
    Map<?,?> stopApp(String serial, String mac, String appName) throws Throwable;
    /*
     * Generate context of container configuration
     * NOTICE: reserved and TODO
     */
    String generateContainerConfiguration(String serial, String mac, String appName) throws Throwable;
}
```
Please refer to [Testing Your Changes](https://github.com/igniterealtime/Openfire#testing-your-changes) of Openfire GitHub Repository about how to verify and debug your own changes.

