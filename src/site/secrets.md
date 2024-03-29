# External DB properties

Sometime it may be more useful to put your database properties into an external file and read it at runtime. Furthermore you may
want to keep your database secrets confidential. 

## Code example

```java
final var stream = new FileInputStream(new File("./hsqldb.properties"));
connectorRepository.addConnectionInfo(PROPS, new PropertiesURLConnectorInfo(stream));
```

Where a properties file looks like this:

```
db.url=jdbc:hsqldb:mem:guttenbase
db.driver=org.hsqldb.jdbc.JDBCDriver
db.username=sa
db.password=sa
db.schema=
db.type=HSQLDB
```

## Encrypted properties file

You may encrypt a properties file using the **PropertiesEncryptionTool** class, .e.g.:

    java -cp $HOME/.m2/repository/io/github/guttenbase/guttenbase/3.2.0/guttenbase-3.2.0.jar:$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.9.22/kotlin-stdlib-1.9.22.jar \
        io.github.guttenbase.connector.impl.PropertiesEncryptionTool ./hsqldb.properties ./encrypted.properties

You will have to provide a password for encryption.

And then later on you decrypt the file in your application at runtime, like this:

```java
final var stream = new FileInputStream(new File("./encrypted.properties"));
final var decryptedProperties = PropertiesEncryptionTool(stream).decrypt(readPassword());

connectorRepository.addConnectionInfo(ENCRYPTED, new PropertiesURLConnectorInfo(decryptedProperties));
```
