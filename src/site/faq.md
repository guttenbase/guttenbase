# Frequently Asked Questions (FAQ)

## Q: Why do I get a "java.sql.SQLIntegrityConstraintViolationException: ORA-01400: cannot insert NULL" when copying to an Oracle DB?
A: Oracle chose for some reason to treat empty strings as NULL (See discussion in stackoverflow).

There is a way to get around this using a DefaultColumnDataMapperProviderHint:

connectorRepository.addConnectorHint(TARGET, new DefaultColumnDataMapperProviderHint() {
protected void addMappings(final DefaultColumnDataMapperProvider columnDataMapperFactory) {
super.addMappings(columnDataMapperFactory);

```java
columnDataMapperFactory.addMapping(ColumnType.CLASS_STRING, ColumnType.CLASS_STRING, new ColumnDataMapper() {
        public boolean isApplicable(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData)
                throws SQLException {
            return sourceColumnMetaData.getColumnName().equalsIgnoreCase("MY_COLUMN");
        }
    
        public Object map(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData, final Object value)
                throws SQLException {
            if ("".equals(value.toString().trim())) {
                return " ";
            } else {
                return value;
            }
        }
    });
    }
});
```

## Q: Why do I get a "java.sql.SQLSyntaxErrorException: ORA-00933: SQL command not properly ended" when copying to an Oracle DB?
A: Well, unfortunately not all DB systems obey the standard and support multiple VALUES clauses in an INSERT statement. See discussion in Wikipedia.

You'll have to limit the number of VALUES clauses to 1 or use the BATCH mode.

```java
connectorRepository.addConnectorHint(TARGET, new NumberOfRowsPerInsertionHint() {
    public NumberOfRowsPerInsertion getValue() {
        return new NumberOfRowsPerInsertion() {
            public boolean useMultipleValuesClauses(final TableMetaData targetTableMetaData) {
                return false;
            }

            @Override
            public int getNumberOfRowsPerInsertion(final TableMetaData targetTableMetaData) {
                return 2000;
            }
        };
    }
});
```