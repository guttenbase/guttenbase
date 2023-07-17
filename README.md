[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.guttenbase/guttenbase/badge.svg)](https://central.sonatype.com/search?q=guttenbase)
<meta name="google-site-verification" content="auOvsU7wt1p_7X07rHySknnRdCmwldQRpABsB-zar_Y" />

# ![Banner](src/site/images/logo.png) GuttenBase - Copying done right...

There are many tools to visualize or analyze databases. You will also find lots of programs programs to copy databases between
different systems. However, we found that these tools are not flexible enough and do not fulfill all our needs. They fail, e.g.,
because they can not map the various data types between data bases correctly, or because the amount of data becomes to big.
Examples for such applications are the
[DBCopy-Plugin](http://dbcopyplugin.sourceforge.net/) of [SQuirreL](http://squirrel-sql.sourceforge.net/). You may also have heard
of the database refactoring tool [Liquibase](http://www.liquibase.org/). And of course there is commercial software available
such as the [MySQL Migration Toolkit](https://www.mysql.com/products/workbench/migrate/).

The solution we suggest is to _program_ data migrations using an extensible framework instead of _configuring_ some (limited)
tool.
We found that this approach gives us much more flexibility when performing data migrations. Migrating a data base almost always
requires an individual, special solution, since every data base and RDBMS has its peculiarities.

Another problem we address is the fact that databases are often accessible from within some internal network only. As developers
we would like to work independentlywith a local database clone on our computer. And we don't want to install Oracle or DB2 for
that, but use Open Source products
such as Postgresql or MySQL.

## Quick Example

The following code example first checks whether the source and destination are compatible.
Then the values from the source database are written to the target database.
Finally, we perform (empirical) checks whether the data has been transmitted correctly.

In many cases, that's it!

```java
  final ConnectorRepository connectorRepository = new ConnectorRepositoryImpl();
  connectorRepository.addConnectionInfo("MySql", new AevMySqlConnectionInfo());
  connectorRepository.addConnectionInfo("Postgresql", new AevPostgresqlConnectionInfo());

  new CheckSchemaCompatibilityTool(connectorRepository).checkTableConfiguration("MySql", "Postgresql");
  new DefaultTableCopyTool(connectorRepository).copyTables("MySql", "Postgresql");
  new CheckEqualTableDataTool(connectorRepository).checkTableData("MySql", "Postgresql");
```

# Core features

- Dump a database into a ZIP/JAR file, transfer the file somewhere else via SSH/FTP/... and extract it locally
- GuttenBase supports many data base systems to automatically map different column types
- Transform columns and tables during the migration
    - renaming columns
    - changing the column type
    - split columns into multiple columns
    - ...
- Transform data during the migration
- Optimize the copying process for maximum speed using vendor-specific statements
- Create mass data for testing purposes by copying the source DB multiple times
- Use meta model of data base for analysis
- Create tables from imported schema automatically
- Create schema definitions programatically
- Highly configurable via custom configuration "hints"
- Convert column data on the fly
- Copy just a subset of all data

# Documentation

For documentation and more examples please refer to the [site documentation](src/site/index.md), the
[Getting Started Guide](src/site/gettingstarted.md) and the [API-Docs](https://javadoc.io/doc/io.github.guttenbase/guttenbase/latest/io/github/guttenbase/guttenbase/latest/index.html).

## Getting started

Add the following dependencies to your pom.xml file to get started using **guttenbase**:

```xml
<dependency>
    <groupId>io.github.guttenbase</groupId>
    <artifactId>guttenbase</artifactId>
    <version>3.0.1</version>
</dependency>
```

## Licensing

guttenbase is licensed under the GNU Lesser General Public License (LGPL) version 2.1 or later.

## Links

* Source Code:      https://github.com/guttenbase/guttenbase
* Issue Tracking:   https://github.com/guttenbase/guttenbase/issues
