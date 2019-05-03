package planReadOnlyView;

import java.rmi.RemoteException;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import software_masters.model.PlannerModel;
import software_masters.planner_networking.Node;

public class PlanReadOnlyViewController
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
		treeView.getSelectionModel().selectedItemProperty().addListener((v) ->
		{
			changeSection();
		});
		populateFields();
	}

	/**
	 * Log out the current account on the server
	 */
	@FXML
	public void logOut()
	{
		try {
			application.getModel().logout();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		application.showLoginView();
	}

	/**
	 * Change the view back to planSelectionView
	 */
	@FXML
	public void backToPlans()
	{
		// need to ask users if they want to push

		model.setCurrNode(null);
		model.setCurrPlanFile(null);
		application.showPlanSelectionView();
	}

	/**
	 * Filling the treeview with nodes from business plan
	 */
	private void setTreeView()
	{
		treeView.rootProperty().setValue(convertTree(model.getCurrPlanFile().getPlan().getRoot()));
		treeView.getSelectionModel().select(treeView.getRoot());
		model.setCurrNode(model.getCurrPlanFile().getPlan().getRoot());
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
	private void changeSection()
	{
		TreeItem<Node> item = treeView.getSelectionModel().getSelectedItem();
		model.editName(nameField.getText());
		model.editData(dataField.getText());
		model.setCurrNode(item.getValue());
		nameField.setText(model.getCurrNode().getName());
		dataField.setText(model.getCurrNode().getData());
		treeView.refresh();
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
}
