package me.itzg.titanbrowser.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright &copy; 2015 Geoff Bourne. All rights reserved.</p>
 *
 * @author itzg
 * @since 12/4/2014
 */
public class VertexSurroundings {
    private Map<String,Object> properties;

    public static class Adjacent {
        private Object id;

        private Map<String,Object> properties;

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }

    public static class Edge {
        private String label;

        private Adjacent adjacent;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Adjacent getAdjacent() {
            return adjacent;
        }

        public void setAdjacent(Adjacent adjacent) {
            this.adjacent = adjacent;
        }
    }

    private List<Edge> edgesIn;
    private List<Edge> edgesOut;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<Edge> getEdgesIn() {
        return edgesIn;
    }

    public void setEdgesIn(List<Edge> edgesIn) {
        this.edgesIn = edgesIn;
    }

    public void addEdgeIn(Edge edge) {
        if (edgesIn == null) {
            edgesIn = new ArrayList<>();
        }
        edgesIn.add(edge);
    }

    public void addEdgeOut(Edge edge) {
        if (edgesOut == null) {
            edgesOut = new ArrayList<>();
        }
        edgesOut.add(edge);
    }

    public List<Edge> getEdgesOut() {
        return edgesOut;
    }

    public void setEdgesOut(List<Edge> edgesOut) {
        this.edgesOut = edgesOut;
    }
}
