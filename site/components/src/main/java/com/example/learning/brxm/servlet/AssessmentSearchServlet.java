package com.example.learning.brxm.servlet;

import com.example.learning.brxm.service.MyJcrCrudService;
import com.example.learning.brxm.utils.MyEncoderUtils;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// Example servlet to demonstrate Scenario-2 (specifically scenario "2.4 - Querying" of the assessment)
public class AssessmentSearchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AssessmentSearchServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        log.info("Inside doGet");
        String[] searchText = req.getParameterValues("query");
        Session session = null;
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        MyJcrCrudService myJcrCrudService =
                HstServices.getComponentManager().getComponent(MyJcrCrudService.class.getName());
        List<String> nodeListStr = new ArrayList<>();
        PrintWriter out = res.getWriter();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            if (searchText != null) {
                log.info("Input Search text is:" + searchText[0]);
                String sanitizedInput = MyEncoderUtils.sanitizeInputString(searchText[0]);
                log.info("Sanitized Search text is:" + sanitizedInput);
                sanitizedInput = MyEncoderUtils.encodeCharactersForJCRQueries(sanitizedInput);
                log.info("Encoded Search text is:" + sanitizedInput);
                if (StringUtils.isNotBlank(sanitizedInput)) {
                    String xPathQuery = "//*[jcr:contains(. , '" + sanitizedInput + "')]";
                    nodeListStr.addAll(myJcrCrudService.getNodesAsListOfStrings(xPathQuery, session));
                } else {
                    log.error("Error in encoding/sanitizing the input");
                    out.println("<div>" + "No results found" + "</div>");
                }
            }
            if (!nodeListStr.isEmpty()) {
                for (String nodeStr : nodeListStr) {
                    out.println("<div>" + nodeStr + "</div>");
                }
            } else {
                out.println("<div>" + "No results found" + "</div>");
            }
        } catch (RepositoryException e) {
            log.error("Error processing the assessment search get request", e);
            out.println("<div>" + "There was an error processing this search" + "</div>");
        } finally {
            //may be used for a logout if needed
            if (session != null) {
                session.logout();
            }
        }
    }
}