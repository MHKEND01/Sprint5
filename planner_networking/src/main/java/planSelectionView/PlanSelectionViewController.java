package planSelectionView;

import java.rmi.RemoteException;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import software_masters.model.PlannerModel;
import software_masters.planner_networking.PlanFile;

/**
 * @author lee.kendall MVC Controller for the plan selection view
 */
public class PlanSelectionViewController
{

	@FXML
	private ListView<PlanFile> planTemplateList;

	@FXML
	private ListView<PlanFile> departmentPlanList;

	protected Main app;

	/**
	 * Resets client's cookie and planFile, displays the login window
	 * 
	 * @param event
	 *                  Action
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
	 * Allows controller to access showPlanEditView, showPlanReadOnlyView, and the
	 * showLoginView methods in the main application. Also populates the listviews
	 * with genTemplateList and genPlansList
	 * 
	 * @param app
	 *                main application
	 */
	public void setApplication(Main app)
	{
		this.app = app;
		genPlansList();
		genTemplateList();
	}

	/**
	 * This method generates a list view of developer templates
	 * 
	 * @return
	 */
	public void genTemplateList()
	{
		ObservableList<PlanFile> items = null;
		try
		{
			items = FXCollections.observableArrayList(this.app.getModel().listPlanTemplates());
		}
		catch (RemoteException e)
		{
			this.app.showConnectToServer();
		}
		planTemplateList.setItems(items);
		
	}

	/**
	 * This method generates a list view of plans associated with the user's department
	 * 
	 * @return
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
	 * This method lists all the plan templates.
	 */
	@FXML
	public void openPlanTemplate()
	{
		PlanFile selected = this.planTemplateList.getSelectionModel().getSelectedItem();
		try
		{
			this.app.getModel().getPlanOutline(selected.getYear());
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("Invalid plan template");
			this.app.showPlanSelectionView();
			return;
		}
		catch (RemoteException e)
		{
			this.app.showConnectToServer();
			return;
		}
		this.app.showPlanEditView();
		return;
	}

	/**
	 * This method lists all the plans associated with the user's department
	 */
	@FXML
	public void openPlan()
	{
		PlanFile selected = this.departmentPlanList.getSelectionModel().getSelectedItem();
		try
		{
			this.app.getModel().getPlan(selected.getYear());
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("Invalid plan template");
			this.app.showPlanSelectionView();
			return;
		}
		catch (RemoteException e)
		{
			this.app.showConnectToServer();
			return;
		}

		if (selected.isCanEdit())
		{
			this.app.showPlanEditView();
			return;
		}
		else
		{
			this.app.showPlanReadOnlyView();
			return;
		}
	}
	
	/**
	 * Opens the plan comparison selection view.
	 */
	@FXML
	public void openPlanComparisonSelectionView()
	{
		this.app.showPlanComparisonSelectionView();
	}

}
