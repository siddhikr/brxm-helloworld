package com.example.learning.brxm.servlet;

import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AssessmentServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AssessmentServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        log.info("Inside doGet()");
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        Session session = null;
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            Node rootNode = session.getRootNode();
        } catch (RepositoryException e) {
            log.warn("Error processing the assessment get request", e);
        } finally {
            //may be used for a logout if needed
            //session.logout();
        }
    }
}