/**
 * 
 */
package planReadOnlyView;

import java.util.ArrayList;

import application.Main;
import software_masters.planner_networking.Node;
import software_masters.planner_networking.PlanFile;

/**
 * View for viewing differences between selected plans.
 * @author lee.kendall
 */
public class PlanReadOnlyComparisonViewController extends PlanReadOnlyViewController 
{
	private ArrayList<Node> changedNodes = new ArrayList<Node>();
	
	/**
	 * Finds nodes which are different between the viewed plan and the compared plan
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
			
			if(viewedNode.getData()!=comparedNode.getData() 
					|| viewedNode.getName() != comparedNode.getName())
			{
				changedNodes.add(viewedNode); //Different data content or names
			}
		}
	}
	
}
