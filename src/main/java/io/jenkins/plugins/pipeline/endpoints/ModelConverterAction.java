package io.jenkins.plugins.pipeline.endpoints;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.HttpResponses;
import io.jenkins.plugins.pipeline.models.PipelineModel;
import io.jenkins.plugins.pipeline.parsers.PipelineParser;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.Converter;
import org.jenkinsci.plugins.pipeline.modeldefinition.validator.ErrorCollector;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Endpoint for converting to/from JSON/Groovy and validating both.
 *
 * @author Ke Bei
 */
@Extension
public class ModelConverterAction implements RootAction {

    public static final String PSET_PIPELINE_CONVERTER_URL = "pset-pipeline-model-converter";

    @Override
    public String getUrlName() {
        return PSET_PIPELINE_CONVERTER_URL;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @RequirePOST
    public HttpResponse doValidateYaml(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);
        JSONObject result = new JSONObject();

        String yamlContent = req.getParameter("yaml");

        if (!StringUtils.isEmpty(yamlContent)){
            PipelineParser pipelineParser = new PipelineParser(yamlContent);
            Optional<PipelineModel> pipelineModel = pipelineParser.parse();
            if (pipelineModel.isPresent()){
                try {
                String jenkinsFileContent = pipelineModel.get().toPrettyGroovy();
                Converter.scriptToPipelineDef(jenkinsFileContent);
                result.accumulate("result", "success");
                } catch (Exception e) {
                    JSONObject jfErrors = new JSONObject();
                    reportFailure(jfErrors, e);
                    JSONArray errors = new JSONArray();
                    errors.add(new JSONObject().accumulate("error", jfErrors));
                    reportFailure(result, errors);
                }
            }

        } else {
            reportFailure(result, "No content found for 'yaml' parameter");
        }
        return HttpResponses.okJSON(result);
    }

    @RequirePOST
    public HttpResponse doValidateJson(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);

        JSONObject result = new JSONObject();

        String jsonAsString = req.getParameter("json");
        if (!StringUtils.isEmpty(jsonAsString)) {
            try {
                String yaml = convertJsonToYaml(jsonAsString);
                PipelineParser pipelineParser = new PipelineParser(yaml);
                Optional<PipelineModel> pipelineModel = pipelineParser.parse();
                if (pipelineModel.isPresent()){
                    try {
                        String jenkinsFileContent = pipelineModel.get().toPrettyGroovy();
                        Converter.scriptToPipelineDef(jenkinsFileContent);
                        result.accumulate("result", "success");
                    } catch (Exception e) {
                        JSONObject jfErrors = new JSONObject();
                        reportFailure(jfErrors, e);
                        JSONArray errors = new JSONArray();
                        errors.add(new JSONObject().accumulate("error", jfErrors));
                        reportFailure(result, errors);
                    }
                }
            } catch (Exception je) {
                reportFailure(result, je);
            }
        } else {
            reportFailure(result, "No content found for 'json' parameter");
        }

