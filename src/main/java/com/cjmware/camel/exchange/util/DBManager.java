/*
 * Copyright (c) 2018.
 * THE SOURCE CODE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cjmware.camel.exchange.util;

import com.cjmware.camel.exchange.db.H2PersistenceHandler;
import com.cjmware.camel.exchange.db.PersistenceHandler;
import com.cjmware.camel.exchange.model.InsertData;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DBManager {

    public static PersistenceHandler getInstance() {
        PersistenceHandler persistenceHandler = null;
        if(System.getProperty("db.implementation") != null &&
           System.getProperty("db.implementation").equalsIgnoreCase("jdbc")) {
            persistenceHandler = new H2PersistenceHandler();
        }
        return persistenceHandler;
    }

    public static PreparedStatement buildPrepareStatementSqlInsert(Connection conn, String tableName, Object dataObject) {
        PreparedStatement ps = null;
        InsertData insertData = new InsertData();
        // Assumption is that the object is a POJO with getters/setters

        Method[] methodsArray = dataObject.getClass().getMethods();
        List<Method> methodsList = Arrays.asList(methodsArray);

        for(Method method : methodsList) {
            if(method.getName().toLowerCase().startsWith("get") && !method.getName().toLowerCase().equalsIgnoreCase("getclass")) {
                String fieldName = method.getName().toLowerCase().substring(3);
                try {
                    Object value = method.invoke(dataObject);
                    if(value != null) {
                        insertData.getFieldNames().add(fieldName);
                        if(fieldName.toLowerCase().contains("date") && value instanceof Long)
                            insertData.getFieldValues().add(new Timestamp(new Long(String.valueOf(value))));
                        else
                            insertData.getFieldValues().add(value);
                    }
                }
                catch(Exception e) {
                    log.error("An error occured while processing data object methods: " +e.toString());
                }
            }
        }
        insertData.getFieldNames().add("asjson");
        insertData.getFieldValues().add(new Gson().toJson(dataObject));

        String insertSql = buildInsertStatment(tableName, insertData.getFieldNames());
        log.info(insertSql);
        int index = 1;
        try {
            ps = conn.prepareStatement(insertSql);
            for(Object fieldValue : insertData.getFieldValues()) {
                if(fieldValue instanceof  String) {
                    ps.setString(index, String.valueOf(fieldValue));

                }
                else if(fieldValue instanceof Long) {
                    ps.setLong(index, Long.parseLong(String.valueOf(fieldValue)));

                }
                else if(fieldValue instanceof Integer) {
                    ps.setInt(index, Integer.parseInt(String.valueOf(fieldValue)));

                }
                else if(fieldValue instanceof Timestamp) {
                    ps.setTimestamp(index, (Timestamp)fieldValue);

                }

                index++;
            }

        }
        catch(Exception e) {

        }



        return ps;
    }

    public static String buildInsertStatment(String tableName, List<String> fieldNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        for(String fieldName : fieldNames) {
            sb.append(fieldName);
            sb.append(", ");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(") ");
        sb.append("VALUES( ");
        for(String fieldName : fieldNames) {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(") ");

        return sb.toString();
    }

}
