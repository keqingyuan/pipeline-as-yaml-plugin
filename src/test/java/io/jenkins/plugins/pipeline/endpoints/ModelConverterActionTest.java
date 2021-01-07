package io.jenkins.plugins.pipeline.endpoints;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.Messages;
import org.jenkinsci.plugins.pipeline.modeldefinition.model.BuildCondition;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * 柯北在 2021/1/4 日创建。
 */
public class ModelConverterActionTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    public void testJsonToGroovy() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/jsonToGroovy"), HttpMethod.POST);
        String simpleYaml = fileContentsFromResources("json/pipelineAllinOne.json");

        assertNotNull(simpleYaml);

        NameValuePair pair = new NameValuePair("json", simpleYaml);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a success - " + result.toString(2), "success", resultData.getString("result"));
        assertTrue(resultData.getString("groovy"),true);
        System.out.println(resultData.getString("groovy"));
    }
    @Test
    public void testYamlToGroovy() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/yamlToGroovy"), HttpMethod.POST);
        String simpleYaml = fileContentsFromResources("pipeline/pipelineAllinOne2.yml");

        assertNotNull(simpleYaml);

        NameValuePair pair = new NameValuePair("yaml", simpleYaml);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a success - " + result.toString(2), "success", resultData.getString("result"));
        assertTrue(resultData.getString("groovy"),true);
        System.out.println(resultData.getString("groovy"));
    }
    @Test
    public void testJsonToYaml() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/jsonToYaml"), HttpMethod.POST);
        String simpleJson = fileContentsFromResources("json/pipelineAllinOne.json");
        String simpleYaml = fileContentsFromResources("pipeline/pipelineAllinOne.yml");

        assertNotNull(simpleJson);

        NameValuePair pair = new NameValuePair("json", simpleJson);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a success - " + result.toString(2), "success", resultData.getString("result"));
        assertTrue(resultData.getString("yaml"),true);
        System.out.println(resultData.getString("yaml"));
        assertEquals(simpleYaml,resultData.getString("yaml"));
    }
    @Test
    public void testYamlToJson() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/yamlToJson"), HttpMethod.POST);
        String simpleYaml = fileContentsFromResources("pipeline/pipelineAllinOne.yml");

        assertNotNull(simpleYaml);

        NameValuePair pair = new NameValuePair("yaml", simpleYaml);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a success - " + result.toString(2), "success", resultData.getString("result"));
        assertTrue(resultData.getString("json"),true);
        System.out.println(resultData.getString("json"));
    }

    @Test
    public void testValidateYaml() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/validateYaml"), HttpMethod.POST);
        String simpleYaml = fileContentsFromResources("pipeline/pipelineAllinOne2.yml");

        assertNotNull(simpleYaml);

        NameValuePair pair = new NameValuePair("yaml", simpleYaml);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        // TODO: Change this when we get proper JSON errors causing HTTP error codes
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a failure - " + result.toString(2), "failure", resultData.getString("result"));

        String expectedError = Messages.ModelValidatorImpl_InvalidBuildCondition("changed", BuildCondition.getOrderedConditionNames());
        assertTrue("Errors array (" + resultData.getJSONArray("errors").toString(2) + ") didn't contain expected error '" + expectedError + "'",
                foundExpectedErrorInJSON(resultData.getJSONArray("errors"), expectedError));
    }

    @Test
    public void testValidateJson() throws IOException {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/validateJson"), HttpMethod.POST);
        String simpleJson = fileContentsFromResources("json/pipelineAllinOne.json");

        assertNotNull(simpleJson);

        NameValuePair pair = new NameValuePair("json", simpleJson);
        req.setRequestParameters(Collections.singletonList(pair));

        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        // TODO: Change this when we get proper JSON errors causing HTTP error codes
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a success - " + result.toString(2), "success", resultData.getString("result"));

        String expectedError = Messages.ModelValidatorImpl_InvalidBuildCondition("changed", BuildCondition.getOrderedConditionNames());
        assertTrue("Errors array (" + resultData.getJSONArray("errors").toString(2) + ") didn't contain expected error '" + expectedError + "'",
                foundExpectedErrorInJSON(resultData.getJSONArray("errors"), expectedError));
    }
    @Test
    public void doValidateYaml() throws Exception {
        getExpectedErrorNoParam("yaml", "validateYaml");
    }

    @Test
    public void doValidateJson() throws Exception {
        getExpectedErrorNoParam("json", "validateJson");
    }

    @Test
    public void doYamlToJson() throws Exception {
        getExpectedErrorNoParam("yaml", "yamlToJson");
    }

    @Test
    public void doYamlToGroovy() throws Exception {
        getExpectedErrorNoParam("yaml", "yamlToGroovy");
    }

    @Test
    public void doJsonToYaml() throws Exception {
        getExpectedErrorNoParam("json", "jsonToYaml");
    }

    @Test
    public void doJsonToGroovy() throws Exception {
        getExpectedErrorNoParam("json", "jsonToGroovy");
    }

    private void getExpectedErrorNoParam(String param, String endpoint) throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest req = new WebRequest(new URL(wc.getContextPath() + ModelConverterAction.PSET_PIPELINE_CONVERTER_URL + "/" + endpoint), HttpMethod.POST);
        String rawResult = wc.getPage(req).getWebResponse().getContentAsString();
        assertNotNull(rawResult);

        JSONObject result = JSONObject.fromObject(rawResult);
        // TODO: Change this when we get proper JSON errors causing HTTP error codes
        assertEquals("Full result doesn't include status - " + result.toString(2), "ok", result.getString("status"));
        JSONObject resultData = result.getJSONObject("data");
        assertNotNull(resultData);
        assertEquals("Result wasn't a failure - " + result.toString(2), "failure", resultData.getString("result"));

        String expectedError = "No content found for '" + param + "' parameter";
        assertTrue("Errors array (" + resultData.getJSONArray("errors").toString(2) + ") didn't contain expected error '" + expectedError + "'",
                foundExpectedErrorInJSON(resultData.getJSONArray("errors"), expectedError));

    }

    protected boolean foundExpectedErrorInJSON(JSONArray errors, String expectedError) {
        for (Object e : errors) {
            if (e instanceof JSONObject) {
                JSONObject o = (JSONObject) e;
                if (o.getString("error").equals(expectedError)) {
                    return true;
                } else if (o.getString("error").contains(expectedError)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected String fileContentsFromResources(String fileName) throws IOException {
        return fileContentsFromResources(fileName, false);
    }

    protected String fileContentsFromResources(String fileName, boolean swallowError) throws IOException {
        String fileContents = null;

        URL url = getClass().getResource("/" + fileName);
        if (url != null) {
            fileContents = IOUtils.toString(url);
        }

        if (!swallowError) {
            assertNotNull("No file contents for file " + fileName, fileContents);
        } else {
            assumeTrue(fileContents != null);
        }
        return fileContents;

    }
}