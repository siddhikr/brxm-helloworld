package com.example.learning.brxm.servlet;

import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// Example servlet to demonstrate Scenario-2 (specifically scenarios 2.2,2.3 of the assessment)
// Write a Servlet that reads/queries from the repository
public class AssessmentServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AssessmentServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        log.info("Inside doGet()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        PrintWriter out = res.getWriter();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            final Node documentsNode = session.getRootNode().getNode("content/documents");
            long t0 = System.currentTimeMillis();
            listNodesUnderAGivenNode(documentsNode, out);
            log.info("Time taken by listNodesUnderAGivenNode:" + (System.currentTimeMillis() - t0));
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

    // Print all nodes under the given inputNode (including the inputNode) in the JCR tree
    private void listNodesUnderAGivenNode(Node inputNode, PrintWriter out) throws RepositoryException {
        NodeIterator iterator = inputNode.getNodes();
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            // To avoid an infinite traverse exclude hippofacnav:facetnavigation
            if (!node.isNodeType("hippofacnav:facetnavigation")) {
                log.info("node.getName():" + node.getName());
                out.println("<div>" + node.getName() + "</div>");
            }
            listNodesUnderAGivenNode(node, out);
        }
    }
}