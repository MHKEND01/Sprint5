/**
 * 
 */
package planSelectionView;

import javafx.fxml.FXML;

/**
 * @author lee.kendall
 * 
 * Plan selection view for admins: includes mark editable plans button
 *
 */
public class PlanSelectionControllerAdminOnly extends PlanSelectionViewController {
	
	/**
	 * Opens the markEditablePlans view for admins.
	 */
	@FXML
	public void openMarkEditablePlans(){
		app.showMarkEditablePlansView();
	}

}
