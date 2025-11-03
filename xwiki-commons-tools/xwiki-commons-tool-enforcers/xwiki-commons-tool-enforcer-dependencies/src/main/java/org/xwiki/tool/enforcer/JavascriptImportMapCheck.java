package org.xwiki.tool.enforcer;

import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verify if the {@code "xwiki.javascript.modules.importmap"} property is wellformed.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Named("javascriptImportMapCheck")
public class JavascriptImportMapCheck extends AbstractPomCheck
{
    private static final String XWIKI_JAVASCRIPT_MODULES_IMPORTMAP = "xwiki.javascript.modules.importmap";

    @Override
    public void execute() throws EnforcerRuleException
    {
        Model model = getResolvedModel();
        var property = model.getProperties().getProperty(XWIKI_JAVASCRIPT_MODULES_IMPORTMAP);
        Object jsonObject;
        try {
            jsonObject = new ObjectMapper().readValue(property, Object.class);
        } catch (JsonProcessingException e) {
            throw new EnforcerRuleException(
                "Failed to parse the [%s] property".formatted(XWIKI_JAVASCRIPT_MODULES_IMPORTMAP),
                e);
        }
        if (jsonObject instanceof Map jsonMap) {
            for (Map.Entry o : (Set<Map.Entry>) jsonMap.entrySet()) {
                var key = String.valueOf(o.getKey());
                var value = String.valueOf(o.getValue());
                var splits = value.split("/", 2);
                for (Dependency dependency : model.getDependencies()) {
                    getLog().debug("Checking dependency [%s]".formatted(dependency));
                }
            }
        } else {
            throw new EnforcerRuleException(
                "Was expecting a JSON object for the [%s] property".formatted(XWIKI_JAVASCRIPT_MODULES_IMPORTMAP));
        }
    }
}
