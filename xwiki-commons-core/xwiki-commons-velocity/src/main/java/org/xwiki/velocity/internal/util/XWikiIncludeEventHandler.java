package org.xwiki.velocity.internal.util;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.event.IncludeEventHandler;

public class XWikiIncludeEventHandler implements IncludeEventHandler {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      XWikiIncludeEventHandler.class);

  public String includeEvent(String includeResourcePath, String currentResourcePath,
      String directiveName) {
    mLogger.trace("velocity include event: [" + includeResourcePath + "], ["
        + currentResourcePath + "], [" + directiveName + "]");
    String template = URI.create("/templates/" + includeResourcePath).normalize(
        ).toString();
    if (!template.startsWith("/templates/")) {
        mLogger.warn("Illegal access, tried to use file [" + template
            + "] as a template. Possible break-in attempt!");
        return null;
    }
    return template;
  }

}
