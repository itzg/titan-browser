package me.itzg.titanbrowser.service;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import me.itzg.titanbrowser.common.ConfigurationAware;
import me.itzg.titanbrowser.common.TitanBrowserConstants;
import me.itzg.titanbrowser.common.VertexSurroundings;
import org.apache.commons.configuration.BaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
@Service
@Scope("session")
public class TitanCassandraServiceImpl implements TitanService, ConfigurationAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitanCassandraServiceImpl.class);

    private String seedHost;
    private TitanGraph titanGraph;
    private String username;
    private String password;

    void init() {
        if (titanGraph != null) {
            titanGraph.shutdown();
        }

        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("storage.backend", "cassandra");
        config.setProperty("storage.hostname", seedHost);
        if (username != null && password != null) {
            config.setProperty("storage.username", username);
            config.setProperty("storage.password", password);
        }

        LOGGER.debug("Connecting to {} using {}", seedHost, config);
        titanGraph = TitanFactory.open(config);
    }

    public String getSeedHost() {
        return seedHost;
    }

    public void setSeedHost(String seedHost) {
        this.seedHost = seedHost;
        init();
    }

    private void setAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<Map<String, Object>> searchForVertexGetProperties(Map<String, String> searchProperties) {
        checkState();

        if (searchProperties == null || searchProperties.isEmpty()) {
            throw new IllegalArgumentException("search properties was missing or empty");
        }
        LOGGER.debug("Searching for vertices using {}", searchProperties);

        TitanGraphQuery<? extends TitanGraphQuery> query = titanGraph.query();

        for (Map.Entry<String, String> searchEntry : searchProperties.entrySet()) {
            query.has(searchEntry.getKey(), searchEntry.getValue());
        }

        Iterable<Vertex> vertices = query.vertices();

        List<Map<String, Object>> results = new ArrayList<>();
        for (Vertex vertex : vertices) {
            Map<String, Object> result = getPropertiesOfVertex(vertex);
            results.add(result);
        }

        return results;
    }

    private void checkState() {
        Preconditions.checkState(titanGraph != null, "The Cassandra seed host needs to be set first.");
    }

    @Override
    public VertexSurroundings getVertexSurroundings(Object vertexId, List<String> includeAdjacentProperties) {
        checkState();

        Vertex originVertex = titanGraph.getVertex(vertexId);
        if (originVertex == null) {
            return null;
        }

        final VertexSurroundings surroundings = new VertexSurroundings();
        surroundings.setProperties(getPropertiesOfVertex(originVertex));

        extractEdgeAdjacents(originVertex, Direction.OUT, includeAdjacentProperties, new EdgeConsumer() {
            @Override
            public void consume(VertexSurroundings.Edge edgeDetails) {
                surroundings.addEdgeOut(edgeDetails);
            }
        });

        extractEdgeAdjacents(originVertex, Direction.IN, includeAdjacentProperties, new EdgeConsumer() {
            @Override
            public void consume(VertexSurroundings.Edge edgeDetails) {
                surroundings.addEdgeIn(edgeDetails);
            }
        });

        return surroundings;
    }

    @Override
    public Map<String, Object> getVertexProperties(Object vertexId) {
        checkState();

        Vertex originVertex = titanGraph.getVertex(vertexId);
        if (originVertex == null) {
            return null;
        }
        return getPropertiesOfVertex(originVertex);
    }

    @Override
    public Object getVertexProperty(Object vertexId, String propertyKey) {
        checkState();

        Vertex originVertex = titanGraph.getVertex(vertexId);
        if (originVertex == null) {
            return null;
        }

        return originVertex.getProperty(propertyKey);
    }

    @Override
    public Collection<String> getVertexPropertyKeys(String vertexId) {
        checkState();

        Vertex originVertex = titanGraph.getVertex(vertexId);
        if (originVertex == null) {
            return null;
        }

        return originVertex.getPropertyKeys();
    }

    private void extractEdgeAdjacents(Vertex vertex, Direction edgeDirection, List<String> includeNeighborProperties,
                                      EdgeConsumer consumer) {
        Iterable<com.tinkerpop.blueprints.Edge> edges = vertex.getEdges(edgeDirection);
        for (com.tinkerpop.blueprints.Edge edge : edges) {
            VertexSurroundings.Edge edgeDetails = new VertexSurroundings.Edge();
            edgeDetails.setLabel(edge.getLabel());
            Vertex adjacentVertex = edge.getVertex(edgeDirection == Direction.OUT ? Direction.IN : Direction.OUT);

            edgeDetails.setAdjacent(new VertexSurroundings.Adjacent());
            edgeDetails.getAdjacent().setId(adjacentVertex.getId());

            if (includeNeighborProperties != null) {
                edgeDetails.getAdjacent().setProperties(new HashMap<String, Object>());

                for (String prop : includeNeighborProperties) {
                    edgeDetails.getAdjacent().getProperties().put(prop, adjacentVertex.getProperty(prop));
                }
            }

            consumer.consume(edgeDetails);
        }
    }

    private Map<String, Object> getPropertiesOfVertex(Vertex vertex) {
        HashMap<String, Object> result = new HashMap<>();
        for (String propKey : vertex.getPropertyKeys()) {
            result.put(propKey, vertex.getProperty(propKey));
        }
        result.put(TitanBrowserConstants.PROP_ID, vertex.getId());
        return result;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Collections.singletonMap("titan.cassandra.seedHost", (Object)seedHost);
    }

    @Override
    public void setConfiguration(Map<String, Object> configuration) {
        final String authenticateStr = (String) configuration.get("titan.cassandra.authentication[authenticate]");
        if (Boolean.parseBoolean(authenticateStr)) {
            setAuthentication(((String) configuration.get("titan.cassandra.authentication[authUser]")),
                    ((String) configuration.get("titan.cassandra.authentication[authPass]")));
        }

        final Object value = configuration.get("titan.cassandra.seedHost");
        if (value != null) {
            setSeedHost(value.toString());
        }


    }

    private interface EdgeConsumer {
        void consume(VertexSurroundings.Edge edgeDetails);
    }
}
