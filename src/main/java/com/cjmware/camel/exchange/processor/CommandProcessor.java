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

import com.cjmware.camel.exchange.util.ConvertToPDFUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.exec.ExecBinding;
import org.apache.camel.component.exec.ExecResult;
import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandProcessor implements Processor {


    @Override
    public void process(Exchange exchange) throws Exception {

        Message message = exchange.getIn();

        String documentPath = message.getHeader(Exchange.FILE_PATH).toString();
        Integer instanceNumber = message.getHeader("consumer.instance.number", Integer.class);

        String sofficeBin = System.getProperty("sofficeBin");
        String outputFolderName = System.getProperty("fileOutputFolder").concat(ConvertToPDFUtil.getISODateFolderName());

        List<String> parameterList = new ArrayList<>();
        parameterList.add(" --nologo");
        parameterList.add(" --invisible");
        parameterList.add(" --headless");
        parameterList.add(" --nofirststartwizard");
        parameterList.add(" --norestore");

        // Need to tie this to thread instances instead of random number/folder
        parameterList.add(" -env:UserInstallation=file:///tmp/TCE_TO_PDF_LibreOffice_".concat(instanceNumber.toString()));

        parameterList.add(" --convert-to");
        parameterList.add(" pdf:writer_pdf_Export");
        parameterList.add(" --outdir ");
        parameterList.add(outputFolderName);
        parameterList.add(" ");
        parameterList.add(documentPath);

        message.setHeader(ExecBinding.EXEC_COMMAND_EXECUTABLE, sofficeBin);
        message.setHeader(ExecBinding.EXEC_COMMAND_ARGS, parameterList);


        String pdfFilePath = ConvertToPDFUtil.getConvertedFilePath(documentPath, outputFolderName,"pdf");
        message.setHeader("pdf.output.file.path", pdfFilePath);
        message.setHeader("doc.input.file.path", documentPath);

        Exchange commandExchange = exchange.copy();
        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
        producerTemplate.send("direct:execute-command", commandExchange);

        if(commandExchange.getException() != null) {
            log.error("Execution failed: " +commandExchange.getException().toString());
        }
        else {
            log.info("Execution return code was: " +commandExchange.getIn().getHeader(ExecBinding.EXEC_EXIT_VALUE, Integer.class));
            ExecResult execResult = commandExchange.getIn().getBody(ExecResult.class);
            if(execResult.getStdout() != null) {
                String consoleLog = IOUtils.toString(execResult.getStdout(), "UTF-8");
                log.info(consoleLog);
            }
            if(execResult.getStderr() != null) {
                String consoleLog = IOUtils.toString(execResult.getStderr(), "UTF-8");
                log.info(consoleLog);
            }
        }
    }
}
