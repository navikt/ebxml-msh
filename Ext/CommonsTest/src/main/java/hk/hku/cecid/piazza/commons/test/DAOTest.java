/**
 * 
 */
package hk.hku.cecid.piazza.commons.test;

import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDAO;


/**
 * @author aaronwalker
 *
 */
public abstract class DAOTest<T extends DataSourceDAO> {

    public T getTestingTarget() {
        return null;
    }
    
    public void setUp() throws Exception {
        
    }
    
    public abstract String getTableName();

}
