package software_masters.planner_networking;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author lee Kendall
 * @author Wes Murray Verifies that client methods work correctly.
 */
public class LocalClientTest
{

	/**
	 * The server is initialized with two accounts - an Admin(Username: admin,
	 * password: admin, cookie: 0) and a normal user (Username: user, password:
	 * user, cookie: 1) The server is initialized with one department - (name:
	 * default) The default department has a default plan file - (year: "2019",
	 * candEdit: true, Plan Centre_Plan_1) planTemplateMap is initialized with VMOSA
	 * and Centre templates
	 */
	static Server testServer;
	static Client testClient;
	static Server actualServer;
	static Registry registry;

	/**
	 * @throws Exception
	 *                       Sets up RMI registry, ensures that a server is pulled
	 *                       from the registry, and sets up a client. The server and
	 *                       client are used for subsequent tests.
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.out.println("Starting Test");
		try
		{
			registry = LocateRegistry.createRegistry(1075);
			ServerImplementation server = new ServerImplementation();
			actualServer = server;
			Server stub = (Server) UnicastRemoteObject.exportObject(server, 0);
			registry.rebind("PlannerServer", stub);
			testServer = (Server) registry.lookup("PlannerServer");
			testClient = new Client(testServer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		registry.unbind("PlannerServer");
		// Unexport; this will also remove us from the RMI runtime
		UnicastRemoteObject.unexportObject(registry, true);
		System.out.println("Closing RMI Server");
	}

	/**
	 * Verifies that the login method works by returning a valid cookie from a valid
	 * login.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testLogin() throws IllegalArgumentException, RemoteException
	{

		// Checks invalid cases
		assertThrows(IllegalArgumentException.class, () -> testClient.login("invalidUsername", "invalidPassword"));
		assertThrows(IllegalArgumentException.class, () -> testClient.login("user", "invalidPassword"));
		assertThrows(IllegalArgumentException.class, () -> testClient.login("invalidUsername", "user"));

		// Checks valid login
		testClient.login("user", "user");
		assertEquals("1", testClient.getCookie());
		testClient.login("admin", "admin");
		assertEquals("0", testClient.getCookie());
	}

	/**
	 * Verifies addUser method works and that only admins can call it
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testAddUser() throws IllegalArgumentException, RemoteException
	{

		// tests non-admin addUser
		testClient.login("user", "user");
		assertThrows(IllegalArgumentException.class, () -> testClient.addUser("newUser", "newUser", "default", false));

		// tests admin can add new account.
		testClient.login("admin", "admin");
		testClient.addUser("newUser", "newUser", "default", false);

		// test if account was actually created and correct department was added
		ConcurrentHashMap<String, Account> loginMap = actualServer.getLoginMap();
		assertTrue(loginMap.containsKey("newUser"));
		ConcurrentHashMap<String, Department> departmentMap = actualServer.getDepartmentMap();
		assertEquals(departmentMap.get("default"), loginMap.get("newUser").getDepartment());

		// Verifies an exception is thrown when admins attempt to add a user with a
		// non-existent department
		testClient.login("admin", "admin");
		assertThrows(IllegalArgumentException.class,
				() -> testClient.addUser("anotherUser", "anotherUser", "invalidDepartment", false));

	}

	/**
	 * Verifies addDepartment method works and that only admins can call it
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testAddDepartment() throws IllegalArgumentException, RemoteException
	{
		// tests non-admin addDepartment
		testClient.login("user", "user");
		assertThrows(IllegalArgumentException.class, () -> testClient.addDepartment("newDepartment"));

		// tests admin can add new department.
		testClient.login("admin", "admin");
		testClient.addDepartment("newDepartment");

		// verifies the department object was created and added to hash
		ConcurrentHashMap<String, Department> departmentMap = actualServer.getDepartmentMap();
		assertTrue(departmentMap.containsKey("newDepartment"));

	}

	/**
	 * This method verifies that only admins can flag a plan as editable.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testFlagPlan() throws IllegalArgumentException, RemoteException
	{
		// tests non-admin flagFile
		testClient.login("user", "user");
		assertThrows(IllegalArgumentException.class, () -> testClient.flagPlan("default", "2019", false));

		// tests admin can flag file.
		testClient.login("admin", "admin");
		testClient.flagPlan("default", "2019", true);
		ConcurrentHashMap<String, Department> departmentMap = actualServer.getDepartmentMap();
		PlanFile file = departmentMap.get("default").getPlan("2019");
		assertTrue(file.isCanEdit());

		// tests exception is thrown if try to flag invalid file.
		assertThrows(IllegalArgumentException.class, () -> testClient.flagPlan("default", "2000", true));

	}

	/**
	 * This method verifies that the client can retrieve plans that exist. Throws
	 * exception if plan does not exist.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testGetPlan() throws IllegalArgumentException, RemoteException
	{
		// plan does not exist throws exception
		testClient.login("user", "user");
		assertThrows(IllegalArgumentException.class, () -> testClient.getPlan("2000"));

		// verify obtained plan is as expected
		ConcurrentHashMap<String, Department> departmentMap = actualServer.getDepartmentMap();
		testClient.getPlan("2019");
		assertEquals(departmentMap.get("default").getPlan("2019"), testClient.getCurrPlanFile());
	}

	/**
	 * This method verifies that the client can retrieve plan outlines that exist.
	 * Throws exception if plan outline does not exist.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testGetPlanOutline() throws RemoteException
	{
		testClient.login("user", "user");

		// build expected file.
		PlanFile centreBase = new PlanFile(null, true, new Centre());

		// test that retrieved business plan outline matches expected
		testClient.getPlanOutline("Centre");
		assertTrue(centreBase.getPlan().getRoot().testEquals(testClient.getCurrPlanFile().getPlan().getRoot()));

		// if plan outline does not exist throw exception
		assertThrows(IllegalArgumentException.class, () -> testClient.getPlanOutline("invalid_outline"));
	}

	/**
	 * Verifies the client can push plans if and only if the planfile flag canEdit
	 * is true.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testpushPlan() throws IllegalArgumentException, RemoteException
	{
		// change canEdit flag to false for default planfile
		testClient.login("admin", "admin");
		testClient.flagPlan("default", "2019", false);

		// edit existing default planfile
		testClient.login("user", "user");
		testClient.getPlan("2019");
		PlanFile test = testClient.getCurrPlanFile();
		Plan temp = test.getPlan(); // if issues later verify shallow not deep copy
		temp.setName("Centre_Plan_2");

		// throws exception when pushing edited planfile if canEdit is false
		assertThrows(IllegalArgumentException.class, () -> testClient.pushPlan(test));

		// change can edit flag to true for default planfile
		testClient.login("admin", "admin");
		testClient.flagPlan("default", "2019", true);

		// verifies test file now on server is the same as the object that was pushed
		testClient.login("user", "user");
		testClient.pushPlan(test);
		testClient.getPlan("2019");
		assertTrue(test.getPlan().getRoot().testEquals(testClient.getCurrPlanFile().getPlan().getRoot()));

	}

	/**
	 * verifies client can add a branch to plan only if the root of that branch is
	 * allowed to be copied.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testAddBranch() throws IllegalArgumentException, RemoteException
	{
		testClient.login("user", "user");
		//////////////////////////////////// Centre
		//////////////////////////////////// example/////////////////////////////////////////////
		testClient.getPlan("2019");
		PlanFile test = testClient.getCurrPlanFile();
		Node root = test.getPlan().getRoot();
		// try adding second mission should throw exception
		testClient.setCurrNode(root);
		assertThrows(IllegalArgumentException.class, () -> testClient.addBranch());
		// try adding second goal
		testClient.setCurrNode(root.getChildren().get(0));// at goal level
		testBranchCopy();
		/////////////////////////////////// VMOSA
		/////////////////////////////////// example///////////////////////////////////////////////
		Plan VMOSA_test = new VMOSA();
		root = VMOSA_test.getRoot();
		PlanFile vmosaTest = new PlanFile("2018", true, VMOSA_test);
		testClient.setCurrPlanFile(vmosaTest);
		// try adding second mission should throw exception
		testClient.setCurrNode(root.getChildren().get(0));
		assertThrows(IllegalArgumentException.class, () -> testClient.addBranch());
		// try adding second objective
		testClient.setCurrNode(testClient.getCurrNode().getChildren().get(0));// at objective level
		testBranchCopy();
		/////////////////////////////////// Iowa state
		/////////////////////////////////// example///////////////////////////////////////////////
		Plan IOWA_test = new IowaState();
		root = IOWA_test.getRoot();
		PlanFile iowaTest = new PlanFile("2017", true, IOWA_test);
		testClient.setCurrPlanFile(iowaTest);
		// try adding second mission should throw exception
		testClient.setCurrNode(root.getChildren().get(0));
		assertThrows(IllegalArgumentException.class, () -> testClient.addBranch());
		// try adding second core value
		testClient.setCurrNode(testClient.getCurrNode().getChildren().get(0));// at core value level
		testBranchCopy();
	}

	/**
	 * This is a helper method. It tests that a branch is added to plan. It also
	 * verifies that the new branch is a deep copy of the original branch
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	private void testBranchCopy() throws IllegalArgumentException, RemoteException
	{
		testClient.addBranch();
		assertTrue(testClient.getCurrNode().testEquals(testClient.getCurrNode().getParent().getChildren().get(1)));
		
		// assures deep copy not shallow. this is tested by changing one copy and
		// verifying that the original was not changed.
		testClient.getCurrNode().getParent().getChildren().get(1).setData("some text");
		assertFalse(testClient.getCurrNode().testEquals(testClient.getCurrNode().getParent().getChildren().get(1)));
		
	}

	/**
	 * This method verifies that the centre template enforces remove branch
	 * constraints. Cannot remove node if only one exists.
	 * 
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testCentreRemoveBranch() throws IllegalArgumentException, RemoteException
	{
		testClient.login("user", "user");
		//////////////////////////////////// Centre
		//////////////////////////////////// example/////////////////////////////////////////////
		testClient.getPlan("2019");
		PlanFile test = testClient.getCurrPlanFile();
		Node root = test.getPlan().getRoot();
		// try removing mission should throw exception
		testClient.setCurrNode(root);// mission level
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// try removing goal should throw exception bc only one exists
		testClient.setCurrNode(root.getChildren().get(0));// goal level
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// add second goal and verify that it can be removed
		testClient.setCurrNode(root.getChildren().get(0));// at goal level
		testBranchCopy();
		testClient.removeBranch();
		assertEquals("some text", testClient.getCurrNode().getParent().getChildren().get(0).getData());
	}

	/**
	 * This method verifies that the VMOSA template enforces remove branch
	 * constraints. Cannot remove node if only one exists.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testVMOSARemoveBranch() throws RemoteException
	{
		/////////////////////////////////// VMOSA
		/////////////////////////////////// example///////////////////////////////////////////////
		Plan VMOSA_test = new VMOSA();
		Node root = VMOSA_test.getRoot();
		PlanFile vmosaTest = new PlanFile("2018", true, VMOSA_test);
		testClient.setCurrPlanFile(vmosaTest);
		// try removing mission should throw exception
		testClient.setCurrNode(root.getChildren().get(0));
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// try removing objective should throw exception bc only one exists
		testClient.setCurrNode(testClient.getCurrNode().getChildren().get(0));// objective level
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// add second objective
		testClient.setCurrNode(testClient.getCurrNode());// at objective level
		testBranchCopy();
		testClient.removeBranch();
		assertEquals("some text", testClient.getCurrNode().getParent().getChildren().get(0).getData());
	}

	/**
	 * This method verifies that the Iowa template enforces remove branch
	 * constraints. Cannot remove node if only one exists.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testIowaRemoveBranch() throws RemoteException
	{
		/////////////////////////////////// Iowa state
		/////////////////////////////////// example///////////////////////////////////////////////
		Plan IOWA_test = new IowaState();
		Node root = IOWA_test.getRoot();
		PlanFile iowaTest = new PlanFile("2017", true, IOWA_test);
		testClient.setCurrPlanFile(iowaTest);
		// try remove mission should throw exception
		testClient.setCurrNode(root.getChildren().get(0));
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// try removing core value should throw exception bc only one exists
		testClient.setCurrNode(testClient.getCurrNode().getChildren().get(0));// objective level
		assertThrows(IllegalArgumentException.class, () -> testClient.removeBranch());
		// add second core value
		testClient.setCurrNode(testClient.getCurrNode().getChildren().get(0));// at core value level
		testBranchCopy();
		testClient.removeBranch();
		assertEquals("some text", testClient.getCurrNode().getParent().getChildren().get(0).getData());
	}

}
