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
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;

@Slf4j
public class PDFMetadataProcessor implements Processor {


    @Override
    public void process(Exchange exchange) throws Exception {

        // Obtain out Message object from the exchange
        Message message = exchange.getIn();

        // Get the String path to the PDF File
        String pdfFilePath = message.getHeader("pdf.output.file.path").toString();
        File pdfFile = new File(pdfFilePath);

        try {
            // Load the PDF from the file
            PDDocument pdDocument = PDDocument.load(pdfFile, MemoryUsageSetting.setupTempFileOnly());

            PDDocumentInformation info = pdDocument.getDocumentInformation();
            info.setSubject("The Camel Exchange - Convert to PDF");
            info.setKeywords("processIndentifier:"+ exchange.getExchangeId());

            pdDocument.save(pdfFile);
            pdDocument.close();
        }
        catch(Exception e) {
            log.error(e.toString());
        }
    }
}
