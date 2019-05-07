/**
 * 
 */
package software_masters.gui_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobotException;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;

import javafx.scene.input.KeyCode;

/**
 * Tests the feature of admins marking plans as editable or read-only
 * 
 * @author lee.kendall
 *
 */
class MarkEditablePlansTest extends GuiTestBase {

	/**
	 * method that coordinates order of gui test.
	 */
	@Test
	void test() 
	{
		adminTest();
		userTest();
		cleanUp();
	}
	
	/**
	 * Verifies admins can set plan permissions.
	 */
	private void adminTest()
	{
		clickOn("Connect");
		loginAsAdmin();
		
		//Demonstrates the 2020 plan is initially set to read-only, and the 2019 plan initially set
		//to editable.
		clickOn("2020 Read Only");
		verifyThat("#readOnlyLabel", LabeledMatchers.hasText("Local View Only - Cannot Save Changes"));
		clickOn("Back to plans");
		clickOn("2019");
		verify("#deleteSectionButton","Delete Section");
		clickOn("Back to plans");
		
		clickOn("Mark Editable Plans"); //will result in error if button is absent
		checkButtons();
		
		//Swaps permissions of both plans, then checks that the listview updated correctly.
		clickOn("2019");
		clickOn("Mark as Read-only");
		clickOn("2020 Read Only");
		clickOn("Mark as Editable");
		//Will result in errors if listviews not updated correctly
		clickOn("2019 Read Only");
		clickOn("2020");
		
		clickOn("Back to plans");
		//will result in error if back to plans button did not work, or if plan selection listview not
		//correctly updated
		clickOn("2019 Read Only");
		//Verifies that opening the 2019 plan now displays a read-only view, by checking for presence
		//of the text label only found in the read-only view.
		verifyThat("#readOnlyLabel", LabeledMatchers.hasText("Local View Only - Cannot Save Changes"));
		clickOn("Back to plans");
		
		clickOn("2020");//will result in error if plan selection listview not updated correctly
		//Verifies that opening the 2020 plan now displays an editabl view, by checking for presence
		//of the delete button only found in the plan edit view.
		verify("#deleteSectionButton","Delete Section");
		
		clickOn("Back to plans");
		clickOn("Mark Editable Plans");
		clickOn("Logout");
		
	}
	
	/**
	 * Verifies that users can't access the mark editable plans feature, and that the plans accessed
	 * by the user now have the permissions set by the admin previously.
	 */
	private void userTest()
	{
		//will result in error if the admin logging out from the markeditable plans view at 
		//the end of the admin test failed
		clickOn("#loginButton");
		
		//Verifies exception thrown when the test tries to click on the mark editable plans button.
		//This button should not exist for the regular user's plan selection view.
		Assertions.assertThrows(FxRobotException.class, () -> {
	        clickOn("Mark Editable Plans");});
		
		//Verifies permissions correctly set for regular users
		clickOn("2019 Read Only");
		//Verifies that opening the 2019 plan now displays a read-only view, by checking for presence
		//of the text label only found in the read-only view.
		verifyThat("#readOnlyLabel", LabeledMatchers.hasText("Local View Only - Cannot Save Changes"));
		clickOn("Back to plans");
		
		clickOn("2020");//will result in error if plan selection listview not updated correctly
		//Verifies that opening the 2020 plan now displays an editable view, by checking for presence
		//of the delete button only found in the plan edit view.
		verify("#deleteSectionButton","Delete Section");
		
	}	
	
	/**
	 * Helper method to login as an admin
	 */
	private void loginAsAdmin()
	{
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
	}
	
	/**
	 * Helper method which verifies buttons and label are populated with the intended text
	 */
	private void checkButtons()
	{
		verify("#markEditableButton","Mark as Editable");
		verify("#markReadOnlyButton","Mark as Read-only");
		verify("#backToPlansButton","Back to plans");
		verify("#logoutButton","Logout");
		verifyThat("#instructionsLabel", 
		LabeledMatchers.hasText("Select a plan, then mark it as editable or read-only"));
	}
	
	/**
	 *Resets plan permissions so the original PlanEditTest works correctly 
	 */
	private void cleanUp()
	{
		clickOn("Log Out");
		loginAsAdmin();
		clickOn("Mark Editable Plans");
		clickOn("2019 Read Only");
		clickOn("Mark as Editable");
		
		
	}

}
