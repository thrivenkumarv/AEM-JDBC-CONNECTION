package com.mysite.core.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.commons.datasource.poolservice.DataSourceNotFoundException;
import com.day.commons.datasource.poolservice.DataSourcePool;

/**
 * The servlet class for form handler callbacks. It simply needs to response 200
 * OK
 * with as small a payload as possible.
 *
 * @author Vonage
 *
 */
@Component(service = Servlet.class, property = { "description=Get form handler callback response",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/mysite/hitdatabase" })
public class HitDataBaseServlet extends SlingSafeMethodsServlet {
    /**
     * DataSourcePool
     */
    @Reference
    private DataSourcePool dataSourcePool;
    /**
     ** DATA_SOURCE_NAME
     */
    private static final String DATA_SOURCE_NAME = "databrain";

    @Override
    public final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final Logger log = LoggerFactory.getLogger(HitDataBaseServlet.class);
        try {
            DataSource dataSource = (DataSource) dataSourcePool.getDataSource(DATA_SOURCE_NAME);
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement();
                     final ResultSet resultSet = statement.executeQuery("SELECT name FROM databrain.COUNTRY");) {
                    int r = 0;
                    while (resultSet.next()) {
                        r = r + 1;
                        String lastName = resultSet.getString("name");
                        response.setContentType("text/html; charset=utf-8");
                        response.getWriter().write(lastName);
                        response.getWriter().write("<br>");
                    }
                    response.getWriter().write("success");
                    resultSet.close();

                } catch (SQLException e) {
                    log.error("Unable to validate SQL connection for [ {} ]", DATA_SOURCE_NAME, e);
                    response.getWriter().write("Failed");
                }
            } else {
                response.getWriter().write("Failed");
            }
        } catch (DataSourceNotFoundException e) {
            log.error("Unable to establish an connection with the JDBC data source [ {} ]", DATA_SOURCE_NAME, e);
            response.getWriter().write("Failed");
        }
    }
}
