/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.jdbc.core.connection;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.hint.HintScope;
import io.shardingjdbc.core.jdbc.adapter.AbstractConnectionAdapter;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.statement.MasterSlavePreparedStatement;
import io.shardingjdbc.core.jdbc.core.statement.MasterSlaveStatement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

/**
 * Connection that support master-slave.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MasterSlaveConnection extends AbstractConnectionAdapter {
    
    private final MasterSlaveDataSource masterSlaveDataSource;
    
    private SQLType cachedSQLType;
    
    /**
     * Get database connections via SQL type.
     *
     * <p>DDL will return all connections; DQL will return slave connection; DML or updated before in same thread will return master connection.</p>
     * 
     * @param sqlType SQL type
     * @return database connections via SQL type
     * @throws SQLException SQL exception
     */
    public Collection<Connection> getConnections(final SQLType sqlType) throws SQLException {
        cachedSQLType = sqlType;
        Map<String, DataSource> dataSources = SQLType.DDL == sqlType ? masterSlaveDataSource.getAllDataSources() : masterSlaveDataSource.getDataSource(sqlType).toMap();
        Collection<Connection> result = new LinkedList<>();
        for (Entry<String, DataSource> each : dataSources.entrySet()) {
            String dataSourceName = each.getKey();
            if (getCachedConnections().containsKey(dataSourceName)) {
                result.add(getCachedConnections().get(dataSourceName));
                continue;
            }
            Connection connection = each.getValue().getConnection();
            getCachedConnections().put(dataSourceName, connection);
            result.add(connection);
            replayMethodsInvocation(connection);
        }
        return result;
    }
    
    @Override
    public final CallableStatement prepareCall(final String sql) throws SQLException {
    	return getConnections(SQLType.DML).iterator().next().prepareCall(sql);
    }
    
    @Override
    public final CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
    	return getConnections(SQLType.DML).iterator().next().prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public final CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    	return getConnections(SQLType.DML).iterator().next().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (!getCachedConnections().isEmpty()) {
            return getCachedConnections().values().iterator().next().getMetaData();
        }
        return getConnections(null == cachedSQLType ? SQLType.DML : cachedSQLType).iterator().next().getMetaData();
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        return new MasterSlaveStatement(this);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new MasterSlaveStatement(this, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new MasterSlaveStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql, columnIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return new MasterSlavePreparedStatement(this, sql, columnNames);
    }
    
    @Override
    public void close() throws SQLException {
    	if(!HintScope.isInHintScope()) {
	        HintManagerHolder.clear();
	        MasterSlaveDataSource.resetDMLFlag();
    	}
        super.close();
    }
}
