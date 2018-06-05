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

import com.cjmware.camel.exchange.model.RestJsonResponse;
import com.cjmware.camel.exchange.util.JDBCHelper;
import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class QueryProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        RestJsonResponse restJsonResponse = new RestJsonResponse();
        String resultString = null;
        Message message = exchange.getIn();

        if(message.getHeader("query-exception") != null || message.getBody() == null) {
            Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
            restJsonResponse.setStatus("500");
            restJsonResponse.setMessage("An error occurred while querying:" +exception.getMessage());
            resultString = new Gson().toJson(restJsonResponse);
        }
        else {
            try {
                resultString = JDBCHelper.jdbcResultsToJson(message);
            } catch (Exception e) {
                restJsonResponse.setStatus("500");
                restJsonResponse.setMessage("An error occurred while querying: " + e.toString());
                resultString = new Gson().toJson(restJsonResponse);
            }
        }
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, restJsonResponse.getStatus());
        message.setBody(resultString);

        /*if(message.getBody() instanceof ArrayList) {
            ArrayList listFromBody = message.getBody(ArrayList.class);
            if(listFromBody != null && listFromBody.size() > 0 ) {
                Object obj = listFromBody.get(0);
                if(obj != null && obj instanceof HashMap) {
                    convert = true;
                }
            }
        }

        if(message.getHeader(Exchange.HTTP_URI) != null && convert == true) {
            String uri = message.getHeader(Exchange.HTTP_URI).toString();
            if(uri.endsWith("summary")) {
                Type t = new TypeToken<ArrayList<HashMap<String,Object>>>(){}.getType();
                String queryResultString = new Gson().toJson(message.getBody(), t);
                message.setBody(queryResultString);
            }
            else if(uri.contains("/byname")) {
                String resultString = JDBCHelper.jdbcResultsToJson(message);
                message.setBody(resultString);
                ArrayList<JsonObject> resultList = new ArrayList<>();
                ArrayList listFromBody = message.getBody(ArrayList.class);
                for(Object obj : listFromBody) {
                    HashMap<String, String> object = (HashMap) obj;
                    String jsonString = object.get("ASJSON");
                    JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
                    resultList.add(jsonObject);
                }
                Type t = new TypeToken<ArrayList<JsonObject>>(){}.getType();
                String queryResultString = new Gson().toJson(resultList, t);
                message.setBody(queryResultString);
            }
            else if(uri.contains("/bytype")) {
                ArrayList<JsonObject> resultList = new ArrayList<>();
                ArrayList listFromBody = message.getBody(ArrayList.class);
                for(Object obj : listFromBody) {
                    HashMap<String, String> object = (HashMap) obj;
                    String jsonString = object.get("ASJSON");
                    JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
                    resultList.add(jsonObject);
                }
                Type t = new TypeToken<ArrayList<JsonObject>>(){}.getType();
                String queryResultString = new Gson().toJson(resultList, t);
                message.setBody(queryResultString);
            }
        }
        */
    }
}
