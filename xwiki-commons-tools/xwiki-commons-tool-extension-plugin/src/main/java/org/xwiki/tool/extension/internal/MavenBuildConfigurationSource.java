package org.xwiki.tool.extension.internal;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.internal.MemoryConfigurationSource;

/**
 * {@link ConfigurationSource} designed to be used during a Maven build.
 * 
 * @version $Id$
 * @since 9.5RC1
 */
@Component
@Singleton
public class MavenBuildConfigurationSource extends MemoryConfigurationSource
{
    
}
