package databaseModels;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManagement {

    public void insert(Connection con) throws SQLException;

    public int getID(Connection con);
}
