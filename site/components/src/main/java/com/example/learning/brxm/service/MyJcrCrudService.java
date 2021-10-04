package com.example.learning.brxm.service;

import com.example.learning.brxm.jaxrs.model.Node;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;

public interface MyJcrCrudService {

    /**
     * Based on a given input JCR xpath query
     * this method returns a list of nodeStrings representing node information
     * Example list of nodeStrs returned by this method is of the format
     * nodeNameAndPath:nodePath1/nodeName1:::propertyName1---propertyValue1:::propertyName2---propertyValue2...
     * nodeNameAndPath:nodePath2/nodeName2:::propertyName1---propertyValue1:::propertyName2---propertyValue2...
     * and so on so forth..
     * ":::" is used as a delimiter between different properties of a given node
     * "---" is used as delimiter between a given property's key and value
     * Since this is a simple string representation these 2 types of delimiters are used to represent nodeinfo
     *
     * @param xpath   - input JCR query string
     * @param session - JCR session
     * @return a list of strings having node information
     * @throws RepositoryException
     */
    List<String> getNodesAsListOfStrings(String xpath, Session session) throws RepositoryException;

    /**
     * Based on a given input JCR xpath query
     * this method returns a list of model POJO nodes representing node information
     *
     * @param xpath   - input JCR query string
     * @param session - JCR session
     * @return a list of model POJO nodes having node information
     * Each POJO node will have the following JCR node info - uuid, name, path and type
     * @throws RepositoryException
     */
    List<Node> getNodeList(String xpath, Session session) throws RepositoryException;

    /**
     * Based on a given input JCR node's uuid
     * this method returns the JCR node with that uuid
     *
     * @param uuid   - input JCR uuid
     * @param session - JCR session
     * @return a model POJO node having the given input uuid
     * The POJO node will have the following JCR node info - uuid, name, path and type
     * @throws RepositoryException
     */
    Node getNodeByUUID(String uuid, Session session) throws RepositoryException;
}