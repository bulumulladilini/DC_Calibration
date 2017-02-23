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

import org.jlab.ccdb.CcdbPackage;
import org.jlab.ccdb.JDBCProvider;
import org.jlab.utils.groups.IndexedTable;

public class TestMySql {

	public static void main(String[] args) {

		String address = "mysql://localhost:3306/test";
		JDBCProvider provider = CcdbPackage.createProvider(address);
		provider.connect();

		if (provider.getIsConnected() == true) {
			if (1 > 0)
				System.out.println("[DB] --->  database connection  : success");
		} else {
			System.out.println("[DB] --->  database connection  : failed");
		}
		IndexedTable table = provider.readTable("/test/hvpintolayer");

	}

}
