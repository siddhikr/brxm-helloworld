package com.example.learning.brxm.servlet;

import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AssessmentServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AssessmentServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        log.info("Inside doGet()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        PrintWriter out = res.getWriter();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            final Node documentsNode = session.getRootNode().getNode("content/documents");
            long t0 = System.currentTimeMillis();
            iterateThroughTheJCRTreeUsingGetNodes(documentsNode, out);
            log.info("Time taken by iterateThroughTheJCRTreeUsingGetNodes:" + (System.currentTimeMillis() - t0));
            //String xpath = "/jcr:root/content/documents";
            String xpathForOrderByName = "/jcr:root/content/documents//* order by @jcr:name";
            String xpathForOrderByScore = "/jcr:root/content/documents//* order by @jcr:score";
            long t1 = System.currentTimeMillis();
            iterateThroughTheJCRTreeUsingQuery(xpathForOrderByName, session);
            log.info("Time taken by iterateThroughTheJCRTreeUsingQuery for xpathForOrderByName:" + (System.currentTimeMillis() - t1));
            long t2 = System.currentTimeMillis();
            iterateThroughTheJCRTreeUsingQuery(xpathForOrderByScore, session);
            log.info("Time taken by iterateThroughTheJCRTreeUsingQuery for xpathForOrderByScore:" + (System.currentTimeMillis() - t2));
        } catch (RepositoryException e) {
            log.error("Error processing the assessment get request", e);
            out.println("<div>" + "There was an error processing this search" + "</div>");
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
    }

    private void iterateThroughTheJCRTreeUsingGetNodes(Node inputNode, PrintWriter out) throws RepositoryException {
        NodeIterator iterator = inputNode.getNodes();
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            if (!node.isNodeType("hippofacnav:facetnavigation")) {
                log.info("node.getName():" + node.getName());
                out.println("<div>" + node.getName() + "</div>");
            }
            iterateThroughTheJCRTreeUsingGetNodes(node, out);
        }
    }

    private void iterateThroughTheJCRTreeUsingQuery(String xpath, Session session) throws RepositoryException {
        QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(xpath, "xpath").execute();
        NodeIterator nodes = queryResult.getNodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            if (!node.isNodeType("hippofacnav:facetnavigation")) {
                log.info("node.getName():" + node.getName());
            }
        }
    }
}