/**
 * 
 */
package software_masters.gui_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.jupiter.api.Test;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.service.query.NodeQuery;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import software_masters.planner_networking.PlanFile;

/**
 * @author lee.kendall
 *
 */
class PlanComparisonTest extends GuiTestBase {

	@Test
	void test() 
	{
		setupTestCases();
		testDefaults();
		testBackButtons();
		testPopup();
		testDifferences();
		testDifferencesSwapOrder();
	}
	


	/**
	 * Sets up two plans for testing: They have different vision data, different mission names,
	 * and a different number of strategy sections.
	 */
	private void setupTestCases()
	{
		clickOn("Connect");
		clickOn("#loginButton");
		clickOn("VMOSA");
		clickOn("#dataField");
		write("Vision 1 Data Edit");
		doubleClickOn("Vision");
		doubleClickOn("Mission");
		clickOn("#nameField");
		write(" 1 Name Edit");
		doubleClickOn("Objective");
		doubleClickOn("Strategy");
		clickOn("#addSectionButton");
		clickOn("#editYearField");
		write("1991");
		clickOn("#saveButton");
		clickOn("#backToPlansButton");
		
		clickOn("VMOSA");
		clickOn("#dataField");
		write("Vision 2 Data Edit");
		doubleClickOn("Vision");
		doubleClickOn("Mission");
		clickOn("#nameField");
		write(" 2 Name Edit");
		clickOn("#editYearField");
		write("1992");
		clickOn("#saveButton");
		clickOn("#backToPlansButton");
		
	}
	
	/**
	 *Verifies label and buttons exist and have the intended text. 
	 */
	private void testDefaults()
	{
		clickOn("Compare Plans");
		verifyThat("#compareButton", LabeledMatchers.hasText("Compare"));
		verifyThat("#backToPlansButton", LabeledMatchers.hasText("Back to plans"));
		verifyThat("#logoutButton", LabeledMatchers.hasText("Logout"));
		verifyThat("#instructionsLabel", LabeledMatchers.hasText("Select two plans, then click compare"));
	}
	
	/**
	 * Verifies "back to plans" and logout buttons work correctly from the plan comparison selection
	 * view.
	 */
	private void testBackButtons()
	{
		clickOn("Back to plans");
		verifyThat("#selectPlanLabel", LabeledMatchers.hasText("Select Plan"));
		clickOn("Compare Plans");
		clickOn("Logout");
		verifyThat("#usernameLabel", LabeledMatchers.hasText("Username"));
		clickOn("#loginButton");
		clickOn("Compare Plans");
	}
	
	/**
	 * Verifies warning popup is displayed if users select no plans or only one plan.
	 */
	private void testPopup()
	{
		clickOn("Compare");
		checkPopupMsg("You must select a plan from both lists");
		clickOn("OK");
		clickOn("1991");
		clickOn("Compare");
		checkPopupMsg("You must select a plan from both lists");
		clickOn("OK");
	}
	
	/**
	 * Ensures only the nodes which are actually different are marked as different.
	 */
	private void testDifferences()
	{
		//Selects the 1992 planfile from the second listview
		NodeQuery target = from(lookup("#listViewTwo")).lookup("1992");
		Node targetNode = target.query();
		
		//Verify node marked as changed in response to data field changes...
		clickOn(targetNode);
		clickOn("Compare");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
				+ "difference between this section and the corresponding node on the compared "
				+ "plan. This could be a difference in either section content, section name, "
				+ "or the number of subsections each section has."));
		
		//Verify node marked as changed in response to name field changes...
		doubleClickOn("Vision");
		doubleClickOn("Mission 1 Name Edit");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
				+ "difference between this section and the corresponding node on the compared "
				+ "plan. This could be a difference in either section content, section name, "
				+ "or the number of subsections each section has."));
		
		//Verify node marked as changed in response to different number of children...
		doubleClickOn("Objective");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
				+ "difference between this section and the corresponding node on the compared "
				+ "plan. This could be a difference in either section content, section name, "
				+ "or the number of subsections each section has."));
		
		//Verifies identical nodes are not marked as different.
		doubleClickOn("Strategy");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
				+ " and the compared plan section."));
		doubleClickOn("Action Plan");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
				+ " and the compared plan section."));
		doubleClickOn("Assessment");
		verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
				+ " and the compared plan section."));
		clickOn("Back to plans");
		clickOn("Compare Plans");

	}
	
	/**
	 * Ensures that nodes with differences are still correctly marked when the order in which plans
	 * are selected from the comparison selection view are swapped. 
	 */
	private void testDifferencesSwapOrder() {
		
		clickOn("1992");
		//Selects the 1991 planfile from the second listview
				NodeQuery target = from(lookup("#listViewTwo")).lookup("1991");
				Node targetNode = target.query();
				//Verify node marked as changed in response to data field changes...
				clickOn(targetNode);
				clickOn("Compare");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
						+ "difference between this section and the corresponding node on the compared "
						+ "plan. This could be a difference in either section content, section name, "
						+ "or the number of subsections each section has."));
				
				//Verify node marked as changed in response to name field changes...
				doubleClickOn("Vision");
				doubleClickOn("Mission 2 Name Edit");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
						+ "difference between this section and the corresponding node on the compared "
						+ "plan. This could be a difference in either section content, section name, "
						+ "or the number of subsections each section has."));
				
				//Verify node marked as changed in response to different number of children...
				doubleClickOn("Objective");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("There is at least one "
						+ "difference between this section and the corresponding node on the compared "
						+ "plan. This could be a difference in either section content, section name, "
						+ "or the number of subsections each section has."));
				
				//Verifies identical nodes are not marked as different.
				doubleClickOn("Strategy");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
						+ " and the compared plan section."));
				doubleClickOn("Action Plan");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
						+ " and the compared plan section."));
				doubleClickOn("Assessment");
				verifyThat("#comparisonLabel", LabeledMatchers.hasText("No changes between this plan section"
						+ " and the compared plan section."));
		
	 }

}
