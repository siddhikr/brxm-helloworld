package com.example.learning.brxm.jaxrs.services;

import com.example.learning.brxm.jaxrs.model.Node;
import com.example.learning.brxm.jaxrs.model.Nodes;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.util.RepoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Path("/nodes")
@Produces("application/xml")
public class NodePlainResource extends org.hippoecm.hst.jaxrs.services.AbstractResource {

    private static final Logger log = LoggerFactory.getLogger(NodePlainResource.class);
    private static final String colon = ":";
    private static final String colonISO9075 = "_x003A_";

    @Context
    UriInfo uriInfo;

    public static String encode(String input) {
        String cleaned = "";
        if (StringUtils.isNotBlank(input)) {
            cleaned = input.replaceAll(" ", "-");
        }
        cleaned = cleaned.replaceAll("&", "-");
        cleaned = cleaned.replaceAll("=", "-");
        cleaned = deAccent(cleaned);
        return NodeNameCodec.encode(cleaned, true);
    }

    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

/*    @GET
    @Path("/path/{path}")
    public Response getNodeByPath(@PathParam("path") String path) {
        log.info("Inside getNodeByPath()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        Node pojoNode = new Node();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            final javax.jcr.Node jcrNode = session.getNode("/"+path);
            pojoNode.setUuId(jcrNode.getIdentifier());
            pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
            pojoNode.setName(jcrNode.getName());
            pojoNode.setPath(jcrNode.getPath());
            if (pojoNode == null) {
                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                        .build();
            }
            if (pojoNode != null) {
                UriBuilder builder = UriBuilder.fromResource(NodePlainResource.class)
                        .path(NodePlainResource.class, "getNodeByPath");
                Link link = Link.fromUri(builder.build(path)).rel("self").build();
                pojoNode.setLink(link);
            }
        } catch (RepositoryException e) {
            log.error("Error processing the getNodeByPath RestAPI call", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(pojoNode).build();
    }*/

