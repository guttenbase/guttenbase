# Examples and use cases

## Mass data production
During development you will probably generate some test data. Now you want to go public, but before you'd like to prove your system can handle much more data than the few items of your test data.

This examples shows how to duplicate the data multiply in the data base and how to alter some of it to get some distinction. The main problem is the correct handling of primary keys. In this example we simply look for the maximum ID in each table and use that as an offset for new IDs. Of course tables with foreign key need to be updated, too.

During multiplication we alter some of the data, i.e. we change all user names.

```java
import java.sql.SQLException;

public class MassDataProducerTest extends AbstractGuttenBaseTest
{
  private static final int MAX_LOOP = 5;
  public static final String SOURCE = "SOURCE";
  public static final String TARGET = "TARGET";

  private final ColumnDataMapper _nameDataMapper = new ColumnDataMapper()
  {
    @Override
    public boolean isApplicable(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData)
        throws SQLException
    {
      return sourceColumnMetaData.getColumnName().toUpperCase().endsWith("NAME");
    }

    @Override
    public Object map(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData, final Object value)
        throws SQLException
    {
      return value + "_" + _loopCounter;
    }
  };

  private final ColumnDataMapper _idDataMapper = new ColumnDataMapper()
  {
    @Override
    public boolean isApplicable(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData)
        throws SQLException
    {
      return sourceColumnMetaData.getColumnName().toUpperCase().endsWith("ID");
    }

    @Override
    public Object map(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData, final Object value)
        throws SQLException
    {
      return ((Long) value) + getOffset(sourceColumnMetaData);
    }
  };

  private final Map<TableMetaData, Long> _maxTableIds = new HashMap<TableMetaData, Long>();
  private int _loopCounter;

  @Before
  public void setup() throws Exception
  {
    _connectorRepository.addConnectionInfo(SOURCE, new TestDerbyConnectionInfo());
    _connectorRepository.addConnectionInfo(TARGET, new TestH2ConnectionInfo());
    new ScriptExecutorTool(_connectorRepository).executeFileScript(SOURCE, "/ddl/tables-hsqldb.sql");
    new ScriptExecutorTool(_connectorRepository).executeFileScript(TARGET, "/ddl/tables-hsqldb.sql");
    new ScriptExecutorTool(_connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql");

    DefaultColumnDataMapperProvider.INSTANCE.addMapping(ColumnType.CLASS_STRING, ColumnType.CLASS_STRING, _nameDataMapper);
    DefaultColumnDataMapperProvider.INSTANCE.addMapping(ColumnType.CLASS_LONG, ColumnType.CLASS_LONG, _idDataMapper);

    computeMaximumIds();
  }

  @Test
  public void testDataDuplicates() throws Exception
  {
    for (_loopCounter = 0; _loopCounter < MAX_LOOP; _loopCounter++)
    {
      new DefaultTableCopyTool(_connectorRepository).copyTables(SOURCE, TARGET);
    }

    final List<Map<String, Object>> listUserTable = new ScriptExecutorTool(_connectorRepository).executeQuery(TARGET,
        "SELECT DISTINCT ID, USERNAME, NAME, PASSWORD FROM FOO_USER ORDER BY ID");

    assertEquals(5 * MAX_LOOP, listUserTable.size());
    final List<Map<String, Object>> listUserCompanyTable = new ScriptExecutorTool(_connectorRepository).executeQuery(TARGET,
        "SELECT DISTINCT USER_ID, ASSIGNED_COMPANY_ID FROM FOO_USER_COMPANY ORDER BY USER_ID");

    assertEquals(3 * MAX_LOOP, listUserCompanyTable.size());
  }

  private long getOffset(final ColumnMetaData sourceColumnMetaData)
  {
    ColumnMetaData idColumnMetaData = sourceColumnMetaData.getReferencedColumn();

    if (idColumnMetaData == null)
    {
      idColumnMetaData = sourceColumnMetaData;
    }

    final TableMetaData tableMetaData = idColumnMetaData.getTable();
    final Long maxId = _maxTableIds.get(tableMetaData);

    assertNotNull(sourceColumnMetaData + ":" + tableMetaData, maxId);

    return _loopCounter * maxId;
  }

  private void computeMaximumIds() throws SQLException
  {
    final List<TableMetaData> tables = _connectorRepository.getDatabase(SOURCE).getTable();
    final EntityTableChecker entityTableChecker = _connectorRepository.getConnectorHint(SOURCE, EntityTableChecker.class)
        .getValue();
    final MinMaxIdSelectorTool minMaxIdSelectorTool = new MinMaxIdSelectorTool(_connectorRepository);

    for (final TableMetaData tableMetaData : tables)
    {
      if (entityTableChecker.isEntityTable(tableMetaData))
      {
        minMaxIdSelectorTool.computeMinMax(SOURCE, tableMetaData);
        _maxTableIds.put(tableMetaData, minMaxIdSelectorTool.getMaxValue());
      }
    }
  }
}
```