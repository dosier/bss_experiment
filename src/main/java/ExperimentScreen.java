import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is the starting point of the application.
 *
 * @author Stan van der Bend
 * @version 1.0
 * @since 2018-11-29
 */
public class ExperimentScreen extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        final Experiment experiment = new Experiment();

        experiment.buildSession().ifPresent(experimentSession -> {

            final Scene scene = experimentSession.buildScene();

            scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

            primaryStage.setTitle("BSS Experiment");
            primaryStage.setScene(scene);
            primaryStage.show();
        });
    }
}
