package database;

import oracle.jdbc.OracleTypes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * DatabaseUtil.java The class contains all operations for the application that interacts with the database.
 * Uses the Singleton pattern because of the nature of javaFX controllers.
 * 
 * @author Daniel Romero Mancia, Braydon Rooks
 *
 */
public class DatabaseUtil {

    private static DatabaseUtil util;
	private Connection conn;
	String userName = null;
	String password = null;

	/**
	 * Static method to retrieve the instance of the DatabaseUtil.
	 * @return
	 */
	public static DatabaseUtil getDatabaseUtil() {
	    if (util == null) {
	        util = new DatabaseUtil();
	        return util;
        }
        else {
	        return util;
        }
    }

	/**
	 * Private default constructor.
	 */
    private DatabaseUtil() {

    }

    /**
     * Connect to the database using a username and password and check if the user has the appropriate permissions.
     * @param user The username
     * @param pass The password
     * @return True if the connection is made successfully.
     */
	public boolean connection(String user, String pass)
	{
		userName = user;
		password = pass;
		
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",userName, password);

			if (!checkPerm()) {
				System.out.println("No Permission");
				return false;
			}

			return true;
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to find Driver");
			//e.printStackTrace();
			return false;
		} catch (SQLException e) {
			System.out.println("Problem connecting to database");
			//e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Load a semicolon delimited file into the PAYROLL_LOAD database table.
	 * Creates a control file and log in the directory that was supplied by the user.
	 * @param filepath The file path of the text file to be read in.
	 */
	public void loadFile(String filepath)
	{
		 try {
		     String path = filepath.substring(0, filepath.lastIndexOf('/'));
             createControlFile(path, filepath);
             String sqlldrCmd = "sqlldr userid="+userName+"/"+password+" control=" + path + "/control.ctl log=" + path + "/payrollLog.log";

             System.out.println("SQLLDR Started ....... ");
             Runtime rt = Runtime.getRuntime();
             Process proc = rt.exec(sqlldrCmd);
             System.out.println("SQLLDR Ended ........  ");
         } catch (Exception e) {
             e.printStackTrace();
         }
	
	}
	
	/**
	 * Close the connection to the database 
	 */
	public void closeConnection()
	{
		try {
			userName = null;
			password = null;
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Check the permissions of a user that is logged into the database.
	 * Uses a call to a stored function privilege_tf
	 * @return True if the user has the correct permissions. 
	 * @throws SQLException If an error is encountered with the database connection.
	 */
	private boolean checkPerm() throws SQLException
	{
		boolean permission = false;
		CallableStatement cstmt = conn.prepareCall("{? = call privilege_TF(" + "\'" + userName+ "\'" +")}");
		cstmt.registerOutParameter(1, OracleTypes.VARCHAR);
		cstmt.execute();
		String result = cstmt.getString(1);
		if(result.equals("Y"))
		{
			permission = true;
		}

		return permission;
	}

	/**
	 * Create the control.ctl file in the path given.
	 * @param path The path to create the control.ctl file
	 * @param filepath The file path of the file to be loaded in.
	 */
    private void createControlFile(String path, String filepath) {
	    try {
            PrintWriter writer = new PrintWriter(new FileWriter(path + "/control.ctl"));
            writer.println("LOAD DATA");
            writer.println("INFILE \'" + filepath + "\'");
            writer.println("REPLACE");
            writer.println("INTO TABLE payroll_load");
            writer.println("FIELDS TERMINATED BY \';\' OPTIONALLY ENCLOSED BY \'\"\'");
            writer.println("TRAILING NULLCOLS");
            writer.println("(payroll_date DATE \"Month dd, yyyy\",");
            writer.println("employee_id,");
            writer.println("amount,");
            writer.println("status)");
            writer.close();

        } catch (IOException ex) {
	        ex.printStackTrace();
        }
    }
	
    /**
     * Call the ZERO_OUT_TEMP stored procedure to zero out account values.
     * @throws SQLException If an error is encountered with the database connection.
     */
	public void doMonthEnd() throws SQLException {
		CallableStatement cstmt = conn.prepareCall("{call ZERO_OUT_TEMP()}");
		cstmt.execute();
	}

	/**
	 * Export the data from the NEW_TRANSACTIONS table using the export_file_proc procedure,
	 * @param path The full path where the comma separated file will be written to.
	 * @param filename The file name of the file to be created.
	 * @param alias The alias for the full path
	 * @throws SQLException If an error is encountered with the database connection.
	 */
	public void export(String path, String filename, String alias) throws SQLException {
		Statement stmt = conn.createStatement();
		String statement = "CREATE OR REPLACE DIRECTORY " + alias.toUpperCase() + " AS \'" + path + "\'";
		stmt.executeUpdate(statement);

		CallableStatement cstmt = conn.prepareCall("{call export_file_proc(?, ?)}");
		cstmt.setString(1, alias.toUpperCase());
		cstmt.setString(2, filename);
		cstmt.execute();
	}
	
}
