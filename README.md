# Venari Watcher
Venari Watcher is a companion server application for verifying vulnerabilities in Venari.  The hosted server must be available on the network of the web application under test and the Venari scanning machine.

# Build JAR file
````
mvn clean install
````

# Build Docker Image
````
docker build -t venari-watcher .
````

# Run Docker Image
````
docker run --rm -it -p 80:80 -p 3002:3002 -p 3003:3003 venari-watcher
````


