# Change log

## 3.3.3

- Generally map DB-specific types to their generic counterparts

## 3.3.2

- Filter synthetic indexes and foreign keys, i.e. constraints created by the database itself
- Fix plain text export issues

## 3.3.1

- Export data to SQL script via JDBC driver implementation
- Code cleanup and removal of deprecated code
- Blob handling for different types of RDBMS 

## 3.2.x

- Added support for builder pattern style in connector repository
- Added support to externalize and optionally encrypt database properties

## 3.1.x

- Added support for copying autoincrement columns across database systems (CopySchemaTool)
- Make connector repository reachable from anywhere in the meta model

## 3.0.x

- Reimplemention in Kotlin