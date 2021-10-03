package com.example.learning.brxm.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.repository.util.RepoUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AssessmentSearchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AssessmentSearchServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        log.info("Inside doGet");
        String[] searchText = req.getParameterValues("query");
        Session session = null;
        Repository repository =
                HstServices.getComponentManager().getComponent(Repository.class.getName());
        List<String> nodes = new ArrayList<>();
        PrintWriter out = res.getWriter();
        try {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            if (searchText != null) {
                log.info("Input Search text is:" + searchText[0]);
                String sanitizedInput = sanitizeInputString(searchText[0]);
                log.info("Sanitized Search text is:" + sanitizedInput);
                sanitizedInput = encodeCharactersForJCRQueries(sanitizedInput);
                log.info("Encoded Search text is:" + sanitizedInput);
                if (StringUtils.isNotBlank(sanitizedInput)) {
                    // TODO - to check if this does a full text search by default in binary assets
                    String xPathQuery = "//*[jcr:contains(. , '" + sanitizedInput + "')]";
                    nodes.addAll(getAllNodesThatMatchSearchCriteria(xPathQuery, session));
                }
            }
            if (!nodes.isEmpty()) {
                for (String nodeStr : nodes) {
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

    private List<String> getAllNodesThatMatchSearchCriteria(String xpath, Session session) throws RepositoryException {
        List<String> results = new ArrayList<>();
        QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(xpath, "xpath").execute();
        NodeIterator nodes = queryResult.getNodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            if (!node.isNodeType("hippofacnav:facetnavigation")) {
                String nodeInfo = "nodeNameAndPath:" + node.getPath() + "/" + node.getName();
                PropertyIterator nodeProperties = node.getProperties();
                while (nodeProperties.hasNext()) {
                    Property property = nodeProperties.nextProperty();
                    if (!property.isNode()) {
                        String propertyValue = getValue(property).toString();
                        //TODO - To check how to write recursive property node reference calls in terms of referenced nodes
                        nodeInfo = nodeInfo + ":::" + property.getName() + "---" + propertyValue;
                    }
                }
                results.add(nodeInfo);
            }
        }
        return results;
    }

    // TODO - Service/helper here
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
                    //TODO- To check how references can be traversed and values of referenced node be returned
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

    private String sanitizeInputString(String input) {
        HtmlPolicyBuilder htmlPolicyBuilder = new HtmlPolicyBuilder();
        htmlPolicyBuilder.allowStandardUrlProtocols();
        htmlPolicyBuilder.allowUrlProtocols("data");
        htmlPolicyBuilder.allowStyling();
        htmlPolicyBuilder.disallowElements("script");
        PolicyFactory htmlPolicy = htmlPolicyBuilder.toFactory();
        return htmlPolicy.sanitize(input);
    }

    private String encodeCharactersForJCRQueries(String input){
        String output = null;
        if(StringUtils.isNotEmpty(input)){
            output = Text.escapeIllegalXpathSearchChars(input).replaceAll("'", "''");
            output = output.replaceAll(":", "_x003A_");
            output = RepoUtils.encodeXpath(output);
        }
        return output;
    }

    private String gregCalToString(Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(calendar.getTime());
    }
}