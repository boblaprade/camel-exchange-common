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

package com.cjmware.camel.exchange;

import com.cjmware.camel.exchange.route.ProcessGenerator;
import com.cjmware.camel.exchange.route.rest.RESTServices;
import com.cjmware.camel.exchange.util.JDBCHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.main.Main;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class StartMeUp {

    private Main camelMain;

    public void init() {

        // Instantiate new instance of org.apache.camel.main.Main
        camelMain = new Main();
        CamelContext camelContext = null;

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File("./thecamelexchange.properties")));
            for(Map.Entry entry :properties.entrySet()) {
                System.setProperty(entry.getKey().toString(),entry.getValue().toString());
            }

            // Now start the environment so that we can have access to the Context object(s)
            camelMain.start();

            // Startup our ActiveMQ Environment
            startActiveMQBroker();


            // Get our Camel Context object so we can create our Process Flows (Routes)
            List<CamelContext> camelContextList = camelMain.getCamelContexts();
            if(camelContextList != null && camelContextList.size() > 0) {

                // We should have only one context at this point
                camelContext = camelContextList.get(0);

                // This creates a JDBC data source and stores it in the context for our JDBC routes
                JDBCHelper.initializeJDBCDataSource("documentDBSrc", camelContext);

                // This is our class that generates all of the Process Flows (Routes)
                ProcessGenerator processGenerator = new ProcessGenerator();
                processGenerator.initProcesses(camelContext);

                // This will create all of our REST service interface
                RESTServices restServices = new RESTServices();
                restServices.addRoutesToCamelContext(camelContext);

                // Now turn control back over to the Camel environment
                camelMain.run();


                // If we reach this code, then the Camel environment is shutting down
                log.info("Shutdown inside of StartMeUp: "+camelContext.getStatus());

                // OK, lets clean up after our Process Flows
                File tmpFolder = new File("/tmp");
                File[] files = tmpFolder.listFiles();
                List<File> fileList = Arrays.asList(files);

                for(File file : fileList) {
                    if(file.isDirectory() && file.getName().startsWith("TCE_TO_PDF_LibreOffice_")) {
                        log.info("Removing temporary folder: " +file.getName());
                        FileUtils.deleteDirectory(file);
                    }
                }
            }

        }
        catch(Exception e) {
            log.error("THUD! Failed to instantiate Apache Camel: " +e.toString());
        }
    }

    private void startActiveMQBroker() {

        // This will set the specific path where the ActiveMQ environment will write files to
        System.setProperty("org.apache.activemq.default.directory.prefix", System.getProperty("activemq.data.folder"));


        BrokerService broker = new BrokerService();
        broker.setBrokerName("CAMEL_EXCHANGE_BROKER");
        broker.setPersistent(true);

        try {
            broker.addConnector("tcp://localhost:61616");
            broker.start();
        }
        catch(Exception e) {
            log.error("THUD! Failed to instantiate ActiveMQ Broker: " +e.toString());
        }
    }

    public static void main(String[] args) {
        StartMeUp startMeUp = new StartMeUp();
        startMeUp.init();
    }
}
