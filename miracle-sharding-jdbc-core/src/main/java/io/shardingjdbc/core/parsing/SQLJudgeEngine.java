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

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.parsing.lexer.Lexer;
import io.shardingjdbc.core.parsing.lexer.analyzer.Dictionary;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import lombok.RequiredArgsConstructor;

/**
 * SQL judge engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    
    private boolean isPgProcedure()
    {
    	String trimedSql = sql.trim();
    	if(trimedSql.contains("SELECT") || trimedSql.contains("select"))
    	{
    		int firstLeftParPos = trimedSql.indexOf('(');
    		int lastLeftParPos = trimedSql.lastIndexOf('(');
    		int firstRightParPos = trimedSql.indexOf(')');
    		int lastRightParPos = trimedSql.lastIndexOf(')');
    		
    		if((firstLeftParPos > 0 && lastLeftParPos == firstLeftParPos)/*only one left parenthesis*/
    				&& (firstRightParPos > 0 && lastRightParPos == firstRightParPos) /*only one right parenthesis*/
    				&& firstRightParPos == (trimedSql.length() - 1))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    private SQLStatement judgePgProcedureType()
    {
    	String trimedSql = sql.trim();
    	
    	int firstLeftParPos = trimedSql.indexOf('(');
    	String beforeLeftParStr = trimedSql.substring(0, firstLeftParPos).trim();
    	String[] tokenArr = beforeLeftParStr.split(" ");
    	String funcName = beforeLeftParStr;
    	if(tokenArr != null && tokenArr.length > 0)
    	{
    		funcName = tokenArr[tokenArr.length-1];
    	}
    	
    	String sqlOp = funcName;
    	String[] wordArr = funcName.split("_");
    	if(wordArr != null && wordArr.length > 0)
    	{
    		sqlOp = wordArr[wordArr.length - 1];
    	}
    	sqlOp = sqlOp.toLowerCase();
    	if(sqlOp.contains("insert") || sqlOp.contains("update") || sqlOp.contains("delete")
    			|| sqlOp.contains("add") || sqlOp.contains("remove"))
    	{
    		return new DMLStatement();
    	}
    	else
    	{
    		return new SelectStatement();
    	}
    }
    
    /**
     * judge SQL Type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
    	if(isPgProcedure())
    	{
    		return judgePgProcedureType();
    	}
    	
        Lexer lexer = new Lexer(sql, new Dictionary());
        lexer.nextToken();
        while (true) {
            TokenType tokenType = lexer.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (DefaultKeyword.SELECT == tokenType) {
                    return new SelectStatement();
                }
                if (DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType) {
                    return new DMLStatement();
                }
                if (DefaultKeyword.CREATE == tokenType || DefaultKeyword.ALTER == tokenType || DefaultKeyword.DROP == tokenType || DefaultKeyword.TRUNCATE == tokenType) {
                    return new DDLStatement();
                }
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexer.nextToken();
        }
    }
}
