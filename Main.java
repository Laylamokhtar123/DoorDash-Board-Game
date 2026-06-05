package game.GUI;

import java.util.Random;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.Dasher;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage stage;
	private Role selectedRole;
	private Game game;

	private GridPane boardGrid;
	private CheckBox powerupCheck;
	private Label actionLabel;
	private Label turnLabel;
	private Label diceLabel;
	private Label cardLabel;
	private Label deckLabel;
	private Label freezeLabel;
	private Label shieldLabel;
	private Label playerInfoLabel;
	private Label opponentInfoLabel;
	private Label playerDeltaLabel;
	private Label opponentDeltaLabel;
	private int lastDiceRoll;
	private Card lastDrawnCard;

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		stage.setTitle("DoorDasH: Scare vs Laugh Touchdown");
		showStartScreen();
		stage.show();
	}

	public void showStartScreen() {
		Label title = new Label("DoorDasH: Scare vs Laugh Touchdown");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

		Label subtitle = new Label("Choose your side, then race to cell 99 with at least 1000 energy.");
		subtitle.setStyle("-fx-font-size: 15px;");

		Label instructions = new Label(
			"GAME INSTRUCTIONS\n\n"
			+ "1. Choose SCARER or LAUGHER.\n"
			+ "2. Your monster and the opponent are selected by role.\n"
			+ "3. Both active monsters start at cell 0.\n"
			+ "4. The board has 100 zigzag cells.\n"
			+ "5. Doors, monster cells, card cells, conveyor belts, and contamination socks trigger effects.\n"
			+ "6. You may activate your monster powerup before rolling if you have enough energy.\n"
			+ "7. Roll the dice to move the current monster.\n"
			+ "8. Reach cell 99 with at least 1000 energy to win."
		);
		instructions.setWrapText(true);
		instructions.setMaxWidth(680);
		instructions.setStyle("-fx-font-size: 14px; -fx-line-spacing: 2px;");

		ToggleGroup roleGroup = new ToggleGroup();
		RadioButton scarer = new RadioButton("SCARER");
		RadioButton laugher = new RadioButton("LAUGHER");
		scarer.setToggleGroup(roleGroup);
		laugher.setToggleGroup(roleGroup);
		scarer.setOnAction(e -> selectedRole = Role.SCARER);
		laugher.setOnAction(e -> selectedRole = Role.LAUGHER);

		HBox roleBox = new HBox(20, scarer, laugher);
		roleBox.setAlignment(Pos.CENTER);

		Button startButton = new Button("Start Game");
		startButton.setDefaultButton(true);
		startButton.setOnAction(e -> startGame());

		VBox root = new VBox(18, title, subtitle, instructions, roleBox, startButton);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(28));
		root.setStyle("-fx-background-color: #f6f7fb;");

		stage.setScene(new Scene(root, 1000, 720));
	}

	public void startGame() {
		if (selectedRole == null) {
			showError("Choose a side first", "Select SCARER or LAUGHER before starting the game.");
			return;
		}

		try {
			game = new Game(selectedRole);
			showGameScreen();
			refreshGameView("Game started. " + game.getCurrent().getName() + " plays first.", null);
		}
		catch (Exception ex) {
			showError("Could not start game", ex.getMessage());
		}
	}

	public void showGameScreen() {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(14));
		root.setStyle("-fx-background-color: #eaf0f6;");

		boardGrid = new GridPane();
		boardGrid.setHgap(2);
		boardGrid.setVgap(2);
		boardGrid.setAlignment(Pos.CENTER);

		Label title = new Label("DoorDasH: Scare vs Laugh Touchdown");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		title.setStyle("-fx-text-fill: #172033;");

		HBox legend = new HBox(10,
			legendItem("SCARER Door", "#9bd4ff"),
			legendItem("LAUGHER Door", "#ffacc9"),
			legendItem("Card", "#f6c95f"),
			legendItem("Monster", "#8de0d1"),
			legendItem("Transport", "#a8e68d"),
			legendItem("Sock", "#ffb284"),
			legendItem("Used Door", "#a7b0ba")
		);
		legend.setAlignment(Pos.CENTER);

		VBox topBar = new VBox(6, title, legend);
		topBar.setAlignment(Pos.CENTER);
		topBar.setPadding(new Insets(0, 0, 12, 0));
		root.setTop(topBar);

		VBox statusPanel = createStatusPanel();

		Label endLabel = new Label("End");
		endLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		Label startLabel = new Label("Start");
		startLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		VBox sideLabels = new VBox(505, endLabel, startLabel);
		sideLabels.setAlignment(Pos.CENTER);

		HBox boardWithLabels = new HBox(10, sideLabels, boardGrid);
		boardWithLabels.setAlignment(Pos.CENTER);

		StackPane boardHolder = new StackPane(boardWithLabels);
		boardHolder.setAlignment(Pos.CENTER);
		boardHolder.setPadding(new Insets(18));
		boardHolder.setStyle("-fx-background-color: #fff7d6; -fx-border-color: #5b4b22; -fx-border-width: 2; -fx-border-radius: 2; -fx-background-radius: 2;");

		root.setCenter(boardHolder);
		root.setRight(statusPanel);

		Scene scene = new Scene(root, 1280, 800);
		enableCheatKeys(scene);
		stage.setScene(scene);
	}

	public HBox legendItem(String text, String color) {
		Rectangle sample = new Rectangle(14, 14);
		sample.setFill(Color.web(color));
		sample.setStroke(Color.web("#2f3b46"));

		Label label = new Label(text);
		label.setStyle("-fx-font-size: 11px; -fx-text-fill: #172033;");

		HBox item = new HBox(4, sample, label);
		item.setAlignment(Pos.CENTER);
		return item;
	}

	public VBox createStatusPanel() {
		turnLabel = createValueLabel();
		diceLabel = createValueLabel();
		cardLabel = createValueLabel();
		deckLabel = createValueLabel();
		freezeLabel = createValueLabel();
		shieldLabel = createValueLabel();
		actionLabel = createValueLabel();
		playerInfoLabel = createValueLabel();
		opponentInfoLabel = createValueLabel();
		playerDeltaLabel = createValueLabel();
		opponentDeltaLabel = createValueLabel();

		powerupCheck = new CheckBox("Activate powerup before rolling");
		powerupCheck.setWrapText(true);
		powerupCheck.setStyle("-fx-font-size: 12px;");

		Button rollButton = new Button("Roll Dice");
		rollButton.setMaxWidth(Double.MAX_VALUE);
		rollButton.setStyle("-fx-font-weight: bold; -fx-background-color: #2563eb; -fx-text-fill: white;");
		rollButton.setOnAction(e -> playTurn());

		Button restartButton = new Button("Return to Start");
		restartButton.setMaxWidth(Double.MAX_VALUE);
		restartButton.setOnAction(e -> {
			game = null;
			selectedRole = null;
			showStartScreen();
		});

		VBox panel = new VBox(6,
			section("Turn", turnLabel),
			powerupCheck,
			rollButton,
			section("Last Action", actionLabel),
			section("Dice", diceLabel),
			section("Card Deck", deckLabel),
			section("Card", cardLabel),
			section("Freeze", freezeLabel),
			section("Shield", shieldLabel),
			section("Player", playerInfoLabel, playerDeltaLabel),
			section("Opponent", opponentInfoLabel, opponentDeltaLabel),
			restartButton
		);
		panel.setPadding(new Insets(0, 0, 0, 14));
		panel.setPrefWidth(330);
		return panel;
	}

	public void enableCheatKeys(Scene scene) {
		scene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.W) {
				demonstrateWinningScenario();
			}
			else if (e.getCode() == KeyCode.E) {
				increaseCurrentMonsterEnergy();
			}
		});
	}

	public void demonstrateWinningScenario() {
		Monster player = game.getPlayer();
		Snapshot beforePlayer = new Snapshot(game.getPlayer());
		Snapshot beforeOpponent = new Snapshot(game.getOpponent());

		player.setPosition(Constants.WINNING_POSITION);
		if (player.getEnergy() < Constants.WINNING_ENERGY) {
			player.setEnergy(Constants.WINNING_ENERGY);
		}

		refreshGameView("Win demo: " + player.getName() + " was moved to cell 99 with winning energy.",
			new Snapshot[] { beforePlayer, beforeOpponent });
		showWinScreen(player);
	}

	public void increaseCurrentMonsterEnergy() {
		Monster current = game.getCurrent();
		Snapshot beforePlayer = new Snapshot(game.getPlayer());
		Snapshot beforeOpponent = new Snapshot(game.getOpponent());

		current.setEnergy(current.getEnergy() + 500);

		refreshGameView("Energy demo: " + current.getName() + " gained 500 energy.",
			new Snapshot[] { beforePlayer, beforeOpponent });

		Monster winner = game.getWinner();
		if (winner != null) {
			showWinScreen(winner);
		}
	}

	public VBox section(String title, Label... labels) {
		Label header = new Label(title);
		header.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		header.setStyle("-fx-text-fill: #172033;");

		VBox box = new VBox(2);
		box.getChildren().add(header);
		for (Label label : labels) {
			label.setWrapText(true);
			box.getChildren().add(label);
		}
		box.setPadding(new Insets(6));
		box.setStyle("-fx-background-color: white; -fx-border-color: #c8d2df; -fx-border-radius: 7; -fx-background-radius: 7;");
		return box;
	}

	public Label createValueLabel() {
		Label label = new Label();
		label.setWrapText(true);
		label.setStyle("-fx-font-size: 11px; -fx-text-fill: #111827;");
		return label;
	}

	public void playTurn() {
		Monster moving = game.getCurrent();
		Monster opponent = getCurrentOpponent();
		Snapshot beforePlayer = new Snapshot(game.getPlayer());
		Snapshot beforeOpponent = new Snapshot(game.getOpponent());
		boolean requestedPowerup = powerupCheck.isSelected();

		try {
			StringBuilder action = new StringBuilder();
			lastDiceRoll = 0;
			lastDrawnCard = null;

			if (requestedPowerup) {
				game.usePowerup();
				action.append(moving.getName()).append(" activated a powerup. ");
			}

			if (moving.isFrozen()) {
				moving.setFrozen(false);
				game.setCurrent(opponent);
				action.append(moving.getName()).append(" was frozen and skipped the turn.");
			}
			else {
				lastDiceRoll = new Random().nextInt(6) + 1;
				Card nextCard = Board.getCards().isEmpty() ? null : Board.getCards().get(0);
				int cardsBefore = Board.getCards().size();

				game.getBoard().moveMonster(moving, lastDiceRoll, opponent);
				if (nextCard != null && Board.getCards().size() == cardsBefore - 1) {
					lastDrawnCard = nextCard;
				}

				game.setCurrent(opponent);
				action.append(moving.getName()).append(" rolled ").append(lastDiceRoll).append(" and moved.");
			}

			refreshGameView(action.toString(), new Snapshot[] { beforePlayer, beforeOpponent });

			Monster winner = game.getWinner();
			if (winner != null) {
				showWinScreen(winner);
			}
		}
		catch (Exception ex) {
			refreshGameView("Invalid action: " + ex.getMessage(), new Snapshot[] { beforePlayer, beforeOpponent });
			showError("Invalid action", ex.getMessage());
		}
		finally {
			powerupCheck.setSelected(false);
		}
	}

	public void refreshGameView(String actionText, Snapshot[] before) {
		renderBoard();

		Monster player = game.getPlayer();
		Monster opponent = game.getOpponent();
		turnLabel.setText("Current turn: " + game.getCurrent().getName() + " (" + game.getCurrent().getRole() + ")");
		actionLabel.setText(actionText == null ? "Ready." : actionText);
		diceLabel.setText(lastDiceRoll == 0 ? "No dice roll this turn." : "Dice roll: " + lastDiceRoll);
		deckLabel.setText("25 shuffled cards for card cells\nCards currently left: " + Board.getCards().size());
		cardLabel.setText(formatCard(lastDrawnCard));
		freezeLabel.setText(formatFreeze(player, opponent));
		shieldLabel.setText(formatShield(player, opponent, before));
		playerInfoLabel.setText(formatMonster(player));
		opponentInfoLabel.setText(formatMonster(opponent));

		if (before == null) {
			playerDeltaLabel.setText("Energy change: 0");
			opponentDeltaLabel.setText("Energy change: 0");
		}
		else {
			playerDeltaLabel.setText(formatDelta("Energy change", before[0].energy, player.getEnergy()));
			opponentDeltaLabel.setText(formatDelta("Energy change", before[1].energy, opponent.getEnergy()));
		}
	}

	public void renderBoard() {
		boardGrid.getChildren().clear();
		Cell[][] cells = game.getBoard().getBoardCells();

		for (int displayRow = 0; displayRow < Constants.BOARD_ROWS; displayRow++) {
			int boardRow = Constants.BOARD_ROWS - 1 - displayRow;

			for (int col = 0; col < Constants.BOARD_COLS; col++) {
				int index = boardRow % 2 == 0
					? boardRow * Constants.BOARD_COLS + col
					: boardRow * Constants.BOARD_COLS + (Constants.BOARD_COLS - 1 - col);

				Cell cell = cells[boardRow][col];
				boardGrid.add(createCellPane(index, cell), col, displayRow);
			}
		}
	}

	public StackPane createCellPane(int index, Cell cell) {
		Rectangle background = new Rectangle(74, 54);
		background.setArcWidth(0);
		background.setArcHeight(0);
		background.setFill(cellColor(cell, index));
		background.setStroke(Color.web("#111111"));
		background.setStrokeWidth(1.5);

		Label text = new Label(cellText(index, cell));
		text.setAlignment(Pos.CENTER);
		text.setWrapText(true);
		text.setMaxWidth(70);
		text.setStyle("-fx-font-size: 8px; -fx-text-fill: #111827; -fx-font-weight: bold;");

		StackPane pane = new StackPane(background, text);
		pane.setPrefSize(74, 54);

		ImageView image = imageForCell(cell, index);
		if (image != null) {
			image.setOpacity(cell instanceof DoorCell ? 0.78 : 0.55);
			pane.getChildren().add(1, image);
		}

		addMonsterTokens(pane, index);
		return pane;
	}

	public Color cellColor(Cell cell, int index) {
		if (index == Constants.WINNING_POSITION)
			return Color.web("#fff0b8");

		if (cell instanceof DoorCell) {
			DoorCell door = (DoorCell) cell;
			if (door.isActivated())
				return Color.web("#b9b9b9");
			return door.getRole() == Role.SCARER ? Color.web("#cbefff") : Color.web("#ffc4d4");
		}

		if (cell instanceof CardCell)
			return Color.web("#ffe59a");
		if (cell instanceof MonsterCell)
			return Color.web("#c7efe8");
		if (cell instanceof ConveyorBelt)
			return Color.web("#c5efad");
		if (cell instanceof ContaminationSock)
			return Color.web("#ffd0a8");

		return Color.web("#fff7d6");
	}

	public String cellText(int index, Cell cell) {
		if (index == Constants.WINNING_POSITION)
			return index + "\nFINISH";

		if (cell instanceof DoorCell) {
			DoorCell door = (DoorCell) cell;
			return index + "\n" + door.getRole() + " Door\nE:" + door.getEnergy()
				+ (door.isActivated() ? "\nUSED" : "");
		}

		if (cell instanceof CardCell)
			return index + "\nCard";
		if (cell instanceof MonsterCell) {
			Monster stationed = ((MonsterCell) cell).getCellMonster();
			return index + "\nMonster\n" + stationed.getName();
		}
		if (cell instanceof ConveyorBelt)
			return index + "\nConveyor\n" + ((ConveyorBelt) cell).getEffect();
		if (cell instanceof ContaminationSock)
			return index + "\nSock\n" + ((ContaminationSock) cell).getEffect();

		return index + "\nNormal";
	}

	public ImageView imageForCell(Cell cell, int index) {
		String file = null;

		if (index == Constants.WINNING_POSITION)
			file = "enddoor.png";
		else if (cell instanceof DoorCell) {
			DoorCell door = (DoorCell) cell;
			if (door.isActivated())
				file = "door_darkgreen.png";
			else if (door.getRole() == Role.SCARER)
				file = "door_lightblue.png";
			else
				file = "door_pink.png";
		}
		else if (cell instanceof CardCell)
			file = "card.png";
		else if (cell instanceof MonsterCell)
			file = "monster.png";
		else if (cell instanceof ConveyorBelt)
			file = "conveyorbelt.png";
		else if (cell instanceof ContaminationSock)
			file = "sock.png";
		else if (!(cell instanceof DoorCell))
			file = "normal.png";

		if (file == null || getClass().getResource("/Images/" + file) == null)
			return null;

		ImageView imageView = new ImageView(new Image(getClass().getResource("/Images/" + file).toExternalForm()));
		imageView.setFitWidth(60);
		imageView.setFitHeight(42);
		imageView.setPreserveRatio(true);
		return imageView;
	}

	public void addMonsterTokens(StackPane pane, int index) {
		if (game.getPlayer().getPosition() == index) {
			pane.getChildren().add(token("P", "#2563eb", -18));
		}

		if (game.getOpponent().getPosition() == index) {
			pane.getChildren().add(token("O", "#dc2626", 18));
		}
	}

	public Label token(String value, String color, double translateX) {
		Label token = new Label(value);
		token.setMinSize(20, 20);
		token.setAlignment(Pos.CENTER);
		token.setTranslateX(translateX);
		token.setTranslateY(15);
		token.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
		return token;
	}

	public String formatCard(Card card) {
		if (card == null)
			return "No card drawn.";

		return "Card drawn: " + card.getName()
			+ "\nEffect: " + card.getDescription()
			+ "\nType: " + (card.isLucky() ? "Lucky/helpful" : "Trap/risky");
	}

	public String formatFreeze(Monster player, Monster opponent) {
		if (player.isFrozen() && opponent.isFrozen())
			return player.getName() + " and " + opponent.getName() + " are frozen.";
		if (player.isFrozen())
			return player.getName() + " is frozen and will skip a turn.";
		if (opponent.isFrozen())
			return opponent.getName() + " is frozen and will skip a turn.";
		return "No frozen monsters.";
	}

	public String formatShield(Monster player, Monster opponent, Snapshot[] before) {
		String status = "Player shield: " + statusWord(player.isShielded())
			+ "\nOpponent shield: " + statusWord(opponent.isShielded());

		if (before == null)
			return status;

		boolean playerShieldBlocked = before[0].shielded && !player.isShielded() && player.getEnergy() >= before[0].energy;
		boolean opponentShieldBlocked = before[1].shielded && !opponent.isShielded() && opponent.getEnergy() >= before[1].energy;

		if (playerShieldBlocked)
			status += "\nShield block: " + player.getName() + " blocked an energy loss.";
		if (opponentShieldBlocked)
			status += "\nShield block: " + opponent.getName() + " blocked an energy loss.";

		return status;
	}

	public String formatMonster(Monster monster) {
		return "Name: " + monster.getName()
			+ "\nOriginal role: " + monster.getOriginalRole()
			+ "\nCurrent role: " + monster.getRole()
			+ "\nType: " + monster.getClass().getSimpleName()
			+ "\nEnergy: " + monster.getEnergy()
			+ "\nPosition: " + monster.getPosition()
			+ "\nActive status: " + activeStatus(monster);
	}

	public String activeStatus(Monster monster) {
		String status = "Confusion turns: " + monster.getConfusionTurns();

		if (monster instanceof Dasher)
			status += ", Momentum Rush: " + ((Dasher) monster).getMomentumTurns();
		if (monster instanceof MultiTasker)
			status += ", Focus Mode: " + ((MultiTasker) monster).getNormalSpeedTurns();
		if (monster.isFrozen())
			status += ", Frozen";
		if (monster.isShielded())
			status += ", Shielded";
		if (monster.getRole() != monster.getOriginalRole())
			status += ", Role confused";

		return status;
	}

	public String formatDelta(String label, int before, int after) {
		int delta = after - before;
		return label + ": " + (delta > 0 ? "+" : "") + delta;
	}

	public String statusWord(boolean active) {
		return active ? "active" : "inactive";
	}

	public Monster getCurrentOpponent() {
		return game.getCurrent() == game.getPlayer() ? game.getOpponent() : game.getPlayer();
	}

	public void showWinScreen(Monster winner) {
		Label title = new Label("Game Won");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 32));

		Label winnerLabel = new Label("Winner: " + winner.getName() + " (" + winner.getRole() + ")");
		winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		Label finalEnergy = new Label(
			game.getPlayer().getName() + " final energy: " + game.getPlayer().getEnergy()
			+ "\n" + game.getOpponent().getName() + " final energy: " + game.getOpponent().getEnergy()
		);
		finalEnergy.setStyle("-fx-font-size: 15px;");

		Button startButton = new Button("Return to Start Window");
		startButton.setOnAction(e -> {
			game = null;
			selectedRole = null;
			showStartScreen();
		});

		VBox root = new VBox(18, title, winnerLabel, finalEnergy, startButton);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(24));
		root.setStyle("-fx-background-color: #f6f7fb;");
		VBox.setVgrow(finalEnergy, Priority.NEVER);

		stage.setScene(new Scene(root, 900, 620));
	}

	public void showError(String title, String message) {
		Stage popup = new Stage();
		popup.setTitle(title);
		popup.initOwner(stage);
		popup.initModality(Modality.APPLICATION_MODAL);

		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		Label messageLabel = new Label(message == null || message.trim().isEmpty()
			? "The action could not be performed."
			: message);
		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(340);

		Button closeButton = new Button("OK");
		closeButton.setOnAction(e -> popup.close());

		VBox root = new VBox(14, titleLabel, messageLabel, closeButton);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(20));
		root.setStyle("-fx-background-color: white;");

		popup.setScene(new Scene(root, 400, 180));
		popup.showAndWait();
	}

	public static class Snapshot {
		private final int energy;
		private final boolean shielded;

		public Snapshot(Monster monster) {
			energy = monster.getEnergy();
			shielded = monster.isShielded();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
