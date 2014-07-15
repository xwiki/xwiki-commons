package org.xwiki.tool.enforcer;

import java.io.File;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

/**
 * This enforcer checks if the javadoc.jar file exists for project with jar packaging.
 * 
 * @version $Id$
 * @since 6.0-rc-1
 */

public class JavadocCheck implements EnforcerRule
{

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        MavenProject project = getMavenProject(helper);

        if ("jar".equals(project.getPackaging())
            && !new File(project.getBuild().getDirectory() + File.separator + project.getBuild().getFinalName()
                + "-javadoc.jar").exists()) {
            throw new EnforcerRuleException("Missing Javadoc JAR");
        }

    }

    private MavenProject getMavenProject(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        try {
            return (MavenProject) helper.evaluate("${project}");
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Failed to get maven project", e);
        }

    }

    public String getCacheId()
    {
        return "";
    }

    public boolean isCacheable()
    {
        return false;
    }

    public boolean isResultValid(EnforcerRule arg0)
    {
        return false;
    }

}