/**
 * 
 */
package planComparisonSelectionView;

import java.rmi.RemoteException;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import software_masters.planner_networking.PlanFile;

/**
 * View for the window allowing users to select two plans for comparison.
 * @author lee.kendall
 */
public class PlanComparisonSelectionViewController {

    @FXML
    private ListView<PlanFile> listViewOne;

    @FXML
    private Button compareButton;

    @FXML
    private ListView<PlanFile> listViewTwo;

    @FXML
    private Button backToPlansButton;

    @FXML
    private Button logoutButton;
    
    private Main app;
    
	/**
	 * Allows controller to access the main application. 
	 * Also populates the listview with genPlanLists.
	 * 
	 * @param app main application
	 */
	public void setApplication(Main app)
	{
		this.app = app;
		genPlansLists();
	}

	/**
	 * Populates listViews with the planfiles stored on that user's department.
	 */
	private void genPlansLists() 
	{
		ObservableList<PlanFile> itemsForListOne = null;
		ObservableList<PlanFile> itemsForListTwo = null;
		try
		{
			itemsForListOne = FXCollections.observableArrayList(this.app.getModel().listPlans());
			itemsForListTwo = FXCollections.observableArrayList(this.app.getModel().listPlans());
		}
		catch (RemoteException e)
		{
			this.app.showConnectToServer();
		}
		listViewOne.setItems(itemsForListOne);
		listViewTwo.setItems(itemsForListTwo);
	}
	
	/**
	 * Resets client's cookie and planFile, displays the login window
	 * 
	 * @param event Action
	 */
	@FXML
	public void Logout(ActionEvent event)
	{
		try {
			app.getModel().logout();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		app.showLoginView();
	}
	
	/**
	 * Change the view back to planSelectionView
	 */
	@FXML
	public void backToPlans()
	{
		app.showPlanSelectionView();

	}
	
	/**
	 * Displays a read-only view of the first selected plan, with changes between each node and the
	 * corresponding node of the second selected plan marked.
	 */
	@FXML
	public void compare()
	{
		PlanFile viewedPlan = listViewOne.getSelectionModel().getSelectedItem();
		PlanFile comparedPlan = listViewTwo.getSelectionModel().getSelectedItem();
		try {
			app.getModel().getPlan(viewedPlan.getYear());
			viewedPlan = app.getModel().returnPlan(viewedPlan.getYear());
			comparedPlan = app.getModel().returnPlan(comparedPlan.getYear());
		} catch (IllegalArgumentException | RemoteException e) {
			e.printStackTrace();
		}
		app.showPlanReadOnlyComparisonView(viewedPlan, comparedPlan);

	}

}
