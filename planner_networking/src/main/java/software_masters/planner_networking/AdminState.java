/**
 * 
 */
package software_masters.planner_networking;

/**
 * @author lee.kendall
 *
 *Keeps track of information and abilities needed by each level of user - for now,
 *that's just the version of the plan selection view users have access to.
 */
public interface AdminState {
	
	
	/**
	 * Retrieves filepath to planEditView
	 * @return filepath to the correct planEditView
	 */
	public abstract String getViewPath();

}
