package io.jenkins.plugins.pipeline.endpoints;


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
    public static final String PIPELINE_CONVERTER_URL = "kebei-pipeline-model-converter";

    @Override
    public String getUrlName() {
        return PIPELINE_CONVERTER_URL;
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

        String yamlJenkinsFileContent = req.getParameter("yaml");

        if (!StringUtils.isEmpty(yamlJenkinsFileContent)){
            PipelineParser pipelineParser = new PipelineParser(yamlJenkinsFileContent);
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
                    errors.add(new JSONObject().accumulate("psetfileErrors", jfErrors));
                    reportFailure(result, errors);
                }
            }

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

    @Extension
    public static class ModelConverterActionCrumbExclusion extends CrumbExclusion {
        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws IOException, ServletException {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/" + PIPELINE_CONVERTER_URL + "/")) {
                chain.doFilter(req, resp);
                return true;
            }

            return false;
        }
    }
}
