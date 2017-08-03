
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTools {
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

	public int connectDB() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/webta", "root", "agOdnek2");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public void disconnect() {
		try {
			conn.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public void update(Long Id, boolean result) {
//		Statement stmt = null;
//		ResultSet rs = null;

		try {
			PreparedStatement statement = conn.prepareStatement(
					"update " + "file_revision set compiled = " + result + " where id=" + Id);
			statement.executeUpdate();
			System.out.println("Finished updating file_revision");
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public void insertDiagnostic(long Id, long cNum, long lNum, String msg) {
//		Statement stmt = null;
//		ResultSet rs = null;
		
		try {
			PreparedStatement statement = conn.prepareStatement(
					"insert into diagnostic(column_number, line_number, message, file_id, version) " +
					"values(" + cNum +"," + lNum + ",'" + msg + "', " + Id + ", 1)"
					);
			statement.executeUpdate();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public String getFileName(Long Id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String filename = null;
		
		try {
			String sql = "select filename from file_revision where id = " + Id;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				filename = rs.getString(1);
			}
			stmt.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return filename;
	}

	public File getFile(Long Id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Clob xmlCont = null;

		try {
			String sql = "select fileData from file_revision where id = " + Id;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				xmlCont = rs.getClob(1);
			}
			stmt.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		String clobText = "";

		try {
			clobText = clobToString(xmlCont);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File newFile = null;
		
		try {
			FileWriter f = new FileWriter(getFileName(Id));
			BufferedWriter bw = new BufferedWriter(f);
			bw.write(clobText);
			bw.close();
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new File(getFileName(Id));
	}

	// From StackOverflow
	// https://stackoverflow.com/questions/29292626/read-and-write-large-clob-files-from-database-faster
	public static String clobToString(final Clob clob) throws SQLException, IOException {

		if (clob == null) {
			return "";
		}

		Long length = null;

		// try to get the oracle specific CLOB length
		// no vendor-specific code here.
		try {
			final Class<?> oracleClobClass = Class.forName("oracle.sql.CLOB");
			if (oracleClobClass.isInstance(clob)) {
				length = (Long) oracleClobClass.getMethod("getLength", null).invoke(clob, null);
			}
		} catch (final Exception e) {
		}

		// we can set initial capacity if we got the length.
		final StringBuilder builder = length == null ? new StringBuilder() : new StringBuilder(length.intValue());

		final BufferedReader reader = new BufferedReader(clob.getCharacterStream());
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line + '\n');
		}

		return builder.toString();
	}
}
