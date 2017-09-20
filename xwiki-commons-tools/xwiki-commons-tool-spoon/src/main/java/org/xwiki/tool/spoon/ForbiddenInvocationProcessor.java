package org.xwiki.tool.spoon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;

public class ForbiddenInvocationProcessor extends AbstractProcessor<CtInvocation<?>>
{
    // FIXME: replace this by proper configuration when supported by the Spoon Maven plugin, see
    // https://github.com/INRIA/spoon/issues/1537
    private static final Map<String, Set<String>> METHODS = new HashMap<>();

    static {
        METHODS.put("java.io.File", Collections.singleton("deleteOnExit"));
    }

    @Override
    public void process(CtInvocation<?> element)
    {
        CtExpression<?> target = element.getTarget();

        if (target != null) {
            String type = target.getType().getQualifiedName();
            Set<String> methods = METHODS.get(type);
            if (methods != null) {
                String method = element.getExecutable().getSimpleName();
                if (methods.contains(method)) {
                    getFactory().getEnvironment().report(this, Level.ERROR, element,
                        "Forbidden call to " + type + "#" + method);

                    // Forcing the build to stop
                    // FIXME: Remove that when https://github.com/INRIA/spoon/issues/1534 is implemented
                    throw new RuntimeException("Forbidden call to " + type + "#" + method);
                }
            }
        }
    }
}
