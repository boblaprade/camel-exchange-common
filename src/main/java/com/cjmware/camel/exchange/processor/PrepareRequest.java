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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.io.File;

public class PrepareRequest implements Processor {

    private static Integer instanceNumber = 1;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();

        String documentPath = message.getHeader(Exchange.FILE_PATH).toString();
        File documentFile = new File(documentPath);

        StringBuilder sb = new StringBuilder()
            .append(documentFile.getParent())
            .append(File.separatorChar)
            .append("queued")
            .append(File.separatorChar)
            .append(documentFile.getName());

        message.setHeader(Exchange.FILE_PATH,sb.toString());
        getNextInstanceNumber();
        message.setHeader("consumer.instance.number", instanceNumber);
    }

    private void getNextInstanceNumber() {
        this.instanceNumber++;
        if(instanceNumber > 3)
            instanceNumber = 0;
    }
}
