/**
 * 
 */
package software_masters.planner_networking;

/**
 * AdminState for users with admin privileges 
 * 
 * @author lee.kendall
 *
 */
public class Admin implements AdminState {

	private Client client;
	
	public Admin(Client client)
	{
		this.client = client;
	}
	
	/* (non-Javadoc)
	 * @see software_masters.planner_networking.AdminState#getViewPath()
	 */
	@Override
	public String getViewPath() {
		return "../planSelectionView/planSelectionViewAdmins.fxml";
	}
	
	/**
	 * Changes client's state from admin to user when admins logout.
	 */
	public void changeState()
	{
		AdminState state = new User(client);
		client.setState(state);
	}

}
