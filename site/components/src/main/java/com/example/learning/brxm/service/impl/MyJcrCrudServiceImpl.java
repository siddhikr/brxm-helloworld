package com.example.learning.brxm.service.impl;

import com.example.learning.brxm.jaxrs.model.Node;
import com.example.learning.brxm.service.MyJcrCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyJcrCrudServiceImpl implements MyJcrCrudService {

    private static final Logger log = LoggerFactory.getLogger(MyJcrCrudServiceImpl.class);
//    private static final String colon = ":";
//    private static final String colonISO9075 = "_x003A_";

    @Override
    public List<Node> getNodeList(String xpath, Session session) throws RepositoryException {
        List<Node> pojoNodes = new ArrayList<>();
        QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(xpath, "xpath").execute();
        NodeIterator jcrNodes = queryResult.getNodes();
        while (jcrNodes.hasNext()) {
            javax.jcr.Node jcrNode = jcrNodes.nextNode();
            if (!jcrNode.isNodeType("hippofacnav:facetnavigation")) {
                Node pojoNode = new Node();
                pojoNode.setUuId(jcrNode.getIdentifier());
                pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
                pojoNode.setName(jcrNode.getName());
                pojoNode.setPath(jcrNode.getPath());
                pojoNodes.add(pojoNode);
            }
        }
        return pojoNodes;
    }

    @Override
    public Node getNodeByUUID(String uuid, Session session) throws RepositoryException {
        Node resultPojoNode = new Node();
        final javax.jcr.Node jcrNode = session.getNodeByIdentifier(uuid);
        resultPojoNode.setUuId(jcrNode.getIdentifier());
        resultPojoNode.setType(jcrNode.getPrimaryNodeType().getName());
        resultPojoNode.setName(jcrNode.getName());
        resultPojoNode.setPath(jcrNode.getPath());
        return resultPojoNode;
    }

    @Override
    public List<String> getNodesAsListOfStrings(String xpath, Session session) throws RepositoryException {
        List<String> results = new ArrayList<>();
        QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(xpath, "xpath").execute();
        NodeIterator nodes = queryResult.getNodes();
        while (nodes.hasNext()) {
            javax.jcr.Node node = nodes.nextNode();
            if (!node.isNodeType("hippofacnav:facetnavigation")) {
                String nodeInfo = "nodeNameAndPath:" + node.getPath() + "/" + node.getName();
                PropertyIterator nodeProperties = node.getProperties();
                while (nodeProperties.hasNext()) {
                    Property property = nodeProperties.nextProperty();
                    if (!property.isNode()) {
                        // TODO - A better way of getting and serializing all properties of a JCR node
                        String propertyValue = getValue(property).toString();
                        // TODO - To check how to write recursive property node reference calls in terms of referenced nodes
                        nodeInfo = nodeInfo + ":::" + property.getName() + "---" + propertyValue;
                    }
                }
                results.add(nodeInfo);
            }
        }
        return results;
    }

    private Object getValue(final Property property) {
        try {
            if (property.isMultiple()) {
                return getMultipleValues(property);
            } else {
                return getSingleValue(property);
            }
        } catch (RepositoryException ignore) {
        }
        return null;
    }

    private Object getSingleValue(final Property property) {
        try {
            switch (property.getType()) {
                case PropertyType.STRING:
                    return property.getString();
                case PropertyType.LONG:
                    return property.getLong();
                case PropertyType.DATE:
                    return gregCalToString(property.getDate());
                case PropertyType.BOOLEAN:
                    return property.getBoolean();
//                case PropertyType.REFERENCE:
//                    return new RepositoryMapImpl(property.getNode());
                default:
                    return property.getString();
            }
        } catch (RepositoryException ignore) {
        }
        return null;
    }

    private Object[] getMultipleValues(final Property property) {
        try {
            Value[] values = property.getValues();
            Object[] result;
            int type = property.getType();
            switch (type) {
                case PropertyType.STRING:
                    result = new String[values.length];
                    break;
                case PropertyType.LONG:
                    result = new Long[values.length];
                    break;
                case PropertyType.DATE:
                    result = new Calendar[values.length];
                    break;
                case PropertyType.BOOLEAN:
                    result = new Boolean[values.length];
                    break;
//                case PropertyType.REFERENCE:
//                    result = new RepositoryMap[values.length];
//                    break;
                default:
                    result = new String[values.length];
                    break;
            }
            int i = 0;
            for (Value value : values) {
                Object object;
                switch (type) {
                    case PropertyType.STRING:
                        object = value.getString();
                        break;
                    case PropertyType.LONG:
                        object = value.getLong();
                        break;
                    case PropertyType.DATE:
                        object = gregCalToString(value.getDate());
                        break;
                    case PropertyType.BOOLEAN:
                        object = value.getBoolean();
                        break;
                    // TODO- To check how references can be traversed and values of referenced node be returned
//                    case PropertyType.REFERENCE:
//                        object = new RepositoryMapImpl(session.getNodeByIdentifier(value.getString()));
//                        break;
                    default:
                        object = value.getString();
                        break;
                }
                result[i++] = object;
            }
            return result;
        } catch (RepositoryException ignore) {
        }
        return null;
    }

    private String gregCalToString(Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(calendar.getTime());
    }

/*    private String encode(String input) {
        String cleaned = "";
        if (StringUtils.isNotBlank(input)) {
            cleaned = input.replaceAll(" ", "-");
        }
        cleaned = cleaned.replaceAll("&", "-");
        cleaned = cleaned.replaceAll("=", "-");
        cleaned = deAccent(cleaned);
        return NodeNameCodec.encode(cleaned, true);
    }

    private String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }*/
}