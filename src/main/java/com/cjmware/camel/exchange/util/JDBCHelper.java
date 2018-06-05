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

package com.cjmware.camel.exchange.util;

import com.cjmware.camel.exchange.model.RestJsonResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.spi.Registry;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.text.StrSubstitutor;

import java.util.ArrayList;
import java.util.HashMap;


public class JDBCHelper {

    public static String jdbcResultsToJson(Message message) throws Exception{
        // Instantiate our new result list
        ArrayList<JsonObject> resultList = new ArrayList<>();
        String resultsAsJsonString = null;
        RestJsonResponse restJsonResponse = new RestJsonResponse();
        Gson gsonUtil = new Gson();

        if (message.getBody() instanceof ArrayList) {
            ArrayList fromBodyList = message.getBody(ArrayList.class);
            if (fromBodyList != null && fromBodyList.size() > 0) {
                Object bodyObjectFromList = fromBodyList.get(0);
                if (bodyObjectFromList != null && bodyObjectFromList instanceof HashMap) {

                    for (Object bodyObject : fromBodyList) {
                        HashMap<String, String> object = (HashMap) bodyObject;

                        String jsonString = object.get("ASJSON");
                        if(jsonString != null) {
                            JsonObject jsonObject = gsonUtil.fromJson(jsonString, JsonObject.class);
                            resultList.add(jsonObject);
                        }
                        else {
                            JsonObject jsonObject = gsonUtil.fromJson(gsonUtil.toJson(object), JsonObject.class);
                            resultList.add(jsonObject);
                        }
                    }
                    restJsonResponse.setStatus("200");
                    restJsonResponse.setMessage("Success");
                    restJsonResponse.setData(resultList);
                    resultsAsJsonString = gsonUtil.toJson(restJsonResponse);
                }
                else {
                    resultList.add(gsonUtil.fromJson(gsonUtil.toJson(bodyObjectFromList), JsonObject.class));
                }
            } else {
                restJsonResponse.setStatus("200");
                restJsonResponse.setMessage("Success");
                restJsonResponse.setData(message.getBody());
                resultsAsJsonString = gsonUtil.toJson(restJsonResponse);
            }
        }

        return resultsAsJsonString;
    }

    public static void initializeJDBCDataSource(String dataSourceName, CamelContext camelContext) throws Exception {
        // This is the format of the JDBC URL using property references
        String urlTemplate = "${jdbc.uri}//${jdbc.host}:${jdbc.port}/${jdbc.db}";

        // Translate the property references in value
        String jdbcUrl = StrSubstitutor.replaceSystemProperties(urlTemplate);
        System.setProperty("jdbc.url", jdbcUrl);

        // Create a basic datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(System.getProperty("jdbc.driver"));
        ds.setUsername(System.getProperty("jdbc.user"));
        ds.setPassword(System.getProperty("jdbc.password"));
        ds.setUrl(jdbcUrl);

        // Add the data source to our Camel Context registry to allow our Route definitions access
        Registry camelRegistry = camelContext.getRegistry();
        PropertyPlaceholderDelegateRegistry registry = (PropertyPlaceholderDelegateRegistry)camelRegistry;
        JndiRegistry jndiRegistry = (JndiRegistry)registry.getRegistry();
        jndiRegistry.bind(dataSourceName, ds);
    }
}
