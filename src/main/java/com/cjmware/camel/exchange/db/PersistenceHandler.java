package com.cjmware.camel.exchange.db;

import com.cjmware.camel.exchange.model.DocumentIndex;
import com.cjmware.camel.exchange.model.DocumentIndexSummary;

import java.util.List;

public interface PersistenceHandler {
    public void storeDocumentIndex(DocumentIndex documentIndex) throws Exception;
    public Long recordCount(String tableName) throws Exception;
    public List<DocumentIndex> getDocumentListByName(String namePattern) throws Exception;
    public List<DocumentIndex> getDocumentListByType(String orignialFileType) throws Exception;
    public List<DocumentIndexSummary> getDocumentListSummary() throws Exception;
}
