package com.example.learning.brxm.jaxrs.services;

import com.example.learning.brxm.jaxrs.model.Node;
import com.example.learning.brxm.jaxrs.model.Nodes;
import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/nodes")
@Produces("application/xml")
public class NodePlainResource extends org.hippoecm.hst.jaxrs.services.AbstractResource {

    private static final Logger log = LoggerFactory.getLogger(NodePlainResource.class);

    @Context
    UriInfo uriInfo;

    @GET
    public Nodes getNodes() {
        log.info("Inside getNodes()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        List<Node> pojoNodeList = new ArrayList<>();
        Nodes pojoNodes = new Nodes();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            final javax.jcr.Node jcrNode = session.getRootNode().getNode("content/documents");
            //pojoNodeList.addAll(iterateThroughTheJCRTreeUsingGetNodes(jcrNode,pojoNodeList));
            String xpathForOrderByName = "/jcr:root//* order by @jcr:name";
            pojoNodeList.addAll(iterateThroughTheJCRTreeUsingQuery(xpathForOrderByName, session));
            pojoNodes.setNodes(pojoNodeList);
            pojoNodes.setSize(pojoNodeList.size());
        } catch (RepositoryException e) {
            log.error("Error processing the assessment getNodes Rest call", e);
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
        Link link = Link.fromUri(uriInfo.getPath()).rel("uri").build();
        pojoNodes.setLink(link);
        //Set links in configuration items
        for(Node pojoNode: pojoNodeList){
            Link lnk = Link.fromUri(uriInfo.getPath() + "/" + pojoNode.getId()).rel("self").build();
            pojoNode.setLink(lnk);
        }
        return pojoNodes;
    }

    @GET
    @Path("/{id}")
    public Response getNodeById(@PathParam("id") String id) {
        Node node = new Node();
        if (node == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.status(javax.ws.rs.core.Response.Status.OK)
                .entity(node)
                .build();
    }

    private List<Node> iterateThroughTheJCRTreeUsingGetNodes(javax.jcr.Node inputNode,List<Node> pojoNodes) throws RepositoryException {
        NodeIterator iterator = inputNode.getNodes();
        while (iterator.hasNext()) {
            javax.jcr.Node jcrNode = iterator.nextNode();
            if (!jcrNode.isNodeType("hippofacnav:facetnavigation")) {
                Node pojoNode = new Node();
                pojoNode.setId(jcrNode.getIdentifier());
                pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
                pojoNode.setName(jcrNode.getName());
                pojoNode.setPath(jcrNode.getPath());
                pojoNodes.add(pojoNode);
            }
            iterateThroughTheJCRTreeUsingGetNodes(jcrNode,pojoNodes);
        }
        return pojoNodes;
    }

    private List<Node> iterateThroughTheJCRTreeUsingQuery(String xpath, Session session) throws RepositoryException {
        List<Node> pojoNodes = new ArrayList<>();
        QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(xpath, "xpath").execute();
        NodeIterator jcrNodes = queryResult.getNodes();
        while (jcrNodes.hasNext()) {
            javax.jcr.Node jcrNode = jcrNodes.nextNode();
            if (!jcrNode.isNodeType("hippofacnav:facetnavigation")) {
                Node pojoNode = new Node();
                pojoNode.setId(jcrNode.getIdentifier());
                pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
                pojoNode.setName(jcrNode.getName());
                pojoNode.setPath(jcrNode.getPath());
                pojoNodes.add(pojoNode);
            }
        }
        return pojoNodes;
    }
}