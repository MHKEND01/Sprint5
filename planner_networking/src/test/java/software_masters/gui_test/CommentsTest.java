package software_masters.gui_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import software_masters.planner_networking.Node;
import javafx.scene.input.KeyCode;

import org.testfx.api.FxAssert;
import org.testfx.matcher.control.ListViewMatchers;

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
		testAlternativeUser();
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
	
	/**
	 * Tests that users can add comments
	 */
	private void testAddComment()
	{
		clickOn("#commentField");
		write("I have an idea");
		clickOn("#submitCommentButton");
		
		//will fail if comment not added to list view, with the username concatenated to the comment
		 FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: I have an idea"));
		
		//Verifies "isPushed" boolean is changed after commenting - the save warning popup should be displayed
		//if adding a comment without saving.
		clickOn("#logoutButton");
		checkPopupMsg("You have unsaved changes. Do you wish to save before exiting?");
		clickOn("Cancel");
		
		//Verifies list view of comments changes when the user changes sections
		doubleClickOn("Mission");
		clickOn("Goal");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.isEmpty());
		clickOn("#commentField");
		write("We need a goal");
		clickOn("#submitCommentButton");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: We need a goal"));
		
		//Verifies comment reappears when user navigates back to correct section
		clickOn("Mission");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: I have an idea"));
		clickOn("Goal");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: We need a goal"));
	}
	
	/**
	 * Tests that comments can be seen by users other than the one who left the comment
	 */
	private void testAlternativeUser()
	{
		clickOn("#logoutButton");
		clickOn("Yes");
		clickOn("#usernameField");
		for(int i = 0; i<4; i++)
		{
			press(KeyCode.BACK_SPACE);
			release(new KeyCode[] {});	
		}
		
		write("admin");
		
		clickOn("#passwordField");
		for(int i = 0; i<4; i++)
		{
			press(KeyCode.BACK_SPACE);
			release(new KeyCode[] {});	
		}
		write("admin");
		
		clickOn("#loginButton");
		clickOn("2019");
		//Verifies users can see comments left by other users
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: I have an idea"));
		doubleClickOn("Mission");
		clickOn("Goal");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: We need a goal"));
		
	}
	
	/**
	 * Tests that users can remove comments
	 */
	private void testRemoveComment()
	{
		//verifies comment is removed from listview
		clickOn("Mission");
		clickOn("user: I have an idea");
		clickOn("Remove Comment");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.isEmpty());
		
		//verifies the warning popup is diaplayed in response
		// to comment deletions, and that the comment is now absent when another user views the same section
		clickOn("#logoutButton");
		checkPopupMsg("You have unsaved changes. Do you wish to save before exiting?");
		clickOn("Yes");
		clickOn("#loginButton");
		clickOn("2019");
		FxAssert.verifyThat("#commentListView", ListViewMatchers.isEmpty());
		
		clickOn("#commentField");
		write("I have another idea");
		clickOn("#submitCommentButton");
		
		clickOn("#commentField");
		write("So many ideas");
		clickOn("#submitCommentButton");
		
		clickOn("user: So many ideas");
		clickOn("Remove Comment");
		doubleClickOn("Mission");
		clickOn("Goal");
		clickOn("Mission");
		
		//Verifies only the selected comment is removed correctly, by confirming the listview only has one
		//comment, the comment not intended for deletion.
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasListCell("user: I have another idea"));
		FxAssert.verifyThat("#commentListView", ListViewMatchers.hasItems(1));
	}
	
	

}
