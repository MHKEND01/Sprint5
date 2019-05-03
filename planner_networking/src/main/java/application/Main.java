package application;

import serverConnectionView.*;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import loginView.LoginViewController;
import markEditablePlansView.MarkEditablePlansController;
import planEditView.PlanEditViewController;
import planReadOnlyView.PlanReadOnlyViewController;
import planSelectionView.PlanSelectionViewController;
import software_masters.model.PlannerModel;

/**
 * @author lee.kendall
 */
public class Main extends Application
{

	/**
	 * Initializes the server connection window and includes methods for changing
	 * the window to display a new view
	 * 
	 * @param args
	 */

	PlannerModel model;
	Stage primaryStage;
	Parent mainView;

	public static void main(String[] args)
	{
		launch(args);
	}

	/*
	 * (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage) Initializes
	 * server connection view
	 */
	@Override
	public void start(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
		this.model = new PlannerModel();

		this.showConnectToServer();
	}

	/**
	 * Shows the connect to server window
	 */
	public void showConnectToServer()
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../serverConnectionView/serverConnectionView.fxml"));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		ServerConnectionViewController cont = loader.getController();
		cont.setApplication(this); // Allows controller to access showPlanSelectionView

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			primaryStage.close();

		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);

		primaryStage.show();
		primaryStage.sizeToScene();
	}

	/**
	 * Shows the login view window
	 */
	public void showLoginView()
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../loginView/loginView.fxml"));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		LoginViewController cont = loader.getController();
		cont.setApplication(this); // Allows controller to access showPlanSelectionView

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			primaryStage.close();

		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);
		primaryStage.show();
		primaryStage.sizeToScene();
	}

	/**
	 * Shows the plan selection view. Which version is loaded depends on the implementation of the AdminState
	 * is held by the client.
	 */
	public void showPlanSelectionView()
	{
		String viewPath = model.getState().getViewPath();
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource(viewPath));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		PlanSelectionViewController cont = loader.getController();
		cont.setApplication(this); // Allows controller to access showPlanEditView and showPlanReadOnlyView

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			primaryStage.close();

		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	/**
	 * Shows the plan edit view
	 */
	public void showPlanEditView()
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../planEditView/planEditView.fxml"));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		PlanEditViewController cont = loader.getController();
		cont.setApplication(this); // Allows controller to access showPlanSelectionView and showLoginView

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			e.consume();
			cont.changeSection();
			if (!cont.isPushed())
			{
				closeWindow(cont);
			}
			else
			{
				primaryStage.close();
			}
		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);
		primaryStage.show();
		primaryStage.sizeToScene();
	}

	/**
	 * Shows the plan read-only view
	 */
	public void showPlanReadOnlyView()
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../planReadOnlyView/planReadOnlyView.fxml"));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		PlanReadOnlyViewController cont = loader.getController();
		cont.setApplication(this); // Allows controller to access showPlanSelectionView and showLoginView

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			primaryStage.close();

		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);
		primaryStage.show();
		primaryStage.sizeToScene();
	}
	
	/**
	 * Shows the mark editable plans view for admins.
	 */
	public void showMarkEditablePlansView()
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../markEditablePlansView/markEditablePlansView.fxml"));

		try
		{
			mainView = loader.load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		MarkEditablePlansController cont = loader.getController();
		cont.setApplication(this);

		primaryStage.setOnCloseRequest((WindowEvent e) ->
		{
			primaryStage.close();

		});

		Scene s = new Scene(mainView);
		primaryStage.setScene(s);
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	/**
	 * @return the model
	 */
	public PlannerModel getModel()
	{
		return model;
	}

	/**
	 * @param model
	 *                  the model to set
	 */
	public void setModel(PlannerModel model)
	{
		this.model = model;
	}

	/**
	 * This method helps to pop up error message happens when controller is
	 * operating the model For example, delete a node that is not allowed to be
	 * deleted
	 * 
	 * @param message
	 *                    error message from wrong operation on the model
	 */
	public void sendError(String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Warning Dialog");
		alert.setHeaderText(message);
		alert.setContentText(null);
		// alert.getButtonTypes().get(0).

		alert.showAndWait();
	}

	/**
	 * Handles the exit without saving popup
	 * 
	 * @param cont
	 *                 plan edit view controller
	 */
	private void closeWindow(PlanEditViewController cont)
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
			if (cont.push())
			{
				primaryStage.close();
			}
		}
		else
			if (result.get() == noButton)
			{
				primaryStage.close();

			}

	}

}
