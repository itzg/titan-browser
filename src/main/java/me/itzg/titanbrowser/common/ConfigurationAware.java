package me.itzg.titanbrowser.common;

import java.util.Map;

/**
 * <p>Copyright &copy; 2015 Geoff Bourne. All rights reserved.</p>
 *
 * @author itzg
 * @since 12/5/2014
 */
public interface ConfigurationAware {
    Map<String,Object> getConfiguration();

    void setConfiguration(Map<String, Object> configuration);
}
