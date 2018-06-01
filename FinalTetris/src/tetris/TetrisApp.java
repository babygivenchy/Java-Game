package tetris;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;



public class TetrisApp extends Application {

    //MediaPlayer music;

    public static final int TILE_SIZE = 30;

    public static final int GRID_WIDTH = 14;

    public static final int GRID_HEIGHT = 20;



    private double time;

    private GraphicsContext g;



    private int[][] grid = new int[GRID_WIDTH][GRID_HEIGHT];



    private List<Tetromino> original = new ArrayList<>();

    private List<Tetromino> tetrominos = new ArrayList<>();



    private Tetromino selected;


    private Parent createContent() {

        Pane root = new Pane();

        root.setPrefSize(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        root.setStyle("-fx-background-color: black");


        Canvas canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        g = canvas.getGraphicsContext2D();



        root.getChildren().addAll(canvas);



        original.add(new Tetromino(Color.BLUE,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1, Direction.RIGHT),
                new Piece(2, Direction.RIGHT)
        ));
        original.add(new Tetromino(Color.RED,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1, Direction.RIGHT),
                new Piece(1, Direction.DOWN)
        ));

        original.add(new Tetromino(Color.GREEN,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.RIGHT),
                new Piece(2, Direction.RIGHT),
                new Piece(1, Direction.DOWN)));

        original.add(new Tetromino(Color.YELLOW,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(2, Direction.LEFT),
                new Piece(1, Direction.DOWN)));

        original.add(new Tetromino(Color.PURPLE,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.RIGHT),
                new Piece(1, Direction.RIGHT, Direction.DOWN),
                new Piece(1, Direction.DOWN)));

        original.add(new Tetromino(Color.CYAN,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.RIGHT),
                new Piece(1, Direction.DOWN),
                new Piece(1, Direction.DOWN, Direction.LEFT)));

        original.add(new Tetromino(Color.ORANGE,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1, Direction.DOWN),
                new Piece(1, Direction.DOWN, Direction.RIGHT)));







        spawn();



        AnimationTimer timer = new AnimationTimer() {

            @Override

            public void handle(long now) {

                //time += 0.017;
                time += 0.008;



                if (time >= 0.5) {

                    update();

                    render();

                    time = 0;

                }

            }

        };

        timer.start();



        return root;

    }



    private void update() {

        makeMove(p -> p.move(Direction.DOWN), p -> p.move(Direction.UP), true);

    }



    private void render() {

        g.clearRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);



        tetrominos.forEach(p -> p.draw(g));

    }



    private void placePiece(Piece piece) {

        grid[piece.x][piece.y]++;

    }



    private void removePiece(Piece piece) {

        grid[piece.x][piece.y]--;

    }



    private boolean isOffscreen(Piece piece) {

        return piece.x < 0 || piece.x >= GRID_WIDTH

                || piece.y < 0 || piece.y >= GRID_HEIGHT;

    }



    private void makeMove(Consumer<Tetromino> onSuccess, Consumer<Tetromino> onFail, boolean endMove) {

        selected.pieces.forEach(this::removePiece);



        onSuccess.accept(selected);



        boolean offscreen = selected.pieces.stream().anyMatch(this::isOffscreen);



        if (!offscreen) {

            selected.pieces.forEach(this::placePiece);

        } else {

            onFail.accept(selected);



            selected.pieces.forEach(this::placePiece);



            if (endMove) {

                sweep();

            }



            return;

        }



        if (!isValidState()) {

            selected.pieces.forEach(this::removePiece);



            onFail.accept(selected);



            selected.pieces.forEach(this::placePiece);



            if (endMove) {

                sweep();

            }

        }

    }



    private boolean isValidState() {

        for (int y = 0; y < GRID_HEIGHT; y++) {

            for (int x = 0; x < GRID_WIDTH; x++) {

                if (grid[x][y] > 1) {

                    return false;

                }

            }

        }



        return true;

    }



    private void sweep() {

        List<Integer> rows = sweepRows();

        rows.forEach(row -> {

            for (int x = 0; x < GRID_WIDTH; x++) {

                for (Tetromino tetromino : tetrominos) {

                    tetromino.detach(x, row);

                }



                grid[x][row]--;

            }

        });



        rows.forEach(row -> {

            tetrominos.stream().forEach(tetromino -> {

                tetromino.pieces.stream()

                        .filter(piece -> piece.y < row)

                        .forEach(piece -> {

                            removePiece(piece);

                            piece.y++;

                            placePiece(piece);

                        });

            });

        });



        spawn();

    }



    private List<Integer> sweepRows() {

        List<Integer> rows = new ArrayList<>();



        outer:

        for (int y = 0; y < GRID_HEIGHT; y++) {

            for (int x = 0; x < GRID_WIDTH; x++) {

                if (grid[x][y] != 1) {

                    continue outer;

                }

            }



            rows.add(y);

        }



        return rows;

    }



    private void spawn() {

        Tetromino tetromino = original.get(new Random().nextInt(original.size())).copy();

        tetromino.move(GRID_WIDTH / 2, 0);



        selected = tetromino;



        tetrominos.add(tetromino);

        tetromino.pieces.forEach(this::placePiece);


        if (!isValidState()) {
            System.out.println("Game Over");

        /* end page test:
            MenuItem startover = new MenuItem("Start Over?");

            MenuItem exit3 = new MenuItem("Exit");
            exit3.setOnMouseClicked(event -> System.exit(0));

            VBox vbox = new VBox(20);
            vbox.getChildren().addAll(startover, exit3);
            vbox.setAlignment(Pos.CENTER);

            Scene gameover = new Scene(vbox, 420, 600, Color.BLACK);

            startover.setOnMouseClicked(e -> {sweepRows();
                createContent();});

           */


            System.exit(0);

        }

    }

    private static class MenuBox extends VBox
    {
        public MenuBox(MenuItem ... items)
        {
            getChildren().add(createSeparator());
            for(MenuItem item : items)
            {
                getChildren().addAll(item, createSeparator());
            }
        }
        private Line createSeparator()
        {
            Line sep = new Line();
            sep.setEndX(200);
            sep.setStroke(Color.DARKGRAY);
            return sep;
        }


    }

    private static class MenuItem extends StackPane
    {
        public MenuItem(String name)
        {
            LinearGradient gradient = new LinearGradient(0,0,1,0, true, CycleMethod.NO_CYCLE, new Stop[]{
                    new Stop(0,Color.CYAN),
                    new Stop(0.1,Color.BLACK),
                    new Stop(0.9,Color.BLACK),
                    new Stop(1,Color.CYAN),
            });

            Rectangle bg = new Rectangle(200,30);
            bg.setOpacity(0.4);

            Text text = new Text(name);
            text.setFill(Color.DARKGRAY);
            text.setFont(Font.font("Tw Cen MT Condensed", FontWeight.SEMI_BOLD, 22));

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);

            setOnMouseEntered(event -> {
                bg.setFill(gradient);
                text.setFill(Color.WHITE);
            });

            setOnMouseExited(event -> {
                bg.setFill(Color.BLACK);
                text.setFill(Color.DARKGREY);
            });

            setOnMousePressed(event -> {
                bg.setFill(Color.CYAN);
            });

            setOnMouseReleased(event -> {
                bg.setFill(gradient);

            });
        }
    }



    @Override

    public void start(Stage stage) throws Exception {
        Pane rootMenu = new Pane();
        rootMenu.setPrefSize(860,600);


        try (InputStream is = Files.newInputStream(Paths.get("res/Wall.png"))) {
            ImageView img = new ImageView(new Image(is));
            img.setFitWidth(860);
            img.setFitHeight(600);

            rootMenu.getChildren().add(img);
        }
        catch (IOException e) {
            System.out.println("Couldn't load ");
        }

/*      music test:
        Media song = new Media("/tetris/music/tetris.mp3");

        music = new MediaPlayer(song);
        music.setAutoPlay(true);

*/

        MenuItem itemExit = new MenuItem("Exit");
        itemExit.setOnMouseClicked(event -> System.exit(0));

        MenuItem itemPlay = new MenuItem("Play");



        MenuBox menu = new MenuBox(itemPlay, itemExit);

        menu.setTranslateX(335);
        menu.setTranslateY(350);

        rootMenu.getChildren().addAll( menu);

        Scene menuScene = new Scene(rootMenu, Color.BLACK);


        Scene scene = new Scene(createContent());

        itemPlay.setOnMouseClicked(e -> {
            stage.setScene(scene);
        });


        MenuItem exit2 = new MenuItem("Exit");
        exit2.setOnMouseClicked(event -> System.exit(0));

        MenuItem con = new MenuItem("Continue");
        con.setOnMouseClicked(e -> stage.setScene(scene));


        //  MenuBox pausemenu = new MenuBox(con, exit2);
        //  pausemenu.setTranslateX(50);
        // pausemenu.setTranslateY(100);



        VBox vbox = new VBox(20);
        vbox.getChildren().addAll(con, exit2);

        vbox.setAlignment(Pos.CENTER);

        Scene pause = new Scene(vbox,420, 600, Color.BLACK);


        scene.setOnKeyPressed(e -> {

            if (e.getCode() == KeyCode.SPACE) {

                makeMove(p -> p.rotate(), p -> p.rotateBack(), false);

            } else if (e.getCode() == KeyCode.LEFT) {

                makeMove(p -> p.move(Direction.LEFT), p -> p.move(Direction.RIGHT), false);

            } else if (e.getCode() == KeyCode.RIGHT) {

                makeMove(p -> p.move(Direction.RIGHT), p -> p.move(Direction.LEFT), false);

            } else if (e.getCode() == KeyCode.DOWN) {

                makeMove(p -> p.move(Direction.DOWN), p -> p.move(Direction.UP), true);
            } else if (e.getCode() == KeyCode.P) {
                stage.setScene(pause);

            }



            render();

        });

        stage.setScene(menuScene);
        stage.setTitle("Tetris");
        stage.show();

    }


    public static void main(String[] args) {

        launch(args);

    }

}