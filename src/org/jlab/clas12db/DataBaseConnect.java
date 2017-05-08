/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel
 *  `------'
*/
package org.jlab.clas12db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBaseConnect {

	private Connection connection;
	private Statement statement;
	private ResultSet rSet;

	public DataBaseConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/udemy", "root", "");
			statement = connection.createStatement();
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}

	}

	public void getData() {
		try {

			String query = "select * from students";
			rSet = statement.executeQuery(query);
			System.out.println("Records from Database");
			while (rSet.next()) {
				// String colorLeft = rSet.getString("colorleft");
				// String colorRight = rSet.getString("colorright");
				// System.out.println("ColorLeft " + colorLeft + "| ColorRight " + colorRight);
				int id = rSet.getInt("id");
				String name = rSet.getString("name");
				int age = rSet.getInt("age");

				System.out.println(id + "  " + name + "  " + age);
			}
		} catch (Exception e) {
			System.out.println("Error at getData: " + e);
		}
	}

	public static void main(String[] args) {
		DataBaseConnect connect = new DataBaseConnect();
		connect.getData();

		// DatabaseConstantProvider provider = new DatabaseConstantProvider(10, "default");
		// IndexedTable table = provider.readTable("/test/fc/fadc");
		// // table.addConstrain(3, 0.0, 90.0);
		// provider.disconnect();
		// JFrame frame = new JFrame();
		// frame.setSize(600, 600);
		//
		// /*
		// * table.addRowAsDouble(new String[]{"21","7","1","0.5","0.1","0.6"}); table.addRowAsDouble(new String[]{"22","8","2","0.6","0.2","0.7"});
		// * table.addRowAsDouble(new String[]{"23","9","3","0.7","0.3","0.8"}); table.addRowAsDouble(new String[]{"24","10","4","0.8","0.4","0.9"});
		// */
		// // table.readFile("/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/EC.table");
		// // table.show();
		// IndexedTableViewer canvas = new IndexedTableViewer(table);
		// frame.add(canvas);
		// frame.pack();
		// frame.setVisible(true);
		// table.show();

	}

}
