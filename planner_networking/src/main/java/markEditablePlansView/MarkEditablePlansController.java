package markEditablePlansView;

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
 * Handles the view from which admins mark plans as editable or read-only.
 * @author lee.kendall
 *
 */
public class MarkEditablePlansController {
	
	    @FXML
	    private Button markEditableButton;

	    @FXML
	    private Button markReadOnlyButton;

	    @FXML
	    private Button backToPlansButton;

	    @FXML
	    private Button logoutButton;
	    
	    @FXML
		private ListView<PlanFile> departmentPlanList;
	    
	    private Main app;
	    
		/**
		 * Allows controller to access the showPlanSelectionView
		 * showLoginView methods in the main application. Also populates the listview
		 * with genPlansList
		 * 
		 * @param app
		 *                main application
		 */
		public void setApplication(Main app)
		{
			this.app = app;
			genPlansList();
		}
		
		/**
		 * This method generates a list view of plans associated with the user's department
		 * 
		 */
		public void genPlansList()
		{
			ObservableList<PlanFile> items = null;
			try
			{
				items = FXCollections.observableArrayList(this.app.getModel().listPlans());
			}
			catch (RemoteException e)
			{
				this.app.showConnectToServer();
			}
			departmentPlanList.setItems(items);

		}
		
		/**
		 * Marks the selected planfile from the listview as editable. Grabs the planfile's year and the name
		 * of the user's department, then calls client's flagplan method.
		 */
		@FXML
		public void markAsEditable()
		{
			PlanFile selected = this.departmentPlanList.getSelectionModel().getSelectedItem();
			String year = selected.getYear();
			String departmentName = this.app.getModel().getUserDepartment();
			
			try {
				this.app.getModel().flagPlan(departmentName, year, true);
			} catch (IllegalArgumentException | RemoteException e) {
				e.printStackTrace();
			}
			genPlansList();
		}
		
		/**
		 * Marks the selected planfile from the listview as read-only. Grabs the planfile's year and the name
		 * of the user's department, then calls client's flagplan method.
		 */
		@FXML
		public void markAsReadOnly()
		{
			PlanFile selected = this.departmentPlanList.getSelectionModel().getSelectedItem();
			String year = selected.getYear();
			String departmentName = this.app.getModel().getUserDepartment();
			
			try {
				this.app.getModel().flagPlan(departmentName, year, false);
			} catch (IllegalArgumentException | RemoteException e) {
				e.printStackTrace();
			}
			genPlansList();
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


}
