package me.itzg.titanbrowser.service;

import me.itzg.titanbrowser.common.VertexSurroundings;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright &copy; 2015 Geoff Bourne. All rights reserved.</p>
 *
 * @author itzg
 * @since 12/4/2014
 */
public interface TitanService {
    Collection<Map<String, Object>> searchForVertexGetProperties(Map<String, String> searchAttributes);

    VertexSurroundings getVertexSurroundings(Object vertexId, List<String> includeAdjacentProperties);

    Map<String,Object> getVertexProperties(Object vertexId);

    Object getVertexProperty(Object vertexId, String propertyKey);

    Collection<String> getVertexPropertyKeys(String vertexId);
}
