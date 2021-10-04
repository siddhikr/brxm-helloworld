package com.example.learning.brxm.jaxrs.services;

import com.example.learning.brxm.jaxrs.model.Node;
import com.example.learning.brxm.jaxrs.model.Nodes;
import com.example.learning.brxm.service.MyJcrCrudService;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.repository.util.RepoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

@Path("/nodes")
@Produces("application/xml")
// Plain JAX-RS rest service for Scenario 2.5 - REST API endpoint
public class NodePlainResource extends org.hippoecm.hst.jaxrs.services.AbstractResource {

    private static final Logger log = LoggerFactory.getLogger(NodePlainResource.class);

    @Context
    UriInfo uriInfo;

    @GET
    // Scenario - get AllNodes (or) getNode by query
    public Nodes getNodes(@QueryParam("query") String query) {
        log.info("Inside getNodes()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        MyJcrCrudService myJcrCrudService =
                HstServices.getComponentManager().getComponent(MyJcrCrudService.class.getName());
        Session session = null;
        List<Node> pojoNodeList = new ArrayList<>();
        Nodes pojoNodes = new Nodes();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            String xPathQuery;
            if (StringUtils.isNotBlank(query)) {
                xPathQuery = "//*[jcr:contains(. , '" + query + "')]";
            } else {
                xPathQuery = "/jcr:root//* order by @jcr:name";
            }
            pojoNodeList.addAll(myJcrCrudService.getNodeList(xPathQuery, session));
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
    // Scenario - get node by id
    public Response getNodeById(@PathParam("uuid") String uuid) {
        log.info("Inside getNodeById()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        MyJcrCrudService myJcrCrudService =
                HstServices.getComponentManager().getComponent(MyJcrCrudService.class.getName());
        Session session = null;
        Node pojoNode;
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            pojoNode = myJcrCrudService.getNodeByUUID(uuid, session);
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
    // Scenario get node by path
    public Nodes getNodeByPath(@PathParam("path") String path) {
        log.info("Inside getNodeByPath()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        MyJcrCrudService myJcrCrudService =
                HstServices.getComponentManager().getComponent(MyJcrCrudService.class.getName());
        Session session = null;
        List<Node> pojoNodeList = new ArrayList<>();
        Nodes pojoNodes = new Nodes();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            String xPathQuery = null;
            log.info("input path is:" + path);
            if (StringUtils.isNotBlank(path)) {
                String nodeName = path.substring(path.lastIndexOf("/") + 1);
                // TODO - May be handled better if the correct encoding tecnique/JCR Util can be used
                // Current logic - if nodename given in query is a leaf - use getNode
                if (StringUtils.contains(nodeName, ":")) {
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
                    pojoNodeList.addAll(myJcrCrudService.getNodeList(xPathQuery, session));
                }
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
}