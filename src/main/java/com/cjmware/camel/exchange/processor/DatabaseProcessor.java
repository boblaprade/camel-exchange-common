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

package com.cjmware.camel.exchange.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class DatabaseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        try {
            Server.createTcpServer("-tcpPort", System.getProperty("jdbc.port"), "-tcpAllowOthers").start();

            Connection conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));

            createTables(conn);
            conn.close();
        }
        catch(Exception e) {
            log.error("An error occurred while starting up the database engine");
        }
    }

    private void createTables(Connection conn) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS DOCUMENT_INDEX");
            sb.append("(");
            sb.append("identifier varchar(64) primary key,");
            sb.append("filename varchar(1024),");
            sb.append("filesize int,");
            sb.append("filesystempath varchar(1024),");
            sb.append("originalfiletype varchar(128),");
            sb.append("originalfilesize int,");
            sb.append("originalfilename varchar(1024),");
            sb.append("processingdate timestamp,");
            sb.append("asJson varchar(2048)");
            sb.append(")");

            conn.createStatement().execute(sb.toString());
            conn.close();
        }
        catch(Exception e) {
            log.error("An error occurred while trying to validate tables: " +e.toString());
        }
    }
}
