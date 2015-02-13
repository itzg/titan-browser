package me.itzg.titanbrowser.web;

import com.google.common.base.Preconditions;
import me.itzg.titanbrowser.common.ConfigurationAware;
import me.itzg.titanbrowser.common.VertexSurroundings;
import me.itzg.titanbrowser.service.TitanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright &copy; 2015 Geoff Bourne. All rights reserved.</p>
 *
 * @author itzg
 * @since 12/4/2014
 */
@RestController
public class TitanAccessResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitanAccessResource.class);

    @Autowired
    private TitanService titanService;

    @Autowired
    private List<ConfigurationAware> configurables;

    @RequestMapping(value="/search/{type}")
    public Collection<Map<String, Object>> searchVertex(@PathVariable("type") String elementType,
            @RequestParam Map<String,String> attributes) {

        switch (elementType.toLowerCase()) {
            case "vertex":
                return titanService.searchForVertexGetProperties(attributes);
        }

        throw new IllegalArgumentException("A graph element type of " + elementType +
                "is not supported");
    }

    @RequestMapping("/vertex/{id}/surroundings")
    public VertexSurroundings getVertexSurroundings(@PathVariable String id,
                                                    @RequestParam(required = false) List<String> include) {
        return titanService.getVertexSurroundings(id, include);
    }

    @RequestMapping("/vertex/{id}/properties")
    public Map<String,Object> getVertexProperties(@PathVariable String id) {
        return titanService.getVertexProperties(id);
    }

    @RequestMapping("/vertex/{id}/property-keys")
    public Collection<String> getVertexPropertyNames(@PathVariable String id) {
        return titanService.getVertexPropertyKeys(id);
    }

    @RequestMapping(value="/vertex/{id}/properties/{propKey}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Object getVertexProperty(@PathVariable String id, @PathVariable String propKey) {
        return titanService.getVertexProperty(id, propKey);
    }

    @RequestMapping(value = "/configuration", method = RequestMethod.GET)
    public Map<String,Object> getConfiguration() {
        Map<String, Object> merged = new HashMap<>();
        for (ConfigurationAware configurable : configurables) {
            merged.putAll(configurable.getConfiguration());
        }
        return merged;
    }

    @RequestMapping(value = "/configuration", method = RequestMethod.PUT)
    public void setConfiguration(@RequestBody MultiValueMap<String, String> configuration) {

        Map<String, Object> appliedConfig = new HashMap<>();
        for (Map.Entry<String, List<String>> givenEntry : configuration.entrySet()) {
            Preconditions.checkArgument(givenEntry.getValue().size() == 1);
            appliedConfig.put(givenEntry.getKey(), givenEntry.getValue().get(0));
        }
        // read-only for them to use
        appliedConfig = Collections.unmodifiableMap(appliedConfig);
        LOGGER.debug("Setting configuration {}", appliedConfig);

        for (ConfigurationAware configurable : configurables) {
            configurable.setConfiguration(appliedConfig);
        }
    }
}
