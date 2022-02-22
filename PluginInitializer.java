 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.configuration.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.mockito.internal.util.collections.Iterables;
import org.mockito.plugins.PluginSwitch;

class PluginInitializer {

    private final PluginSwitch pluginSwitch;
    private final Set<String> alias;
    private final DefaultMockitoPlugins plugins;

    PluginInitializer(PluginSwitch pluginSwitch, Set<String> alias, DefaultMockitoPlugins plugins) {
        this.pluginSwitch = pluginSwitch;
        this.alias = alias;
        this.plugins = plugins;
    }

    public <T> List<T> loadImpls(Class<T> service) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        Enumeration<URL> resources;
        try {
            resources = loader.getResources("mockito-extensions/" + service.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + service, e);
        }
        
        try {
            List<String> classesOrAliases =
                    new PluginFinder(pluginSwitch)
                            .findPluginClasses(Iterables.toIterable(resources));
            List<T> impls = new ArrayList<>();
            for (String classOrAlias : classesOrAliases) {
                if (alias.contains(classOrAlias)) {
                    classOrAlias = plugins.getDefaultPluginClass(classOrAlias);
                }
                Class<?> pluginClass = loader.loadClass(classOrAlias);
                Object plugin = pluginClass.getDeclaredConstructor().newInstance();
                impls.add(service.cast(plugin));
                
                int size = impls.size();
                System.out.println("size of array list after creating: " + size);
                if (size ==1)
                	return (List<T>) impls.get(1);
            }
            return impls;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load " + service + " implementation declared in " + resources, e);
        }
    }
}
