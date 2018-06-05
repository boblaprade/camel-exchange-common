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

package com.cjmware.camel.exchange.db;

import com.cjmware.camel.exchange.model.DocumentIndex;
import com.cjmware.camel.exchange.model.DocumentIndexSummary;
import com.cjmware.camel.exchange.util.DBManager;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class H2PersistenceHandler implements PersistenceHandler {

    public void storeDocumentIndex(DocumentIndex documentIndex) throws Exception {

        Connection conn = null;
        try {
            conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));

            PreparedStatement ps = DBManager.buildPrepareStatementSqlInsert(conn, "DOCUMENT_INDEX", documentIndex);
            ps.execute();
            conn.commit();
        }
        catch(Exception e) {
            log.error("There was an error encountered while inserting a document index: " + e.toString());
            throw e;
        }
        finally {
            try {conn.close();}catch(Exception e){}
        }
    }

    public Long recordCount(String tableName) throws Exception {
        Long count = 0l;
        Connection conn = null;
        try {
            conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));
            ResultSet rs = conn.createStatement().executeQuery("Select count(*) from " +tableName);
            if(rs != null) {
                rs.next();
                count = rs.getLong(1);
            }
        }
        catch(Exception e) {
            e.toString();
        }
        finally {
            try{conn.close();}catch(Exception e){};
        }

        return count;
    }

    public List<DocumentIndex> getDocumentListByName(String namePattern) throws Exception {
        List<DocumentIndex> documentIndexList = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));
            StringBuilder sb = new StringBuilder();
            sb.append("Select asJson from DOCUMENT_INDEX where filename like ?");

            PreparedStatement ps = conn.prepareStatement(sb.toString());

            if(namePattern.startsWith("*")) {
                namePattern = "%" +namePattern.substring(1);
            }
            if(namePattern.endsWith("*")) {
                namePattern = namePattern.substring(0, namePattern.length()-1) +"%";
            }

            ps.setString(1, namePattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String json = rs.getString(1);
                DocumentIndex documentIndex = new Gson().fromJson(json, DocumentIndex.class);
                documentIndexList.add(documentIndex);
            }

        }
        catch(Exception e) {
            e.toString();
        }
        finally {
            try{conn.close();}catch(Exception e){};
        }

        return documentIndexList;
    }

    public List<DocumentIndex> getDocumentListByType(String orignialFileType) throws Exception {
        List<DocumentIndex> documentIndexList = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));
            StringBuilder sb = new StringBuilder();
            sb.append("Select asJson from DOCUMENT_INDEX where originalFileType = ?");

            PreparedStatement ps = conn.prepareStatement(sb.toString());
            ps.setString(1, orignialFileType);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String json = rs.getString(1);
                DocumentIndex documentIndex = new Gson().fromJson(json, DocumentIndex.class);
                documentIndexList.add(documentIndex);
            }

        }
        catch(Exception e) {
            e.toString();
        }
        finally {
            try{conn.close();}catch(Exception e){};
        }

        return documentIndexList;
    }

    public List<DocumentIndexSummary> getDocumentListSummary() throws Exception {
        List<DocumentIndexSummary> documentIndexSummaryList = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DriverManager.
                    getConnection(System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password"));
            StringBuilder sb = new StringBuilder();
            sb.append("Select originalFileType, count(*), sum(fileSize) from DOCUMENT_INDEX group by originalfiletype");

            PreparedStatement ps = conn.prepareStatement(sb.toString());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DocumentIndexSummary documentIndexSummary = new DocumentIndexSummary();
                documentIndexSummary.setOriginalFileType(rs.getString(1));
                documentIndexSummary.setCount(rs.getLong(2));
                documentIndexSummary.setSpaceUsed(rs.getLong(3));
                documentIndexSummaryList.add(documentIndexSummary);
            }

        }
        catch(Exception e) {
            e.toString();
        }
        finally {
            try{conn.close();}catch(Exception e){};
        }

        return documentIndexSummaryList;
    }

}
