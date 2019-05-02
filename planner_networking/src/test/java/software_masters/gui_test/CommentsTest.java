package software_masters.gui_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import software_masters.planner_networking.Node;

class CommentsTest extends GuiTestBase {
	

	/**
	 * method that coordinates order of gui test.
	 */
	@Test
	void test() 
	{
		clickOn("Connect");
		getToPlanEditView();
		defaultTest();
		testAddComment();
		testRemoveComment();
		
	}
	
	/**
	 * moves from login screen to the plan edit window in the gui
	 */
	private void getToPlanEditView()
	{
		clickOn("#loginButton");
		sleep(1000);
		clickOn((javafx.scene.Node) find("2019"));

	}
	
	/**
	 * Test that default values exist.
	 */
	private void defaultTest()
	{
		verify("#submitCommentButton","Submit Comment");
		verify("#removeCommentButton","Remove Comment");
	}
	
	private void testAddComment()
	{
		clickOn("#commentField");
		write("I have an idea");
		clickOn("#submitCommentButton");
		sleep(3000);
		
		//will fail if comment not added to list view, with the username concatenated to the comment
		Assert.assertTrue("#commentListView".contains("User: I have an idea"));
		
		//Verifies "isPushed" boolean is changed after commenting - the save warning popup should be displayed
		//if adding a comment without saving.
		clickOn("#logoutButton");
		checkPopupMsg("You have unsaved changes. Do you wish to save before exiting?");
		clickOn("Cancel");
		
		//Verifies list view of comments changes when the user changes sections
		doubleClickOn("Mission");
		clickOn("Goal");
		Assert.assertFalse("#commentListView".contains("User: I have an idea"));
		
		clickOn("#logoutButton");
		clickOn("Yes");
		
	}
	
	private void testRemoveComment()
	{}
	
	
	

}
