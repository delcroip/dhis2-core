package org.hisp.dhis.dxf2.events.trackedentity.store.query;

import java.util.Map;

import org.hisp.dhis.dxf2.events.trackedentity.store.TableColumn;

import com.google.common.collect.ImmutableMap;

/**
 * @author Luciano Fiandesio
 */
public class TeiAttributeQuery
{
    public enum COLUMNS
    {
        TEI_UID,
        TEI_ID,
        CREATED,
        UPDATED,
        VALUE,
        STOREDBY,
        ATTR_UID,
        ATTR_NAME,
        ATTR_VALUE_TYPE,
        ATTR_CODE,
        ATTR_SKIP_SYNC
    }

    public static Map<COLUMNS, TableColumn> columnMap = ImmutableMap.<COLUMNS, TableColumn> builder()
        .put( COLUMNS.TEI_UID, new TableColumn( "tei", "uid", "teiuid" ) )
        .put( COLUMNS.TEI_ID, new TableColumn( "teav", "trackedentityinstanceid", "id" ) )
        .put( COLUMNS.CREATED, new TableColumn( "teav", "created" ) )
        .put( COLUMNS.UPDATED, new TableColumn( "teav", "lastupdated" ) )
        .put( COLUMNS.STOREDBY, new TableColumn( "teav", "storedby" ) )
        .put( COLUMNS.VALUE, new TableColumn( "teav", "value" ) )
        .put( COLUMNS.ATTR_UID, new TableColumn( "t", "uid", "att_uid" ) )
        .put( COLUMNS.ATTR_VALUE_TYPE, new TableColumn( "t", "valuetype", "att_val_type" ) )
        .put( COLUMNS.ATTR_CODE, new TableColumn( "t", "code", "att_code" ) )
        .put( COLUMNS.ATTR_NAME, new TableColumn( "t", "name", "att_name" ) )
        .put( COLUMNS.ATTR_SKIP_SYNC, new TableColumn( "t", "skipsynchronization", "att_skip_sync" ) )
        .build();

    public static String getQuery()
    {
        return getSelect() +
            "from trackedentityattributevalue teav " +
            "join trackedentityattribute t on teav.trackedentityattributeid = t.trackedentityattributeid " +
            "join trackedentityinstance tei on teav.trackedentityinstanceid = tei.trackedentityinstanceid " +
            "where teav.trackedentityinstanceid in (:ids)";
    }

    private static String getSelect()
    {
        return QueryUtils.getSelect( columnMap.values() );
    }

    public static String getColumnName( COLUMNS columns )
    {
        return columnMap.get( columns ).getResultsetValue();
    }
}