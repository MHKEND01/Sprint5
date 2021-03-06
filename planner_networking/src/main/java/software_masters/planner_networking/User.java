/**
 * 
 */
package software_masters.planner_networking;

/**
 * AdminState for regular users 
 * @author lee.kendall
 *
 */
public class User implements AdminState {
	
	private Client client;
	
	
	public User(Client client)
	{
		this.client = client;
	}

	/* (non-Javadoc)
	 * @see software_masters.planner_networking.AdminState#getViewPath()
	 */
	@Override
	public String getViewPath() {
		return "../planSelectionView/planSelectionView.fxml";
	}
	
	/**
	 * Changes client's state to admin when an admin account logs in
	 */
	public void changeState()
	{
		AdminState state = new Admin(client);
		client.setState(state);
	}

}
