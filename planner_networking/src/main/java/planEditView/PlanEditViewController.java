package planEditView;

import java.rmi.RemoteException;
import java.util.Optional;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import software_masters.model.PlannerModel;
import software_masters.planner_networking.Node;
import software_masters.planner_networking.PlanFile;

public class PlanEditViewController
{

	Main application;
	PlannerModel model;
	@FXML
	TreeView<Node> treeView;
	@FXML
	TextField nameField;
	@FXML
	TextField dataField;
	@FXML
	TextField yearField;
	@FXML
	TextField commentField;
	@FXML
	ListView<String> commentListView;

	boolean isPushed;

	/**
	 * Let controller to know view
	 * 
	 * @param application
	 */
	public void setApplication(Main application)
	{
		this.application = application;
		model = this.application.getModel();
		setTreeView();
		isPushed = true;
		genComments();
	}

	/**
	 * Delete the current selected node in the treeview. Shows popup asking user if
	 * they actually wish to delete.
	 */
	@FXML
	public void deleteSection()
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		String message = "Are you sure you want to delete this section and all dependencies?"
				+ "They cannot be recovered.";
		alert.setContentText(message);
		ButtonType okButton = new ButtonType("Delete");
		ButtonType noButton = new ButtonType("Don't Delete");
		alert.getButtonTypes().setAll(okButton, noButton);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == okButton)
		{
			try
			{
				this.changeSection();
				model.removeBranch();
				setTreeView();
				isPushed = false;
			}
			catch (IllegalArgumentException e)
			{
				application.sendError("Cannot delete this section");
			}
		}
		else
			if (result.get() == noButton)
			{
			}

	}

	/**
	 * Add a branch at the same level as current selected node to the business plan
	 */
	@FXML
	public void addSection()
	{
		try
		{
			this.changeSection();
			model.addBranch();
			setTreeView();
			isPushed = false;
		}
		catch (RemoteException e)
		{
			application.sendError("Cannot connect to server");
		}
		catch (IllegalArgumentException e)
		{
			application.sendError("Cannot add a section of this type");
		}

	}

	/**
	 * Log out the current account on the server
	 */
	@FXML
	public void logOut()
	{
		// need to ask users if they want to push
		this.changeSection();
		if (this.isPushed)
		{
			model.setCookie(null);
			model.setCurrNode(null);
			model.setCurrPlanFile(null);
			application.showLoginView();
		}
		else
		{
			this.warningToSaveLogout();
		}
	}

	/**
	 * Change the view back to planSelectionView
	 */
	@FXML
	public void backToPlans()
	{
		// need to ask users if they want to push

		this.changeSection();
		if (this.isPushed)
		{
			model.setCurrNode(null);
			model.setCurrPlanFile(null);
			application.showPlanSelectionView();
		}
		else
		{
			this.warningToSaveBackToPlans();
		}
	}

	/**
	 * Push the current business plan to the server
	 */
	@FXML
	public boolean push()
	{
		try
		{
			// set the year to which the user want
			// This allow the user to decide which year they want to edit
			// at editing time
			changeSection();
			model.getCurrPlanFile().setYear(yearField.getText());
			model.pushPlan(model.getCurrPlanFile());
			isPushed = true;
		}
		catch (NumberFormatException e)
		{
			application.sendError("Invalid Year");
			return false;
		}
		catch (IllegalArgumentException e)
		{
			application.sendError("Cannot save changes to this plan");
			return false;
		}
		catch (RemoteException e)
		{
			application.sendError("Cannot connect to server");
			return false;
		}
		return true;
	}

	/**
	 * Filling the treeview with nodes from business plan
	 */
	private void setTreeView()
	{
		treeView.setRoot(convertTree(model.getCurrPlanFile().getPlan().getRoot()));
		treeView.getSelectionModel().select(treeView.getRoot());
		model.setCurrNode(model.getCurrPlanFile().getPlan().getRoot());
		treeView.refresh();
		populateFields();
	}

	/**
	 * @param root
	 *                 build the treeview start from root node of business plan
	 * @return
	 */
	private TreeItem<Node> convertTree(Node root)
	{
		TreeItem<Node> newRoot = new TreeItem<Node>(root);
		for (int i = 0; i < root.getChildren().size(); i++)
		{
			newRoot.getChildren().add(convertTree(root.getChildren().get(i)));
		}
		return newRoot;
	}

	/**
	 * Change the nameField and dataField to the content stored in current node
	 * 
	 * @param item
	 */
	@FXML
	public void changeSection()
	{
		boolean isChanged = !(nameField.getText().equals(model.getCurrNode().getName())
				&& dataField.getText().equals(model.getCurrNode().getData())
				&& yearField.getText().equals(model.getCurrPlanFile().getYear()));
		
		TreeItem<Node> item = treeView.getSelectionModel().getSelectedItem();
		model.editName(nameField.getText());
		model.editData(dataField.getText());
		model.setCurrNode(item.getValue());
		nameField.setText(model.getCurrNode().getName());
		dataField.setText(model.getCurrNode().getData());
		treeView.refresh();
		
		commentField.clear();
		genComments();

		if (isChanged)
		{
			isPushed = false;
		}

	}
	
	/**
	 * Adds a comment to the currently accessed node's list of comments
	 */
	@FXML
	public void addComment()
	{
		String comment = commentField.getText();
		model.addComment(comment);
		genComments();
		commentField.clear();
		isPushed = false;
	}
	
	/**
	 *Removes the comment currently selected in the listview of comments
	 */
	@FXML
	public void removeComment()
	{
		String comment = commentListView.getSelectionModel().getSelectedItem();
		model.removeComment(comment);
		genComments();
		isPushed = false;
	}
	
	/**
	 * Generates the listview of comments for the currently accessed section
	 */
	public void genComments()
	{
		ObservableList<String> items = null;
		items = FXCollections.observableArrayList(model.getCurrNode().getComments());
		commentListView.setItems(items);
	}

	/**
	 * Initializes the year, name, and data text fields.
	 */
	private void populateFields()
	{
		yearField.setText(model.getCurrPlanFile().getYear());
		nameField.setText(model.getCurrNode().getName());
		dataField.setText(model.getCurrNode().getData());
	}

	/**
	 * Asks user if they want to save unsaved changes before leaving the plan edit
	 * view window for the logout button
	 * 
	 * @return result of button press
	 */
	public void warningToSaveLogout()
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		String message = "You have unsaved changes. Do you wish to save before exiting?";
		alert.setContentText(message);
		ButtonType okButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		ButtonType cancelButton = new ButtonType("Cancel");
		alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == okButton)
		{
			if (this.push())
			{
				model.setCookie(null);
				model.setCurrNode(null);
				model.setCurrPlanFile(null);
				application.showLoginView();
			}
		}
		else
			if (result.get() == noButton)
			{
				model.setCookie(null);
				model.setCurrNode(null);
				model.setCurrPlanFile(null);
				application.showLoginView();

			}
			else
				if (result.get() == cancelButton)
				{
				}
	}

	/**
	 * Asks user if they want to save unsaved changes before leaving the plan edit
	 * view window for the back to plans button
	 * 
	 * @return result of button press
	 */
	public void warningToSaveBackToPlans()
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		String message = "You have unsaved changes. Do you wish to save before exiting?";
		alert.setContentText(message);
		ButtonType okButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		ButtonType cancelButton = new ButtonType("Cancel");
		alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == okButton)
		{
			if (this.push())
			{
				model.setCurrNode(null);
				model.setCurrPlanFile(null);
				application.showPlanSelectionView();
			}
		}
		else
			if (result.get() == noButton)
			{
				model.setCurrNode(null);
				model.setCurrPlanFile(null);
				application.showPlanSelectionView();

			}
			else
				if (result.get() == cancelButton)
				{
				}
	}

	/**
	 * @return the isPushed
	 */
	public boolean isPushed()
	{
		return isPushed;
	}
}
