package software_masters.planner_networking;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lee Kendall
 * @author Wesley Murray 
 */

public class ServerImplementation implements Server
{

	private ConcurrentHashMap<String, Account> loginMap = new ConcurrentHashMap<String, Account>();
	private ConcurrentHashMap<String, Account> cookieMap = new ConcurrentHashMap<String, Account>();
	private ConcurrentHashMap<String, Department> departmentMap = new ConcurrentHashMap<String, Department>();
	private ConcurrentHashMap<String, PlanFile> planTemplateMap = new ConcurrentHashMap<String, PlanFile>();

	/**
	 * Initializes server with default objects for testing purposes.
	 */
	public ServerImplementation() throws RemoteException
	{
		Department dpt = new Department("default");
		this.departmentMap.put("default", dpt);

		Account admin = new Account("admin", "admin", "0", dpt, true);
		Account user = new Account("user", "user", "1", dpt, false);
		this.loginMap.put("admin", admin);
		this.loginMap.put("user", user);
		this.cookieMap.put("0", admin);
		this.cookieMap.put("1", user);

		Plan plan = new Centre();
		plan.setName("Centre_Plan_1");
		PlanFile planfile = new PlanFile("2019", true, plan);
		dpt.addPlan("2019", planfile);

		Plan plan2 = new VMOSA();
		plan2.setName("VMOSA_Plan_1");
		plan2.getRoot().setData("My Vision is to...");
		plan2.getRoot().getChildren().get(0).setData("My Mission is to...");
		PlanFile planfile2 = new PlanFile("2020", false, plan2);
		dpt.addPlan("2020", planfile2);

		Plan defaultCentre = new Centre();
		Plan defaultVMOSA = new VMOSA();
		this.planTemplateMap.put("Centre", new PlanFile("", true, defaultCentre));
		this.planTemplateMap.put("VMOSA", new PlanFile("", true, defaultVMOSA));

	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#logIn(java.lang.String,
	 * java.lang.String)
	 */
	public String logIn(String username, String password)
	{
		if (!this.loginMap.containsKey(username))// checks username is valid
		{
			throw new IllegalArgumentException("Invalid username and/or password");

		}
		Account userAccount = this.loginMap.get(username);

		String cookie = userAccount.testCredentials(password);
		return cookie;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getPlan(java.lang.String,
	 * java.lang.String)
	 */
	public PlanFile getPlan(String year, String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid

		Account userAccount = this.cookieMap.get(cookie);
		Department department = userAccount.getDepartment();
		if (!department.containsPlan(year))
		{
			throw new IllegalArgumentException("Plan doesn't exist within your department");

		}
		return department.getPlan(year);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * software_masters.planner_networking.Server#getPlanOutline(java.lang.String,
	 * java.lang.String)
	 */
	public PlanFile getPlanOutline(String name, String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid

		if (!this.planTemplateMap.containsKey(name))// checks plan template exists
		{
			throw new IllegalArgumentException("Plan outline doesn't exist");

		}
		return this.planTemplateMap.get(name);

	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#savePlan(software_masters.
	 * planner_networking.PlanFile, java.lang.String)
	 */
	public void savePlan(PlanFile plan, String cookie)
	{
		Integer.parseInt(plan.getYear());
		cookieChecker(cookie);// checks that cookie is valid

		if (plan.getYear() == null)// checks planFile is given a year
		{
			throw new IllegalArgumentException("This planFile needs a year!");
		}

		Account userAccount = this.cookieMap.get(cookie);
		Department dept = userAccount.getDepartment();

		if (dept.containsPlan(plan.getYear()))
		{
			PlanFile oldPlan = dept.getPlan(plan.getYear());
			if (!oldPlan.isCanEdit())// checks planFile is editable
			{
				throw new IllegalArgumentException("Not allowed to edit this plan");
			}

		}
		dept.addPlan(plan.getYear(), plan);
		this.save();
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#addUser(java.lang.String,
	 * java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	public void addUser(String username, String password, String departmentName, boolean isAdmin, String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid and that user is admin
		adminChecker(cookie);

		departmentChecker(departmentName);

		String newCookie = cookieMaker();
		Department newDept = this.departmentMap.get(departmentName);
		Account newAccount = new Account(username, password, newCookie, newDept, isAdmin);
		this.loginMap.put(username, newAccount);
		this.cookieMap.put(newAccount.getCookie(), newAccount);

	}

	/**
	 * Helper method to randomly generate a 25-character cookie. This method
	 * regenerates a cookie if it already exists in the cookieMap.
	 * 
	 * @return String cookie
	 */
	private String cookieMaker()
	{
		int leftLimit = 33; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 25;
		Random random = new Random();
		String generatedString;
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++)
		{
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		generatedString = buffer.toString();

		while (this.cookieMap.containsKey(generatedString))
		{
			buffer = new StringBuilder(targetStringLength);
			for (int i = 0; i < targetStringLength; i++)
			{
				int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
				buffer.append((char) randomLimitedInt);
			}
			generatedString = buffer.toString();
		}
		return generatedString;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#flagPlan(java.lang.String,
	 * java.lang.String, boolean, java.lang.String)
	 */
	public void flagPlan(String departmentName, String year, boolean canEdit, String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid and that user is admin
		adminChecker(cookie);
		departmentChecker(departmentName);

		Department dept = this.departmentMap.get(departmentName);
		if (!dept.containsPlan(year))
		{
			throw new IllegalArgumentException("Plan doesn't exist");

		}

		dept.getPlan(year).setCanEdit(canEdit);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * software_masters.planner_networking.Server#addDepartment(java.lang.String,
	 * java.lang.String)
	 */
	public void addDepartment(String departmentName, String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid and that user is admin
		adminChecker(cookie);

		this.departmentMap.put(departmentName, new Department(departmentName));

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * software_masters.planner_networking.Server#addPlanTemplate(java.lang.String,
	 * software_masters.planner_networking.PlanFile)
	 */
	public void addPlanTemplate(String name, PlanFile plan)
	{
		this.planTemplateMap.put(name, plan);
	}

	/**
	 * Loads server from xml, called in main
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ServerImplementation load() throws FileNotFoundException
	{
		String filepath = "PlannerServer.serv";
		XMLDecoder decoder = null;
		decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filepath)));
		ServerImplementation server = (ServerImplementation) decoder.readObject();
		decoder.close();
		return server;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#save()
	 */
	public void save()
	{
		String filename = "PlannerServer.serv";
		XMLEncoder encoder = null;
		try
		{
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
		}
		catch (FileNotFoundException fileNotFound)
		{
			System.out.println("ERROR: While Creating or Opening the File " + filename);
		}
		encoder.writeObject(this);
		encoder.close();

	}

	/**
	 * Checks that the client is logged in with a valid cookie
	 * 
	 * @param cookie
	 * @throws IllegalArgumentException
	 */
	private void cookieChecker(String cookie)
	{
		if (!this.cookieMap.containsKey(cookie))
		{
			throw new IllegalArgumentException("Need to log in");

		}

	}

	/**
	 * Checks that the user is an admin
	 * 
	 * @param cookie
	 * @throws IllegalArgumentException
	 */
	private void adminChecker(String cookie)
	{
		if (!this.cookieMap.get(cookie).isAdmin())// Checks that user is admin
		{
			throw new IllegalArgumentException("You're not an admin");
		}
	}
	
	/**
	 * Checks that the user is an admin, can return the state to the client
	 * @param cookie
	 */
	public boolean adminCheckerReturn(String cookie)
	{
		return this.cookieMap.get(cookie).isAdmin();
	}

	/**
	 * Checks department is valid
	 * 
	 * @param name
	 * @throws IllegalArgumentException
	 */
	private void departmentChecker(String name)
	{
		if (!this.departmentMap.containsKey(name))
		{
			throw new IllegalArgumentException("Deparment doesn't exist");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getLoginMap()
	 */
	public ConcurrentHashMap<String, Account> getLoginMap()
	{
		return loginMap;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * software_masters.planner_networking.Server#setLoginMap(java.util.concurrent.
	 * ConcurrentHashMap)
	 */
	public void setLoginMap(ConcurrentHashMap<String, Account> loginMap)
	{
		this.loginMap = loginMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getCookieMap()
	 */
	public ConcurrentHashMap<String, Account> getCookieMap()
	{
		return cookieMap;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * software_masters.planner_networking.Server#setCookieMap(java.util.concurrent.
	 * ConcurrentHashMap)
	 */
	public void setCookieMap(ConcurrentHashMap<String, Account> cookieMap)
	{
		this.cookieMap = cookieMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getDepartmentMap()
	 */
	public ConcurrentHashMap<String, Department> getDepartmentMap()
	{
		return departmentMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#setDepartmentMap(java.util.
	 * concurrent.ConcurrentHashMap)
	 */
	public void setDepartmentMap(ConcurrentHashMap<String, Department> departmentMap)
	{
		this.departmentMap = departmentMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getPlanTemplateMap()
	 */
	public ConcurrentHashMap<String, PlanFile> getPlanTemplateMap()
	{
		return planTemplateMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#setPlanTemplateMap(java.util.
	 * concurrent.ConcurrentHashMap)
	 */
	public void setPlanTemplateMap(ConcurrentHashMap<String, PlanFile> planTemplateMap)
	{
		this.planTemplateMap = planTemplateMap;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#listPlanTemplates()
	 */
	public Collection<PlanFile> listPlanTemplates()
	{
		Collection<PlanFile> collections = planTemplateMap.values();
		LinkedList<PlanFile> list = new LinkedList<PlanFile>();
		Iterator<PlanFile> iter = collections.iterator();
		PlanFile temp;
		String name;
		for (Enumeration<String> e = planTemplateMap.keys(); e.hasMoreElements();)
		{
			name = e.nextElement();
			temp = iter.next();
			list.add(new PlanFile(name, temp.isCanEdit(), null));
		}
		collections = (Collection<PlanFile>) list;
		return collections;
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#listPlans(java.lang.String)
	 */
	public Collection<PlanFile> listPlans(String cookie)
	{
		cookieChecker(cookie);// checks that cookie is valid

		Account userAccount = this.cookieMap.get(cookie);
		Department department = userAccount.getDepartment();

		Collection<PlanFile> collections = department.getPlanFileMap().values();
		LinkedList<PlanFile> list = new LinkedList<PlanFile>();
		PlanFile temp;
		for (Iterator<PlanFile> iter = collections.iterator(); iter.hasNext();)
		{
			temp = iter.next();
			list.add(new PlanFile(temp.getYear(), temp.isCanEdit(), null));
		}
		collections = (Collection<PlanFile>) list;
		return collections;
	}
	
	/**
	 * Retrieves the department name of the user
	 * 
	 * @param cookie
	 * @return department name of user
	 */
	public String getUserDepartment(String cookie)
	{
		Account user = this.getCookieMap().get(cookie);
		Department department = user.getDepartment();
		return department.getDepartmentName();
	}

	/*
	 * (non-Javadoc)
	 * @see software_masters.planner_networking.Server#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerImplementation other = (ServerImplementation) obj;
		if (cookieMap == null)
		{
			if (other.cookieMap != null)
				return false;
		}
		else
			if (!ServerImplementation.<String, Account>hashesEqual(cookieMap, other.cookieMap))
				return false;
		if (departmentMap == null)
		{
			if (other.departmentMap != null)
				return false;
		}
		else
			if (!ServerImplementation.<String, Department>hashesEqual(departmentMap, other.departmentMap))
				return false;
		if (loginMap == null)
		{
			if (other.loginMap != null)
				return false;
		}
		else
			if (!ServerImplementation.<String, Account>hashesEqual(loginMap, other.loginMap))
				return false;
		if (planTemplateMap == null)
		{
			if (other.planTemplateMap != null)
				return false;
		}
		else
			if (!ServerImplementation.<String, PlanFile>hashesEqual(planTemplateMap, other.planTemplateMap))
				return false;
		return true;
	}

	/**
	 * Compares two hashes for testing
	 * 
	 * @param map1
	 * @param map2
	 * @return
	 */
	private static <K, V> boolean hashesEqual(ConcurrentHashMap<K, V> map1, ConcurrentHashMap<K, V> map2)
	{
		for (Enumeration<K> keyList = map1.keys(); keyList.hasMoreElements();)
		{
			K key = keyList.nextElement();
			if (!map1.containsKey(key))
				return false;
			if (!map2.containsKey(key))
				return false;
			if (!map1.get(key).equals(map2.get(key)))
				return false;
		}
		return true;
	}
	
	
	/**
	 * Attribute for singleton pattern 
	 */
	private static Server server=null;
	
	/**
	 * Helper static method that allows us to use singleton pattern for testing.
	 * Ensures server being tested is an object with known values.
	 */
	public static void testSpawn() {
		if (server == null) {
			System.out.println("Starting New Server");
			Registry registry = null;
			Server stub=null;
			try {
				server = new ServerImplementation();
				registry = LocateRegistry.createRegistry(1060);
				stub = (Server) UnicastRemoteObject.exportObject(server, 0);
				registry.bind("PlannerServer", stub);
			} catch (RemoteException e) {
				System.out.println("Unable to create and bind to server using rmi.");
				System.exit(0);
			} catch (AlreadyBoundException e) {
				System.out.println("Connecting to Existing Server");
			}
			return;
		}
		System.out.println("Connecting to Existing Server");
	}
	
	/**
	 * Helper static method that enforces a modified singleton pattern for program.
	 * Constructor is not private because XML serialization.
	 */
	public static void spawn() {
		if (server == null) {
			System.out.println("Starting New Server");
			Registry registry = null;
			Server stub=null;
			try {
				server = ServerImplementation.load();
				registry = LocateRegistry.createRegistry(1060);
				stub = (Server) UnicastRemoteObject.exportObject(server, 0);
				registry.bind("PlannerServer", stub);
			} catch (RemoteException e) {
				System.out.println("Unable to create and bind to server using rmi.");
				System.exit(1);
			} catch (AlreadyBoundException e) {
				System.out.println("Connecting to Existing Server");
			} catch (FileNotFoundException e) {
				System.out.print("Cannot find file to load server from.");
				System.exit(1);
			}
			return;
		}
		System.out.println("Connecting to Existing Server");
	}

	/**
	 * Starts the server, allows clients to access it
	 * 
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException
	{
		ServerImplementation.spawn();
	}
	
	/* (non-Javadoc)
	 * @see software_masters.planner_networking.Server#getUsername(java.lang.String)
	 */
	public String getUsername(String cookie)
	{
		return this.cookieMap.get(cookie).getUserName();
	}

}
