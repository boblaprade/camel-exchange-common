import com.cjmware.camel.exchange.db.PersistenceHandler;
import com.cjmware.camel.exchange.util.DBManager;
import com.cjmware.camel.exchange.model.DocumentIndex;
import com.cjmware.camel.exchange.model.DocumentIndexSummary;

import java.util.Date;
import java.util.List;

public class TestStoreIndex {

    public static void main(String[] args) {
        TestStoreIndex testStoreIndex = new TestStoreIndex();

        testStoreIndex.runTest();

    }

    public void runTest() {

        PersistenceHandler persistenceHandler = DBManager.getInstance();

        DocumentIndex documentIndex = new DocumentIndex();
        documentIndex.setFileName("bob.odt");
        documentIndex.setFileSize(325l);
        documentIndex.setFileSystemPath("/tmp/bob.pdf");
        documentIndex.setIdentifier("rumpf23");
        documentIndex.setOriginalFileSize(400l);
        documentIndex.setOriginalFileType("odt");
        documentIndex.setOriginalFileName("bob.odt");
        documentIndex.setProcessingDate(new Date().getTime());

        try {
            //persistenceHandler.storeDocumentIndex(documentIndex);

            Long count = persistenceHandler.recordCount("document_index");
            System.out.println("Total rows in table: " + count);

            List<DocumentIndex> documentIndexList = persistenceHandler.getDocumentListByType("docx");

            documentIndexList = persistenceHandler.getDocumentListByName("%seven%");

            System.out.println("Result size for: " + documentIndexList.size());

            List<DocumentIndexSummary> documentIndexSummaryList = persistenceHandler.getDocumentListSummary();
        }
        catch(Exception e)  {
            System.out.print(e.toString());
        }

    }
}
