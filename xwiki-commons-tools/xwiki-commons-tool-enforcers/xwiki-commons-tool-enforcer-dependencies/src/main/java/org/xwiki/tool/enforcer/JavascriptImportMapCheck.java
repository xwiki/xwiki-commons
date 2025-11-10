package org.xwiki.tool.enforcer;

import java.util.Map;

import javax.inject.Named;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Model;
import org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapException;
import org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapParser;
import org.xwiki.webjars.WebjarDescriptor;

import static org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapParser.JAVASCRIPT_IMPORTMAP_PROPERTY;

/**
 * Verify if the {@code "xwiki.javascript.modules.importmap"} property is wellformed.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Named("javascriptImportMapCheck")
public class JavascriptImportMapCheck extends AbstractPomCheck
{
    @Override
    public void execute() throws EnforcerRuleException
    {
        Model model = getResolvedModel();
        var property = model.getProperties().getProperty(JAVASCRIPT_IMPORTMAP_PROPERTY);
        if (property == null) {
            return;
        }
        try {
            var importMap = new JavascriptImportmapParser().parse(property);
            for (Map.Entry<String, WebjarDescriptor> entry : importMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                getLog().debug("Checking key [%s] for webjar reference [%s]".formatted(key, value));
                model.getDependencies();
            }
        } catch (JavascriptImportmapException e) {
            throw new EnforcerRuleException(
                "Failed to parse the [%s] property".formatted(JAVASCRIPT_IMPORTMAP_PROPERTY), e);
        }
    }
}
