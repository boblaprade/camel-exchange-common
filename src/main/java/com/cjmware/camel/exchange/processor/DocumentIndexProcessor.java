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

import com.cjmware.camel.exchange.db.PersistenceHandler;
import com.cjmware.camel.exchange.util.DBManager;
import com.cjmware.camel.exchange.model.DocumentIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class DocumentIndexProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        // Obtain out Message object from the exchange
        Message message = exchange.getIn();

        // Get the String path to the PDF File
        String pdfFilePath = message.getHeader("pdf.output.file.path").toString();
        File pdfFile = new File(pdfFilePath);

        // Get the String path to the original file
        String documentPath = message.getHeader("doc.input.file.path").toString();
        File documentFile = new File(documentPath);

        // Populate the DocumentIndex object
        DocumentIndex documentIndex = new DocumentIndex();
        documentIndex.setOriginalFileType(FilenameUtils.getExtension(documentFile.getName()));
        documentIndex.setOriginalFileName(documentFile.getName());
        documentIndex.setOriginalFileSize(documentFile.length());

        documentIndex.setFileSystemPath(pdfFilePath);
        documentIndex.setFileName(pdfFile.getName());
        documentIndex.setFileSize(pdfFile.length());

        documentIndex.setProcessingDate(new Date().getTime());
        documentIndex.setIdentifier(UUID.randomUUID().toString());

        try {
            // Store the DocumentIndex object
            PersistenceHandler persistenceHandler = DBManager.getInstance();
            persistenceHandler.storeDocumentIndex(documentIndex);
        }
        catch(Exception e) {
            log.error("DocumentIndexProcessor failed to store document index: " +e.toString());
        }

    }
}