    @GET
    public Nodes getNodes(@QueryParam("query") String query) {
        log.info("Inside getNodes()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        List<Node> pojoNodeList = new ArrayList<>();
        Nodes pojoNodes = new Nodes();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            //final javax.jcr.Node jcrNode = session.getRootNode().getNode("content/documents");
            //pojoNodeList.addAll(iterateThroughTheJCRTreeUsingGetNodes(jcrNode,pojoNodeList));
            String xPathQuery;
            if (StringUtils.isNotBlank(query)) {
                xPathQuery = "//*[jcr:contains(. , '" + query + "')]";
            } else {
                xPathQuery = "/jcr:root//* order by @jcr:name";
            }
            pojoNodeList.addAll(iterateThroughTheJCRTreeUsingQuery(xPathQuery, session));
            if (pojoNodeList.isEmpty()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            pojoNodes.setNodes(pojoNodeList);
            pojoNodes.setSize(pojoNodeList.size());
        } catch (RepositoryException e) {
            log.error("Error processing the getNodes RestAPI call", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
        Link link = Link.fromUri(uriInfo.getPath()).rel("uri").build();
        pojoNodes.setLink(link);
        //Set links in configuration items
        for (Node pojoNode : pojoNodeList) {
            Link lnk = Link.fromUri(uriInfo.getPath() + "/" + pojoNode.getUuid()).rel("self").build();
            pojoNode.setLink(lnk);
        }
        return pojoNodes;
    }

    @GET
    @Path("/uuid/{uuid}")
    public Response getNodeById(@PathParam("uuid") String uuid) {
        log.info("Inside getNodeById()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        Node pojoNode = new Node();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            final javax.jcr.Node jcrNode = session.getNodeByIdentifier(uuid);
            pojoNode.setUuId(jcrNode.getIdentifier());
            pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
            pojoNode.setName(jcrNode.getName());
            pojoNode.setPath(jcrNode.getPath());
            if (pojoNode == null) {
                return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                        .build();
            }
            if (pojoNode != null) {
                UriBuilder builder = UriBuilder.fromResource(NodePlainResource.class)
                        .path(NodePlainResource.class, "getNodeById");
                Link link = Link.fromUri(builder.build(uuid)).rel("self").build();
                pojoNode.setLink(link);
            }
        } catch (RepositoryException e) {
            log.error("Error processing the getNodeById RestAPI call", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(pojoNode).build();
    }

    @GET
    @Path("/path/{path:.+}")
    public Nodes getNodeByPath(@PathParam("path") String path) {
        log.info("Inside getNodeByPath()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        List<Node> pojoNodeList = new ArrayList<>();
        Nodes pojoNodes = new Nodes();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            String xPathQuery = null;
            log.info("input path is:" + path);
            if (StringUtils.isNotBlank(path)) {
                String nodeName = path.substring(path.lastIndexOf("/") + 1);
                if (StringUtils.contains(nodeName, ":")) {
//                    log.info("nodeName:"+nodeName);
//                    String newNodeName = encode(nodeName);
//                    log.info("newNodeName:"+newNodeName);
//                    String newPath = path.substring(0,path.lastIndexOf('/'))+"/"+newNodeName;
//                    log.info("newPath:"+newPath);
//                    log.info("nodeName:"+nodeName);
//                    String newNodeName = NodeNameCodec.encode(nodeName);
//                    log.info("newNodeName:"+newNodeName);
//                    String newPath = path.substring(0,path.lastIndexOf('/'))+"/"+newNodeName;
//                    log.info("newPath:"+newPath);
//                    xPathQuery = "/jcr:root/" + newPath + "//* order by @jcr:name";
                    final javax.jcr.Node jcrNode = session.getRootNode().getNode(path);
                    Node pojoNode = new Node();
                    pojoNode.setUuId(jcrNode.getIdentifier());
                    pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
                    pojoNode.setName(jcrNode.getName());
                    pojoNode.setPath(jcrNode.getPath());
                    pojoNodeList.add(pojoNode);
                } else {
                    xPathQuery = "/jcr:root/" + RepoUtils.encodeXpath(path) + "//* order by @jcr:name";
                    log.info("xPathQuery:" + xPathQuery);
                    pojoNodeList.addAll(iterateThroughTheJCRTreeUsingQuery(xPathQuery, session));
                }
                //path = encode(path);
                //log.info("Encoded Search text is:" + path);
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            if (pojoNodeList.isEmpty()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            pojoNodes.setNodes(pojoNodeList);
            pojoNodes.setSize(pojoNodeList.size());
        } catch (RepositoryException e) {
            log.error("Error processing the getNodeByPath RestAPI call", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
        Link link = Link.fromUri(uriInfo.getPath()).rel("uri").build();
        pojoNodes.setLink(link);
        //Set links in configuration items
        for (Node pojoNode : pojoNodeList) {
            Link lnk = Link.fromUri(uriInfo.getPath() + "/" + pojoNode.getUuid()).rel("self").build();
            pojoNode.setLink(lnk);
        }
        return pojoNodes;
    }

//    private List<Node> iterateThroughTheJCRTreeUsingGetNodes(javax.jcr.Node inputNode, List<Node> pojoNodes) throws RepositoryException {
//        NodeIterator iterator = inputNode.getNodes();
//        while (iterator.hasNext()) {
//            javax.jcr.Node jcrNode = iterator.nextNode();
//            if (!jcrNode.isNodeType("hippofacnav:facetnavigation")) {
//                Node pojoNode = new Node();
//                pojoNode.setUuId(jcrNode.getIdentifier());
//                pojoNode.setType(jcrNode.getPrimaryNodeType().getName());
//                pojoNode.setName(jcrNode.getName());
//                pojoNode.setPath(jcrNode.getPath());
//                pojoNodes.add(pojoNode);
//            }
//            iterateThroughTheJCRTreeUsingGetNodes(jcrNode, pojoNodes);
//        }
//        return pojoNodes;
//    }

    private List<Node> iterateThroughTheJCRTreeUsingQuery(String xpath, Session session) throws RepositoryException {
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
}