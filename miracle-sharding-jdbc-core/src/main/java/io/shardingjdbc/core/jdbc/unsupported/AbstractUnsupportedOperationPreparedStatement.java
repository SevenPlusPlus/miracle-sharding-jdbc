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

package io.shardingjdbc.core.jdbc.unsupported;

import io.shardingjdbc.core.jdbc.adapter.AbstractStatementAdapter;

import java.io.Reader;
import java.sql.Array;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Unsupported {@code PreparedStatement} methods.
 * 
 * @author zhangliang
 */
public abstract class AbstractUnsupportedOperationPreparedStatement extends AbstractStatementAdapter implements PreparedStatement {
    
    public AbstractUnsupportedOperationPreparedStatement() {
        super(PreparedStatement.class);
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("getMetaData");
    }
    
    @Override
    public final ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("ParameterMetaData");
    }
    
    @Override
    public final void setNString(final int parameterIndex, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNString");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final NClob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNCharacterStream(final int parameterIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream");
    }
    
    @Override
    public final void setNCharacterStream(final int parameterIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream");
    }
    
    @Override
    public final void setArray(final int parameterIndex, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setArray");
    }
    
    @Override
    public final void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRowId");
    }
    
    @Override
    public final void setRef(final int parameterIndex, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRef");
    }
    
    @Override
    public final ResultSet executeQuery(final String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeQuery with SQL for PreparedStatement");
    }
    
    @Override
    public final int executeUpdate(final String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
    }
    
    @Override
    public final int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
    }
    
    @Override
    public final int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
    }
    
    @Override
    public final int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
    }
    
    @Override
    public final boolean execute(final String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
    }
    
    @Override
    public final boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
    }
    
    @Override
    public final boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
    }
    
    @Override
    public final boolean execute(final String sql, final String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
    }
}
