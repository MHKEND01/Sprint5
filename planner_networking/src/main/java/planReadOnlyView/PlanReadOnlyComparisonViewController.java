/**
 * 
 */
package planReadOnlyView;

import java.util.ArrayList;

import com.sun.prism.paint.Color;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import software_masters.planner_networking.Node;
import software_masters.planner_networking.PlanFile;

/**
 * View for viewing differences between selected plans.
 * @author lee.kendall
 */
public class PlanReadOnlyComparisonViewController extends PlanReadOnlyViewController 
{
	@FXML
    private Label comparisonLabel;
	
	private ArrayList<Node> changedNodes = new ArrayList<Node>();
	
	/**
	 * Finds nodes which are different between the viewed plan and the compared plan
	 * @param viewedPlanFile
	 * @param comparedPlanFile
	 */
	
	/**
	 * Gives controller access to the application, generates the treeView and initializes textfields.
	 * 
	 * @param application
	 */
	public void setApplication(Main application, PlanFile viewedPlanFile, PlanFile comparedPlanFile)
	{
		this.application = application;
		model = this.application.getModel();
		findChangedNodes(viewedPlanFile, comparedPlanFile);
		setTreeView();
		treeView.getSelectionModel().selectedItemProperty().addListener((v) ->
		{
			changeSection();
		});
		populateFields();
	}

	/**
	 * Checks for differences between the two planfiles.
	 * @param viewedPlanFile
	 * @param comparedPlanFile
	 */
	private void  findChangedNodes(PlanFile viewedPlanFile, PlanFile comparedPlanFile)
	{
		Node viewedRoot = viewedPlanFile.getPlan().getRoot();
		Node comparedRoot = comparedPlanFile.getPlan().getRoot();
		findChangedNodesHelper(viewedRoot, comparedRoot);
	}
	
	/**
	 * Helper method which recursively checks for differences between pairs of nodes, adds nodes
	 * to arraylist if differences detected.
	 * @param viewedNode
	 * @param comparedNode
	 */
	private void findChangedNodesHelper(Node viewedNode, Node comparedNode)
	{
		if(viewedNode.getChildren().size() != comparedNode.getChildren().size())
		{
			changedNodes.add(viewedNode); //Different # of children, indicates a difference
		}
		else
		{	
			for(int i=0; i < viewedNode.getChildren().size(); i++)
			{
				Node viewedChild = viewedNode.getChildren().get(i);
				Node comparedChild = comparedNode.getChildren().get(i);
				findChangedNodesHelper(viewedChild, comparedChild);
	
			}
			
			if(!viewedNode.getData().contentEquals(comparedNode.getData()) 
					|| !viewedNode.getName().contentEquals(comparedNode.getName()))
			{
				changedNodes.add(viewedNode); //Different data content or names
			}
		}
	}
	
	/**
	 * Initializes the year, name, and data text fields. Also initializes the comparison label.
	 */
	@Override
	protected void populateFields()
	{
		yearField.setText(model.getCurrPlanFile().getYear());
		nameField.setText(model.getCurrNode().getName());
		dataField.setText(model.getCurrNode().getData());
		setComparisonLabel();
	}
	
	/**
	 * Changes the comparison label text depending on if there are changes detected between the
	 * currently accessed node and the corresponding node in the compared plan.
	 */
	private void setComparisonLabel()
	{
		if(differenceDetected(model.getCurrNode()))
		{
			comparisonLabel.setText("There is at least one difference between this section and the"
					+ " corresponding node on the compared plan. This could be a difference in either"
					+ " section content, section name, or the number of subsections each section has.");
			comparisonLabel.setStyle("-fx-text-fill: #ff0000");
		}
		else
		{
			comparisonLabel.setText("No changes between this plan section and the compared plan section.");
			comparisonLabel.setStyle("-fx-text-fill: Green");
			
		}
	}
	
	/**
	 * Serves as a substitute for the arrayLis ".contains()" method, which compares nodes using the
	 * deafult ".equals()" method. This always returns false because the compared node references are
	 * not pointing at the same object. The custom "testEquals()" method, originally used for testing,
	 * actually compares the contents of each node. Substituting that method for the default equals()
	 * method returns the intended result.
	 * @param currNode
	 * @return
	 */
	private boolean differenceDetected(Node currNode)
	{
		for(int i=0; i<changedNodes.size();i++)
		{
			if(changedNodes.get(i).testEquals(currNode))
			{
				return true;
			}
		}
		return false;
		
	}
	
	/**
	 * Change the nameField and dataField to the content stored in current node. Also updates the
	 * comparison label if changes are associated with that node.
	 * 
	 * @param item
	 */
	@Override
	protected void changeSection()
	{
		TreeItem<Node> item = treeView.getSelectionModel().getSelectedItem();
		model.editName(nameField.getText());
		model.editData(dataField.getText());
		model.setCurrNode(item.getValue());
		nameField.setText(model.getCurrNode().getName());
		dataField.setText(model.getCurrNode().getData());
		treeView.refresh();
		setComparisonLabel();
	}
	
}
