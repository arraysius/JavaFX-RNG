import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class RNG extends Application {

	private static int min;
	private static int max;
	private static boolean repeat;
	private static boolean animSound;
	private static ArrayList<Integer> historyNums;
	private static ListView<HBox> historyList;
	private static Label genNumber;
	private static Button genButton;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		settingMenu();
	}

	private void popUp(String title, String message, String link) {
		final Stage popUpStage = new Stage();
		popUpStage.setTitle(title);
		popUpStage.getIcons().add(new Image("file:rng_icon.png"));
		popUpStage.initModality(Modality.APPLICATION_MODAL);
		double width = 350;
		double height = 200;

		Label text = new Label(message);
		text.setFont(new Font(20));
		Button okButton = new Button("OK");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				popUpStage.close();
			}
		});

		VBox layout = new VBox(20);
		layout.getChildren().add(text);
		if (link != null) {
			final Hyperlink github = new Hyperlink(link);
			github.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					getHostServices().showDocument(github.getText());
				}
			});
			layout.getChildren().add(github);
		}
		layout.getChildren().add(okButton);
		layout.setAlignment(Pos.CENTER);

		popUpStage.setScene(new Scene(layout, width, height));
		popUpStage.setWidth(width);
		popUpStage.setHeight(height);
		popUpStage.setResizable(false);
		popUpStage.show();
	}

	private void settingMenu() {
		final Stage settingsStage = new Stage();
		settingsStage.setTitle("Random Number Generator");
		settingsStage.getIcons().add(new Image("file:rng_icon.png"));

		/* Settings layout */

		final TextField minValue = new TextField("1");
		final TextField maxValue = new TextField("100");

		// Number inputs
		GridPane minMaxGrid = new GridPane();
		minMaxGrid.setAlignment(Pos.CENTER);
		minMaxGrid.setHgap(10);
		minMaxGrid.setVgap(8);

		Label minLabel = new Label("Minimum Value:");
		GridPane.setConstraints(minLabel, 0, 0);

		GridPane.setConstraints(minValue, 1, 0);
		minValue.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*"))
					minValue.setText(newValue.replaceAll("[^\\d]", ""));

				String minText = minValue.getText();
				if (minText.length() > 1 && minText.charAt(0) == '0')
					minValue.setText(minText.substring(1));

				if (!minText.isEmpty() && Integer.parseInt(minText) >= 100_000_000) {
					minValue.setText("99999999");
					maxValue.setText("100000000");
				}
			}
		});

		Label maxLabel = new Label("Maximum Value:");
		GridPane.setConstraints(maxLabel, 0, 1);

		GridPane.setConstraints(maxValue, 1, 1);
		maxValue.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					maxValue.setText(newValue.replaceAll("[^\\d]", ""));
				}

				String maxText = maxValue.getText();
				if (maxText.equals("0")) {
					minValue.setText("0");
					maxValue.setText("1");
				}

				if (maxText.length() > 1 && maxText.charAt(0) == '0') {
					maxValue.setText(maxText.substring(1));
				}

				if (!maxText.isEmpty() && Integer.parseInt(maxText) > 100_000_000)
					maxValue.setText("100000000");
			}
		});

		minMaxGrid.getChildren().addAll(minLabel, minValue, maxLabel, maxValue);

		// Checkboxes
		GridPane checkboxes = new GridPane();
		checkboxes.setAlignment(Pos.CENTER);
		checkboxes.setHgap(10);
		checkboxes.setVgap(8);

		final CheckBox repeatCheckbox = new CheckBox();
		GridPane.setConstraints(repeatCheckbox, 0, 2);

		Label repeatLabel = new Label("Repeat Values");
		repeatLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				repeatCheckbox.requestFocus();
				repeatCheckbox.setSelected(!repeatCheckbox.isSelected());
			}
		});
		GridPane.setConstraints(repeatLabel, 1, 2);

		final CheckBox animSoundCheckbox = new CheckBox();
		GridPane.setConstraints(animSoundCheckbox, 0, 3);

		Label animSoundLabel = new Label("Animation & Sound");
		animSoundLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				animSoundCheckbox.requestFocus();
				animSoundCheckbox.setSelected(!animSoundCheckbox.isSelected());
			}
		});
		animSoundCheckbox.setSelected(true);
		GridPane.setConstraints(animSoundLabel, 1, 3);

		checkboxes.getChildren().addAll(repeatCheckbox, repeatLabel, animSoundCheckbox, animSoundLabel);

		// Continue nextButton
		Button nextButton = new Button("Next");
		nextButton.setAlignment(Pos.CENTER);
		nextButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				// Validate inputs
				String minText = minValue.getText();
				String maxText = maxValue.getText();
				if (!minText.isEmpty() && !maxText.isEmpty() && Integer.parseInt(minText) < Integer.parseInt(maxText)) {
					min = Integer.parseInt(minText);
					max = Integer.parseInt(maxText);
					repeat = repeatCheckbox.isSelected();
					animSound = animSoundCheckbox.isSelected();
					settingsStage.close();
					generatorWindow();
				} else {
					popUp("Error", "Invalid input values!", null);
				}
			}
		});

		VBox settingsLayout = new VBox(10);
		settingsLayout.getChildren().addAll(minMaxGrid, checkboxes, nextButton);
		settingsLayout.setAlignment(Pos.CENTER);

		double minWidth = 500, minHeight = minWidth * 0.75;

		settingsStage.setScene(new Scene(settingsLayout, minWidth, minHeight));
		settingsStage.setMinWidth(minWidth);
		settingsStage.setMinHeight(minHeight);
		settingsStage.show();
	}

	private void generatorWindow() {
		final Stage genStage = new Stage();
		double minWidth = 800, minHeight = 600;
		genStage.setMinWidth(minWidth);
		genStage.setMinHeight(minHeight);
		genStage.setTitle("Random Number Generator");
		try {
			genStage.getIcons().add(new Image(new FileInputStream("rng_icon.png")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		historyList = new ListView<>();
		historyList.setFocusTraversable(false);
		historyNums = new ArrayList<>();

		genNumber = new Label("0");
		genNumber.setFont(new Font(250));
		genButton = new Button("Generate");
		genButton.setFont(new Font(35));
		genButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				if (animSound) {
					// Sound
					Media sound = new Media(new File("ticking.mp3").toURI().toString());
					MediaPlayer mediaPlayer = new MediaPlayer(sound);
					mediaPlayer.play();

					// Animation
					genButton.setDisable(true);
					int seconds = 2;
					int animGens = 20;
					Timeline animationTimeline = new Timeline(new KeyFrame(
							Duration.millis(1000 / animGens),
							new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent actionEvent) {
									genNumber.setText(String.valueOf(genRandomNumber(min, max)));
								}
							}
					));
					animationTimeline.setCycleCount(seconds * animGens);
					animationTimeline.setOnFinished(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent actionEvent) {
							// Set generated number
							setGenNumber();
						}
					});
					animationTimeline.play();
				} else {
					// Set generated number
					setGenNumber();
				}
			}
		});

		VBox genDisplay = new VBox(15);
		genDisplay.setAlignment(Pos.CENTER);
		genDisplay.getChildren().addAll(genNumber, genButton);

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(10));
		borderPane.setCenter(genDisplay);

		if (!repeat) {
			borderPane.setLeft(historyList);
		}

		Label restartLabel = new Label("Restart");
		restartLabel.setTextFill(Color.web("black"));
		restartLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				genStage.close();
				settingMenu();
			}
		});
		Label aboutLabel = new Label("About");
		aboutLabel.setTextFill(Color.web("black"));
		aboutLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				popUp("About", "Created by Aloysius Lee", "https://github.com/cyanoise/JavaFX-RNG");
			}
		});
		Menu restartMenuButton = new Menu();
		restartMenuButton.setGraphic(restartLabel);
		Menu aboutMenuButton = new Menu();
		aboutMenuButton.setGraphic(aboutLabel);
		MenuBar menuBar = new MenuBar();
		menuBar.setStyle("-fx-background-color: linear-gradient(lightgray 0%, darkgray 100%)");
		menuBar.getMenus().addAll(restartMenuButton, aboutMenuButton);

		BorderPane genScreen = new BorderPane();
		genScreen.setTop(menuBar);
		genScreen.setCenter(borderPane);

		genStage.setScene(new Scene(genScreen, minWidth, minHeight));
		genStage.show();
	}

	private static ObservableList<HBox> newHistItems(ArrayList<Integer> historyNums) {
		ObservableList<HBox> historyItems = FXCollections.observableArrayList();
		for (int i : historyNums) {
			Label label = new Label(String.valueOf(i));
			label.setFont(new Font(35));
			HBox hBox = new HBox(5);
			hBox.setAlignment(Pos.CENTER);
			hBox.getChildren().add(label);
			historyItems.add(hBox);
		}
		return historyItems;
	}

	private static int genRandomNumber(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	private void setGenNumber() {
		int num = genRandomNumber(min, max);
		if (!repeat) {
			while (historyNums.contains(num)) {
				num = genRandomNumber(min, max);
			}
			historyNums.add(num);
			historyList.setItems(newHistItems(historyNums));
			genNumber.setText(String.valueOf(num));
			if (historyNums.size() == max - min + 1) {
				genButton.setDisable(true);
				popUp("Complete", "No more numbers to generate", null);
			} else {
				genButton.setDisable(false);
			}
		} else {
			genNumber.setText(String.valueOf(num));
			genButton.setDisable(false);
		}
	}
}
