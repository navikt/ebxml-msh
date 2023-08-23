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
import java.time.Duration;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;

/**
 * SimpleDSDAOFactory is a subclass of DataSourceDAOFactory and provides an
 * implementation for accessing the data source by a simple DataSource object
 * which is backed by the DriverManager and provides no pooling.
 * <p>
 * Notice that the DAO created by the createDAO() method shall be an instance of
 * DataSourceDAO since this factory is a DataSourceDAOFactory.
 * 
 * @author Hugo Y. K. Lam
 *  
 */
public class SimpleDSDAOFactory extends DataSourceDAOFactory {

    /**
     * Creates a new instance of SimpleDSDAOFactory.
     */
    public SimpleDSDAOFactory() {
        super();
    }

    /**
     * Initializes this DAOFactory.
     */
    public void initFactory() throws DAOException {
        try {
			String driver = null;
			String url = null;
			String username = null;
			String password = null;
						
			boolean isPooling = true;
			int maxIdle = 0;
			int maxActive = 10;
			Duration maxWait;
			
			boolean testOnBorrow = false;
			boolean testOnReturn = false;
			boolean testWhileIdle = false;
			String validationQuery = null;
			
			try {
	            driver = getParameter("driver");
	            url = getParameter("url");
	            username = getParameter("username", null);
	            password = getParameter("password", null);
	            
	            maxIdle = StringUtilities.parseInt(getParameter("maxIdle", null), 0);
		        maxActive = StringUtilities.parseInt(getParameter("maxActive", null), 0);
		        maxWait = Duration.ofSeconds(StringUtilities.parseInt(getParameter("maxWait", null), -1));
				
		        validationQuery = StringUtilities.trim(getParameter("validationQuery", null));
		        if (validationQuery != null) {
		            testOnBorrow = StringUtilities.parseBoolean(getParameter("testOnBorrow", "false"));
		            testOnReturn = StringUtilities.parseBoolean(getParameter("testOnReturn", "false"));
		            testWhileIdle = StringUtilities.parseBoolean(getParameter("testWhileIdle", "false"));
		        }
	            
			} catch (Exception e) {
				throw new DAOException("Invalid parameter for SimpleDSDAOFactory.");
			}
			
			if (getParameter("pooling", null) != null) {
				if (!getParameter("pooling").equalsIgnoreCase("true")
						&& !getParameter("pooling").equalsIgnoreCase("false")) {
					throw new DAOException("Invalid parameter for SimpleDSDAOFactory.");
				}
				isPooling = StringUtilities.parseBoolean(getParameter("pooling", "true"));
			}
				
            DataSource datasource;
            
            if (isPooling) {
                DriverAdapterCPDS cpds = new DriverAdapterCPDS();
                cpds.setDriver(driver);
                cpds.setUrl(url);
                cpds.setUser(username);
                cpds.setPassword(password);
    
                SharedPoolDataSource sds = new SharedPoolDataSource();
                sds.setConnectionPoolDataSource(cpds);
                sds.setDefaultMaxIdle(maxIdle);
                sds.setMaxTotal(maxActive);
                sds.setDefaultMaxWait(maxWait);
                sds.setDefaultTestOnBorrow(testOnBorrow);
            	sds.setDefaultTestOnReturn(testOnReturn);
            	sds.setDefaultTestWhileIdle(testWhileIdle);
                sds.setValidationQuery(validationQuery);
                	 				
                datasource = sds;
            }
            else {
                datasource = new SimpleDataSource(driver, url, username, password);
            }
            
            setDataSource(datasource);
        }
        catch (Exception e) {
            throw new DAOException("Cannot initialize SimpleDSDAOFactory!", e);
        }
    }
    
}