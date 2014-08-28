/** Testbed is the main class. Which creates the CentralAuthority object, used previously
 *  to pass the user selected inputs and defense/attack model to the environment.
 */

package main;
import java.util.*;
public class Testbed {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		CentralAuthority c = new CentralAuthority();

		System.out.println("enters testBed");
		ArrayList<String> def = new ArrayList<String>();
		def.add("TrustedGraph");
		ArrayList<String> attack = new ArrayList();
		attack.add("AlwaysUnfair");
		c.evaluateDefenses(def, attack, null, null);

	}

}
