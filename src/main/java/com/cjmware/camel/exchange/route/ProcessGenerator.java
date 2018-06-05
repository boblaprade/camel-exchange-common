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

package com.cjmware.camel.exchange.route;

import com.cjmware.camel.exchange.processor.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
public class ProcessGenerator {


    public void initProcesses(CamelContext camelContext) {

        // Using Route Definition to build a Route
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {


                // One time event to indicate that the object has initiated
                from("timer://startup?repeatCount=1").log("The Camel Exchange processor has been initialized");

                // One time event to indicate that the object has initiated
                from("timer://startup?repeatCount=1")
                        .process(new DatabaseProcessor())
                        .log("The Camel Exchange database processor has been initialized");


                /*
                // Example for applying a file name filter
                RouteDefinition routeDefinition = from("file:///home/bob/TCE/input?preMove=processing&antInclude=.docx")
                        .id("Folder Watcher DOCx");

                routeDefinition.to("log://FolderWatcher?showHeaders=true");
                routeDefinition.process(new CommandProcessor());
                routeDefinition.process(new PDFMetadataProcessor());
                routeDefinition.log("Process Sequence has Completed");

                // Example showing the use of Threads
                Object obj = from("file:///home/bob/TCE/input?preMove=processing&antInclude=*.odt,*.docx")
                        .id("Threaded Folder Watcher").threads(2)
                        .to("log://ThreadedFolderWatcher?showHeaders=true")
                        .process(new CommandProcessor())
                        .process(new PDFMetadataProcessor());

                */

                // General Purpose Command Execution Route. Executable abd parameters are provided as
                // Exchange.Message.Headers
                from("direct:execute-command").id("Execute Command")
                        .to("exec://nop");

                // Folder Watcher that writes a Message to a JMS Queue
                from("file://" +System.getProperty("fileInputFolder")+"?preMove=processing&move=queued").id("Folder Watcher")
                        .process(new PrepareRequest())
                        .to("activemq:CONVERT_TO_PDF");

                // Our main Process Flow that processes Messages from the JMS Queue
                from("activemq:CONVERT_TO_PDF?concurrentConsumers=4").id("CONVERT_TO_PDF")
                        .to("log://message_queue?showHeaders=true")
                        .process(new CommandProcessor())
                        .process(new PDFMetadataProcessor())
                        .process(new DocumentIndexProcessor())
                        .log("Process Sequence has Completed");


                // Summary query using the JDBC Camel component
                from("direct:summary-query").id("Summary Query")
                        .setBody(simple("Select originalFileType, count(*) as count, sum(fileSize) as spaceUsed from DOCUMENT_INDEX group by originalfiletype"))
                        .doTry()
                            .to("jdbc://documentDBSrc")
                        .doCatch(Exception.class)
                            .setHeader("query-exception", simple("true"))
                        .doFinally()
                            .process(new QueryProcessor()) // Reformats the query results to true JSON
                            .to("log:summary-output?showBody=true&showHeaders=true");

                // Document Type query using the JDBC Camel component
                from("direct:bytype-query").id("By Type Query")
                        .setBody(simple("Select * from DOCUMENT_INDEX where originalFileType = '${header.documentType}'"))
                        .doTry()
                            .to("jdbc://documentDBSrc")
                        .doCatch(Exception.class)
                            .setHeader("query-exception", simple("true"))
                        .doFinally()
                            .process(new QueryProcessor())
                            .to("log:bytype-output?showBody=true&showHeaders=true");

                // Document Name query using the JDBC Camel component
                from("direct:byname-query").id("By Name Query")
                        .setBody(simple("Select asJson from DOCUMENT_INDEX where filename like '%${header.namePattern}%'"))
                        .to("log:byname-input?showBody=true&showHeaders=true")
                        .doTry()
                            .to("jdbc://documentDBSrc")
                        .doCatch(Exception.class)
                            .setHeader("query-exception", simple("true"))
                        .doFinally()
                            .process(new QueryProcessor())
                            .to("log:byname-output?showBody=true&showHeaders=true");


                // Test route for our JDBC routes
                from("timer://startup?repeatCount=1").id("query-test")
                        .setHeader("namePattern", simple("two"))
                        .to("direct:byname-query")
                        .log("The query completed, see log for results");



            }
        };

        try {
            routeBuilder.addRoutesToCamelContext(camelContext);
        }
        catch(Exception e) {
            log.error("THUD! Failed to instantiate Camel Routes: " + e.toString());
        }
    }
}
