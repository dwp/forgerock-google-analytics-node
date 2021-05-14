package org.forgerock.openam.auth.nodes;

import static java.util.Arrays.asList;

import javax.inject.Inject;

import org.forgerock.openam.auth.node.api.AbstractNodeAmPlugin;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.plugins.PluginException;
import org.forgerock.openam.sm.AnnotatedServiceRegistry;

/**
 * Core nodes installed by default with no engine dependencies.
 */
public class GoogleAnalyticsNodePlugin extends AbstractNodeAmPlugin {

    private final AnnotatedServiceRegistry serviceRegistry;

    /**
     * DI-enabled constructor.
     * @param serviceRegistry A service registry instance.
     */
    @Inject
    public GoogleAnalyticsNodePlugin(AnnotatedServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getPluginVersion() {
        return "0.0.0";
    }

    @Override
    public void upgrade(String fromVersion) throws PluginException {
        pluginTools.upgradeAuthNode(GoogleAnalyticsNode.class);
    }

    @Override
    public void onStartup() throws PluginException {
        for (Class<? extends Node> nodeClass : getNodes()) {
            pluginTools.registerAuthNode(nodeClass);
        }
    }

    @Override
    protected Iterable<? extends Class<? extends Node>> getNodes() {
        return asList(
                GoogleAnalyticsNode.class
        );
    }
}