        return HttpResponses.okJSON(result);
    }

    @RequirePOST
    public HttpResponse doYamlToJson(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);
        JSONObject result = new JSONObject();

        String yamlJenkinsFileContent = req.getParameter("yaml");

        if (!StringUtils.isEmpty(yamlJenkinsFileContent)){
            try {
                String json = convertYamlToJson(yamlJenkinsFileContent);
                result.accumulate("result", "success");
                result.accumulate("json",json);
            } catch (Exception e) {
                JSONObject jfErrors = new JSONObject();
                reportFailure(jfErrors, e);
                JSONArray errors = new JSONArray();
                errors.add(new JSONObject().accumulate("error", jfErrors));
                reportFailure(result, errors);
            }
        } else {
            reportFailure(result, "No content found for 'yaml' parameter");
        }
        return HttpResponses.okJSON(result);
    }

    @RequirePOST
    public  HttpResponse doYamlToGroovy(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);
        JSONObject result = new JSONObject();

        String yamlJenkinsFileContent = req.getParameter("yaml");

        if (!StringUtils.isEmpty(yamlJenkinsFileContent)){
            PipelineParser pipelineParser = new PipelineParser(yamlJenkinsFileContent);
            Optional<PipelineModel> pipelineModel = pipelineParser.parse();
            if (pipelineModel.isPresent()){
                try {
                    String jenkinsFileContent = pipelineModel.get().toPrettyGroovy();
                    Converter.scriptToPipelineDef(jenkinsFileContent);
                    result.accumulate("result", "success");
                    result.accumulate("groovy",jenkinsFileContent);
                } catch (Exception e) {
                    JSONObject jfErrors = new JSONObject();
                    reportFailure(jfErrors, e);
                    JSONArray errors = new JSONArray();
                    errors.add(new JSONObject().accumulate("error", jfErrors));
                    reportFailure(result, errors);
                }
            }

        } else {
            reportFailure(result, "No content found for 'yaml' parameter");
        }
        return HttpResponses.okJSON(result);
    }

    @RequirePOST
    public HttpResponse doJsonToYaml(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);
        JSONObject result = new JSONObject();

        String json = req.getParameter("json");

        if (!StringUtils.isEmpty(json)){
                try {
                    String yamlContent = convertJsonToYaml(json);
                    result.accumulate("result", "success");
                    result.accumulate("yaml",yamlContent);
                } catch (Exception e) {
                    JSONObject jfErrors = new JSONObject();
                    reportFailure(jfErrors, e);
                    JSONArray errors = new JSONArray();
                    errors.add(new JSONObject().accumulate("error", jfErrors));
                    reportFailure(result, errors);
                }
        } else {
            reportFailure(result, "No content found for 'json' parameter");
        }
        return HttpResponses.okJSON(result);
    }

    @RequirePOST
    public HttpResponse doJsonToGroovy(StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.READ);
        JSONObject result = new JSONObject();

        String jsonAsString = req.getParameter("json");
        String yamlJenkinsFileContent = "";
        try {
            yamlJenkinsFileContent = convertJsonToYaml(jsonAsString);
        } catch (JsonProcessingException e) {
            JSONObject jfErrors = new JSONObject();
            reportFailure(jfErrors, e);
            JSONArray errors = new JSONArray();
            errors.add(new JSONObject().accumulate("error", jfErrors));
            reportFailure(result, errors);
        }

        if (!StringUtils.isEmpty(yamlJenkinsFileContent)){
            PipelineParser pipelineParser = new PipelineParser(yamlJenkinsFileContent);
            Optional<PipelineModel> pipelineModel = pipelineParser.parse();
            if (pipelineModel.isPresent()){
                try {
                    String jenkinsFileContent = pipelineModel.get().toPrettyGroovy();
                    Converter.scriptToPipelineDef(jenkinsFileContent);
                    result.accumulate("result", "success");
                    result.accumulate("groovy",jenkinsFileContent);
                } catch (Exception e) {
                    JSONObject jfErrors = new JSONObject();
                    reportFailure(jfErrors, e);
                    JSONArray errors = new JSONArray();
                    errors.add(new JSONObject().accumulate("error", jfErrors));
                    reportFailure(result, errors);
                }
            }

        } else {
            reportFailure(result, "No content found for 'json' parameter");
        }
        return HttpResponses.okJSON(result);
    }


    /**
     * Report result to be a failure message due to the given exception.
     *
     * @param result the result to mutate
     * @param e      the exception to report
     */
    private void reportFailure(JSONObject result, Exception e) {
        JSONArray errors = new JSONArray();
        JSONObject j = new JSONObject();

        if (e instanceof MultipleCompilationErrorsException) {
            MultipleCompilationErrorsException ce = (MultipleCompilationErrorsException)e;
            for (Object o : ce.getErrorCollector().getErrors()) {
                if (o instanceof SyntaxErrorMessage) {
                    j.accumulate("error", ((SyntaxErrorMessage)o).getCause().getMessage());
                }
            }
        } else {
            j.accumulate("error", e.getMessage());
        }
        errors.add(j);
        reportFailure(result, errors);
    }

    /**
     * Report result to be a failure message due to the given error message.
     *
     * @param result the result to mutate
     * @param message the error
     */
    private void reportFailure(JSONObject result, String message) {
        JSONArray errors = new JSONArray();
        JSONObject o = new JSONObject();
        o.accumulate("error", message);
        errors.add(o);
        reportFailure(result, errors);
    }

    /**
     * Report result to be a failure message due to the given error messages.
     *
     * @param result the result to mutate
     * @param errors the errors
     */
    private void reportFailure(JSONObject result, JSONArray errors) {
        result.accumulate("result", "failure");
        result.accumulate("errors", errors);
    }

    /**
     * convert json to yaml
     * @param json
     * @return yaml
     * @throws JsonProcessingException
     */
    private String convertJsonToYaml(String json) throws JsonProcessingException {
        // parse JSON
        JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
        // save it as YAML
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    /**
     * convert yaml to json
     * @param yaml
     * @return json
     * @throws JsonProcessingException
     */
    private static String convertYamlToJson(String yaml) throws JsonProcessingException {

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);

    }

    private boolean collectErrors(JSONObject result, ErrorCollector errorCollector) {
        if (errorCollector.getErrorCount() > 0) {
            JSONArray errors = errorCollector.asJson();
            reportFailure(result, errors);
            return true;
        }
        return false;
    }
    @Extension
    public static class ModelConverterActionCrumbExclusion extends CrumbExclusion {
        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws IOException, ServletException {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/" + PSET_PIPELINE_CONVERTER_URL + "/")) {
                chain.doFilter(req, resp);
                return true;
            }

            return false;
        }
    }
}
