/*
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 *
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.piazza.commons.dao.ds;

import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.util.StringUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.logging.Logger;


/**
 * SimpleDataSource is an implementation of DataSource and is backed by the
 * DriverManager. It provides no pooling mechcanism. Moreover, the timeout
 * and log writer features are shared among all its instances rather than
 * each has its own.
 *
 * @author Hugo Y. K. Lam
 */
public class SimpleDataSource implements DataSource {

    private String url, username, password;

    /**
     * Creates a new instance of SimpleDataSource.
     *
     * @param driver the jdbc driver.
     * @param url    the url for connecting to the data source.
     * @throws ClassNotFoundException if the driver class was not found.
     * @throws SQLException           if the url is invalid or a database access error
     *                                occurs.
     */
    public SimpleDataSource(String driver, String url)
            throws ClassNotFoundException, SQLException {
        this(driver, url, null, null);
    }

    /**
     * Creates a new instance of SimpleDataSource.
     *
     * @param driver   the jdbc driver.
     * @param url      the url for connecting to the data source.
     * @param username the username used in connection.
     * @param password the password used in connection.
     * @throws ClassNotFoundException if the driver class was not found.
     * @throws SQLException           if the url is invalid or a database access error
     *                                occurs.
     */
    public SimpleDataSource(String driver, String url, String username,
                            String password) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        DriverManager.getDriver(url);

        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * @see DataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    /**
     * @see DataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    /**
     * @see DataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /**
     * @see DataSource#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    /**
     * @see DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * @see DataSource#getConnection(String,
     * String)
     */
    public Connection getConnection(String username, String password)
            throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
    
    
