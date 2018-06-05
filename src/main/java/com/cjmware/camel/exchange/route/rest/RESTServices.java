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

package com.cjmware.camel.exchange.route.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;

public class RESTServices extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        System.setProperty("org.eclipse.jetty.ssl.keystore", "/home/bob/IdeaProjects/member-auth/security/keystore.jks");
        System.setProperty("org.eclipse.jetty.ssl.password", "password");
        System.setProperty("org.eclipse.jetty.ssl.keypassword", "password");


        RestConfigurationDefinition rest = restConfiguration().component("jetty").host("localhost")
                .port(9008).scheme("https").bindingMode(RestBindingMode.auto);


        rest("/api/v1/document_index/").id("document-index")
                .get("summary").id("document-index-summary").to("direct:summary-query")
                .get("byname/{namePattern}").id("document-index-byname").to("direct:byname-query")
                .get("bytype/{documentType}").id("document-index-bytype").to("direct:bytype-query");
    }
}
